package com.DSfinal.broker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ConcurrentModel;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class SagaIntegrationTest {

    @Autowired
    private BrokerViewController brokerViewController;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FailureRecoveryScheduler failureRecoveryScheduler;

    private WireMockServer venueMock;
    private WireMockServer cateringMock;

    @BeforeEach
    public void setup() {
        venueMock = new WireMockServer(8083);
        cateringMock = new WireMockServer(8082);
        venueMock.start();
        cateringMock.start();
        
        //orderRepository.deleteAll();
    }

    @AfterEach
    public void teardown() {
        venueMock.stop();
        cateringMock.stop();
    }

    @Test
    public void testSupplierCrash_ShouldTriggerSagaRollback() throws Exception {
        // GIVEN: Venue confirm lukt, maar de compensatie-rollback faalt (bijv. netwerk weg)!
        venueMock.stubFor(post(urlPathEqualTo("/venue/confirm"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"success\":true}")));
        
        // De rollback van de Venue faalt nu expres (HTTP 500), zodat de rollback incompleet blijft!
        venueMock.stubFor(post(urlPathEqualTo("/venue/cancel"))
                .willReturn(aResponse().withStatus(500)));

        // Catering confirm crasht direct (HTTP 500)
        cateringMock.stubFor(post(urlPathEqualTo("/catering/confirm"))
                .willReturn(aResponse().withStatus(500))); 
                
        cateringMock.stubFor(post(urlPathEqualTo("/catering/cancel"))
                .willReturn(aResponse().withStatus(500)));

        // WHEN: Bevestigen van een geldige RESERVED order
        String orderId = "test-order-123";
        Order order = new Order(orderId, "3", "3", new Date());
        order.setStatus("RESERVED");
        order.setAddress("Wetenschapspark 1, Leuven");
        order.setCardNumber("1234-5678-9012-3456");
        orderRepository.save(order);

        brokerViewController.confirmOrder(orderId, "venue-1", "catering-1", "2026-06-06", new ConcurrentModel());

        // ================= VISUELE LOGGING TOEVOEGEN =================
        System.out.println("\n=== SAGA STAPPENVERLOOP VERLEDEN ===");
        venueMock.getAllServeEvents().forEach(e -> 
            System.out.println("[VENUE CALL] Oproep naar: " + e.getRequest().getUrl() + " -> Antwoord van server: " + e.getResponse().getStatus())
        );
        cateringMock.getAllServeEvents().forEach(e -> 
            System.out.println("[CATERING CALL] Oproep naar: " + e.getRequest().getUrl() + " -> Antwoord van server: " + e.getResponse().getStatus())
        );
        System.out.println("====================================\n");
        // =============================================================

        // THEN: De rollback is mislukt door de HTTP 500, dus de status MOET incompleet zijn
        Order finalOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals("FAILED_ROLLBACK_INCOMPLETE", finalOrder.getStatus());
    }

    @Test
    public void testNetworkFailure_ShouldRetryLaterViaScheduler() throws Exception {
        // GIVEN: De confirm call veroorzaakt een timeout
        venueMock.stubFor(post(urlPathEqualTo("/venue/confirm"))
                .willReturn(aResponse().withFixedDelay(5000)));

        // Laat de automatische back-out rollbacks óók mislukken, zodat de status incompleet blijft
        venueMock.stubFor(post(urlPathEqualTo("/venue/cancel"))
                .willReturn(aResponse().withStatus(500)));
                        
        cateringMock.stubFor(post(urlPathEqualTo("/catering/cancel"))
                .willReturn(aResponse().withStatus(500)));

        // WHEN: Order confirmen
        String orderId = "test-order-456";
        Order order = new Order(orderId, "venue-1", "catering-1", new Date());
        order.setStatus("RESERVED");
        order.setAddress("Andreas Vesaliusstraat 13, Leuven");
        order.setCardNumber("9876-5432-1098-7654");
        orderRepository.save(order);

        brokerViewController.confirmOrder(orderId, "venue-1", "catering-1", "2026-06-06", new ConcurrentModel());

        // THEN: Status moet FAILED_ROLLBACK_INCOMPLETE zijn, klaar voor de scheduler!
        Order checkOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals("FAILED_ROLLBACK_INCOMPLETE", checkOrder.getStatus());
    }

    @Test
    public void testBrokerCrashRecovery_SchedulerShouldCleanUpFloatingReservations() {
        // GIVEN: De herstel-scheduler draait en de supplier reageert nu wél weer netjes via JSON
        cateringMock.stubFor(post(urlPathEqualTo("/catering/cancel"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"success\":true}")));

        // We injecteren een "zwevende" order, overgebleven uit de crash
        String orderId = "gecrashte-broker-order";
        Order crashedOrder = new Order(orderId, "venue-1", "catering-1", new Date());
        crashedOrder.setStatus("FAILED_ROLLBACK_INCOMPLETE");
        crashedOrder.setPendingCompensations("catering");
        crashedOrder.setAddress("Naamsestraat 22, Leuven");
        crashedOrder.setCardNumber("5555-4444-3333-2222");
        crashedOrder.setRetryCount(0);
        crashedOrder.setLastRetryTime(new Date(System.currentTimeMillis() - 10000));
        orderRepository.save(crashedOrder);

        // WHEN: De herstel-scheduler activeert na de herstart
        failureRecoveryScheduler.retryFailedCompensations();

        // THEN: Alles is nu succesvol ingehaald en dichtgezet!
        Order recoveredOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals("FAILED_ROLLED_BACK", recoveredOrder.getStatus());
        assertNull(recoveredOrder.getPendingCompensations());
    }
}