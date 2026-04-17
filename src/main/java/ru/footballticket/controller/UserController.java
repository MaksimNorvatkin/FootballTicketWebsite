package ru.footballticket.controller;

import ru.footballticket.entity.Order;
import ru.footballticket.entity.Ticket;
import ru.footballticket.entity.User;
import ru.footballticket.repository.OrderRepository;
import ru.footballticket.repository.TicketRepository;
import ru.footballticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    @GetMapping
    public String userProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByCustomerEmailOrderByOrderDateDesc(user.getEmail());

        // Для каждого заказа получаем билеты
        Map<Long, List<Ticket>> ticketsByOrder = new HashMap<>();
        for (Order order : orders) {
            List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
            ticketsByOrder.put(order.getId(), tickets);
        }

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("ticketsByOrder", ticketsByOrder);

        return "user/profile";
    }
}