package com.DSfinal.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * SAGA Pattern: Compensating Transactions with Retry Mechanism
 * 
 * This scheduler handles failure recovery for orders that failed rollback:
 * - Monitors orders with status FAILED_ROLLBACK_INCOMPLETE
 * - Retries compensating transactions (cancellations) with exponential backoff
 * - Tracks retry attempts and pending compensations per supplier
 * - Maximum 5 retries over ~15 minutes (exponential backoff: 1s, 3s, 9s, 27s, 81s)
 */
@Service
public class FailureRecoveryScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(FailureRecoveryScheduler.class);
    
    private static final int MAX_RETRY_ATTEMPTS = 6;
    private static final long RETRY_INTERVAL_MS = 1000; // Start with 1 second
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private BrokerController brokerController;
    
    /**
     * Run every 30 seconds to check for failed orders needing compensation retries
     * Scheduled at: 30 second intervals
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 60000)
    public void retryFailedCompensations() {
        log.info("=== FAILURE RECOVERY SCHEDULER START ===");

        List<Order> allOrders = orderRepository.findAll();
        List<Order> ordersToProcess = new ArrayList<>();
        List<Order> pendingOrders = new ArrayList<>();

        int amountIncomplete = 0;
        int amountCrashed = 0;
        int pendingCount = 0;

        for (Order order : allOrders) {
            String status = order.getStatus();
            if (status == null) continue;

            if (status.equals("FAILED_ROLLBACK_INCOMPLETE")) {
                ordersToProcess.add(order);
                amountIncomplete++;
            } else if (status.equals("RESERVED")) {

                if (order.getLastRetryTime() == null) {
                    order.setLastRetryTime(new Date());
                    order.setRetryCount(1); // Start de eerste tick
                    orderRepository.save(order);
                    continue; 
                }
                // Check if this order has been in RESERVED for more than 5 minutes (indicating a possible crash)
                int currentTicks = order.getRetryCount() != null ? order.getRetryCount() : 0; 
                if (currentTicks >= 60) { 
                    log.warn("Order {} has been in RESERVED status for over 24 hours - treating as potential crash", order.getId());
                    order.setStatus("FAILED_ROLLBACK_INCOMPLETE");
                    order.setPendingCompensations("venue,catering");
                    order.setRetryCount(0);
                    order.setLastRetryTime(new Date());
                    orderRepository.save(order);
                    ordersToProcess.add(order);
                    amountCrashed++;
                } else {
                    order.setRetryCount(currentTicks + 1);
                    orderRepository.save(order);
                    log.debug("Order {} is in RESERVED status but not yet considered crashed (ticks: {})", order.getId(), currentTicks);
                }
            } else if (status.equals("PENDING")) {
                pendingOrders.add(order);
                pendingCount++;
            }
        }

        log.info("Found {} orders with FAILED_ROLLBACK_INCOMPLETE status", amountIncomplete);
        log.info("Found {} orders with RESERVED status that may have crashed", amountCrashed);
        log.info("Found {} orders with PENDING status", pendingCount);
        
        for (Order order : ordersToProcess) {
            retryCompensatingTransaction(order);
        }

        for (Order order : pendingOrders) {
            retryCompensatingTransactionPending(order);
        }
        
        log.info("=== FAILURE RECOVERY SCHEDULER END ===");
    }

    private String retryCompensatingTransaction(Order order) {
        int retryCount = order.getRetryCount() != null ? order.getRetryCount() : 0;
        
        // Maximum 5 retries
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            log.error("PERMANENT FAILURE: Order {} exceeded max retry attempts ({}/{}). " +
                    "Manual intervention required!", order.getId(), retryCount, MAX_RETRY_ATTEMPTS);
            order.setStatus("FAILED_PERMANENT");
            orderRepository.save(order);
            return "order-failed";
        }
        
        // Exponential backoff: check if enough time has passed since last retry
        Date lastRetry = order.getLastRetryTime();
        long timeSinceLastRetry = lastRetry != null ? 
            (System.currentTimeMillis() - lastRetry.getTime()) : Long.MAX_VALUE;
        
        long nextRetryTime = calculateBackoffDelay(retryCount);
        
        if (timeSinceLastRetry < nextRetryTime) {
            log.debug("Order {} retry too soon. Last retry: {}ms ago, next: {}ms", 
                    order.getId(), timeSinceLastRetry, nextRetryTime);
            return "order-failed";
        }
        
        log.info("RETRY #{}/{} for order {} with pending compensations: {}", 
                retryCount + 1, MAX_RETRY_ATTEMPTS, order.getId(), order.getPendingCompensations());
        
        String pendingComps = order.getPendingCompensations();
        if (pendingComps == null || pendingComps.isEmpty()) {
            log.warn("Order {} has no pending compensations listed", order.getId());
            return "order-failed";
        }
        
        // Parse pending compensations and retry them
        boolean allCompensationsSucceeded = true;
        String[] suppliers = pendingComps.split(",");
        
        for (String supplier : suppliers) {
            supplier = supplier.trim();
            boolean success = retrySingleCompensation(order, supplier);
            if (!success) {
                allCompensationsSucceeded = false;
            }
        }
        
        // Update retry tracking
        order.setRetryCount(retryCount + 1);
        order.setLastRetryTime(new Date());
        
        if (allCompensationsSucceeded) {
            log.info("✓ All compensations succeeded for order {}", order.getId());
            order.setStatus("FAILED_ROLLED_BACK"); // Mark as eventually recovered
            order.setPendingCompensations(null);
            orderRepository.save(order);
            return "order-failed";
        }
        
        orderRepository.save(order);
        return "order-failed";
    }
    
    /**
     * Retry compensating transactions with exponential backoff
     */
    private String retryCompensatingTransactionPending(Order order) {
        int retryCount = order.getRetryCount() != null ? order.getRetryCount() : 0;
        
        // STRIKTE DEADLINE CHECK VIA RETRY COUNT (Level 2 Requirement)
        // Omdat de scheduler elke 30 seconden draait en exponential backoff toepast,
        // staat een MAX_RETRY_ATTEMPTS van 10 gelijk aan ruim 15 minuten aan totale tijd.
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            log.error("ORDER FAILURE: Order {} has exceeded the 15-minute deadline or reached maximum retries ({}/{}).", 
                    order.getId(), retryCount, MAX_RETRY_ATTEMPTS);
            
            order.setStatus("FAILED_ROLLBACK_INCOMPLETE");
            order.setRetryCount(0);
            order.setLastRetryTime(new Date());
            orderRepository.save(order);
            return "order-failed";
        }
        
        // Exponential backoff check: kijken of er al genoeg tijd is verstreken sinds de vorige poging
        Date lastRetry = order.getLastRetryTime();
        long timeSinceLastRetry = lastRetry != null ? 
            (System.currentTimeMillis() - lastRetry.getTime()) : Long.MAX_VALUE;
        
        long nextRetryTime = calculateBackoffDelay(retryCount);
        
        if (timeSinceLastRetry < nextRetryTime) {
            log.debug("Order {} retry too soon. Last retry: {}ms ago, next: {}ms", 
                    order.getId(), timeSinceLastRetry, nextRetryTime);
            return "order-failed";
        }
        
        log.info("RETRY ASYNC SAGA #{}/{} voor order {}", 
                retryCount + 1, MAX_RETRY_ATTEMPTS, order.getId());

        boolean venueSuccess = false;
        boolean cateringSuccess = false;
        
        String formatDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getDate());

        String pending = order.getPendingCompensations();

        if (pending == null || pending.isEmpty()) {
            venueSuccess = cateringSuccess = true;
            
        } else {
            if (pending.contains("catering")) {
                try {
                    cateringSuccess = brokerController.confirmCatering(order.getCateringId(), formatDate);
                    if (cateringSuccess) {
                        log.info("✓ Catering confirmed successfully during retry for order {}", order.getId());
                        removePendingCompensation(order, "catering");
                        log.info("Updated pending compensations for order {}: {}", order.getId(), order.getPendingCompensations());
                        pending = order.getPendingCompensations(); 
                    }
                } catch (Exception e) {
                    cateringSuccess = false;
                }
            } else {
                cateringSuccess = true;
            }

            if (pending != null && pending.contains("venue")) {
                try {
                    venueSuccess = brokerController.confirmVenue(order.getVenueId(), formatDate);
                    if (venueSuccess) {
                        log.info("✓ Venue confirmed successfully during retry for order {}", order.getId());
                        removePendingCompensation(order, "venue");
                        log.info("Updated pending compensations for order {}: {}", order.getId(), order.getPendingCompensations());
                        pending = order.getPendingCompensations();
                    }
                } catch (Exception e) {
                    venueSuccess = false;
                }
            } else {
                venueSuccess = true;
            }
        }
        
        if (venueSuccess && cateringSuccess) {
            log.info("🎉 Full success! PENDING order {} is confirmed within 15 minutes!", order.getId());
            order.setStatus("CONFIRMED");
            order.setPendingCompensations(null);
            order.setRetryCount(0);
            order.setLastRetryTime(new Date());
            orderRepository.save(order);
            return "order-success";

        } else {
            // Het is nog niet gelukt. We verhogen de teller en proberen het later opnieuw via backoff
            log.warn("✗ One or more suppliers are still needed. Order {} stays PENDING.", order.getId());
            order.setRetryCount(retryCount + 1);
            order.setStatus("PENDING");
        }
        
        // Update de tracking gegevens in de database
        order.setLastRetryTime(new Date());
        orderRepository.save(order);
        return "order-processing";
    }
    
    /**
     * Retry a single compensating transaction (cancel for one supplier)
     */
    private boolean retrySingleCompensation(Order order, String supplier) {
        try {
            boolean success = false;
            
            if ("venue".equalsIgnoreCase(supplier)) {
                log.info("Retrying venue cancellation for order {}", order.getId());
                success = brokerController.cancelVenue(order.getVenueId(), 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getDate()));
            } 
            else if ("catering".equalsIgnoreCase(supplier)) {
                log.info("Retrying catering cancellation for order {}", order.getId());
                success = brokerController.cancelCatering(order.getCateringId(), 
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getDate()));
            }
            
            if (success) {
                log.info("✓ Compensation succeeded for {} on order {}", supplier, order.getId());
                // Remove this supplier from pending list
                removePendingCompensation(order, supplier);
            } else {
                log.warn("✗ Compensation failed for {} on order {} - will retry", supplier, order.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Exception during compensation retry for {} on order {}: {}", 
                    supplier, order.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove a supplier from the pending compensations list
     */
    private void removePendingCompensation(Order order, String supplier) {
        String pending = order.getPendingCompensations();
        if (pending == null) return;
        
        String[] suppliers = pending.split(",");
        StringBuilder updated = new StringBuilder();
        
        for (String s : suppliers) {
            if (!s.trim().equalsIgnoreCase(supplier)) {
                if (updated.length() > 0) updated.append(",");
                updated.append(s.trim());
            }
        }
        
        order.setPendingCompensations(updated.length() > 0 ? updated.toString() : null);
    }
    
    /**
     * Calculate exponential backoff delay
     * 1s, 3s, 9s, 27s, 81s
     */
    private long calculateBackoffDelay(int retryCount) {
        return RETRY_INTERVAL_MS * (long) Math.pow(3, retryCount);
    }
}
