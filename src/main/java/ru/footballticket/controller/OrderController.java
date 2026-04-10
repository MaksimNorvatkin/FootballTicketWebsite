package ru.footballticket.controller;

import ru.footballticket.entity.Order;
import ru.footballticket.entity.Ticket;
import ru.footballticket.entity.Ticket.TicketStatus;
import ru.footballticket.repository.OrderRepository;
import ru.footballticket.repository.TicketRepository;
import ru.footballticket.repository.UserRepository;
import ru.footballticket.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        if (cartService.getItemCount() == 0) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
        return "order/checkout";
    }

    @PostMapping("/checkout/submit")
    public String submitOrder(@RequestParam String customerEmail,
                              @RequestParam String customerName,
                              Model model) {
        if (cartService.getItemCount() == 0) {
            return "redirect:/cart";
        }

        // Создаем заказ
        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(cartService.getTotalAmount());
        order.setStatus(Order.OrderStatus.PAID);
        order.setCustomerEmail(customerEmail);

        order = orderRepository.save(order);

        // Обновляем билеты и связываем с заказом
        List<Ticket> ticketsToSave = new ArrayList<>();
        for (CartService.CartItem item : cartService.getItems().values()) {
            Ticket ticket = item.getTicket();
            ticket.setStatus(TicketStatus.PAID);
            ticket.setOrder(order);
            ticketsToSave.add(ticket);
        }
        ticketRepository.saveAll(ticketsToSave);

        // Очищаем корзину
        cartService.clear();

        model.addAttribute("orderNumber", order.getOrderNumber());
        model.addAttribute("totalAmount", order.getTotalAmount());
        model.addAttribute("customerEmail", customerEmail);

        return "order/order-success";
    }

    @GetMapping("/track-order")
    public String trackOrderForm() {
        return "order/track-order";
    }

    @PostMapping("/track-order")
    public String trackOrder(@RequestParam String orderNumber,
                             @RequestParam String email,
                             Model model) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElse(null);

        if (order != null && order.getCustomerEmail().equals(email)) {
            model.addAttribute("order", order);
            List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
            model.addAttribute("tickets", tickets);
        } else {
            model.addAttribute("error", "Заказ не найден. Проверьте номер заказа и email.");
        }
        return "order/track-order";
    }
}