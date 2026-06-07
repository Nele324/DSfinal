//this controller is created to handle manger only pages
//why can't we just put all these methods in brokerviewcontroller?
//technically we can
// there's no issue in that
// since springsecurity watched URL patterns, not which java class handles them
//but we seperate this controller for the sake of code organisation


package com.DSfinal.broker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/orders")
    public String viewOrders(Model model) {
        // Only reachable if Spring Security verified ROLE_MANAGER
        model.addAttribute("orders", orderRepository.findAll());
        return "manager-view-orders";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/oauth2/authorization/okta";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/logout";
    }
}
