package ru.footballticket.controller;

import ru.footballticket.entity.Order;
import ru.footballticket.entity.Ticket;
import ru.footballticket.repository.OrderRepository;
import ru.footballticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    /**
     * Главная страница менеджера - список заказов
     */
    @GetMapping
    public String managerDashboard(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalPaid", orderRepository.countByStatus(Order.OrderStatus.PAID));
        model.addAttribute("totalProcessing", orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        model.addAttribute("totalShipped", orderRepository.countByStatus(Order.OrderStatus.SHIPPED));
        model.addAttribute("totalCancelled", orderRepository.countByStatus(Order.OrderStatus.CANCELLED));
        return "manager/dashboard";
    }

    /**
     * Детали заказа
     */
    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElseThrow();
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
        model.addAttribute("order", order);
        model.addAttribute("tickets", tickets);
        return "manager/order-details";
    }

    /**
     * Изменить статус заказа (AJAX)
     */
    @PostMapping("/orders/{id}/status")
    @ResponseBody
    public Map<String, Object> updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderRepository.findById(id).orElseThrow();
            order.setStatus(status);
            orderRepository.save(order);
            response.put("success", true);
            response.put("message", "Статус заказа #" + order.getOrderNumber() + " изменён на " + status);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalPaid", orderRepository.countByStatus(Order.OrderStatus.PAID));
        stats.put("totalProcessing", orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        stats.put("totalShipped", orderRepository.countByStatus(Order.OrderStatus.SHIPPED));
        stats.put("totalCancelled", orderRepository.countByStatus(Order.OrderStatus.CANCELLED));
        return stats;
    }

    @GetMapping("/orders/{id}/details")
    @ResponseBody
    public Map<String, Object> getOrderDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Order order = orderRepository.findById(id).orElseThrow();
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());

        response.put("orderNumber", order.getOrderNumber());
        response.put("customerEmail", order.getCustomerEmail());
        response.put("totalAmount", order.getTotalAmount());
        response.put("status", order.getStatus().toString());
        response.put("orderDate", order.getOrderDate().toString());

        List<Map<String, Object>> ticketList = new ArrayList<>();
        for (Ticket ticket : tickets) {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("match", ticket.getMatch().getHomeTeam().getName() + " vs " + ticket.getMatch().getAwayTeam().getName());
            ticketMap.put("sector", ticket.getSector().getName());
            ticketMap.put("row", ticket.getRowNumber());
            ticketMap.put("seat", ticket.getSeatNumber());
            ticketMap.put("price", ticket.getPrice());
            ticketList.add(ticketMap);
        }
        response.put("tickets", ticketList);

        return response;
    }
}