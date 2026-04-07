package ru.footballticket.controller;

import ru.footballticket.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalAmount", cartService.getTotalAmount());
        model.addAttribute("itemCount", cartService.getItemCount());
        return "cart";
    }

    @PostMapping("/add/{ticketId}")
    public String addToCart(@PathVariable Long ticketId, @RequestParam(required = false) Long matchId) {
        cartService.addTicket(ticketId);
        if (matchId != null) {
            return "redirect:/match/" + matchId;
        }
        return "redirect:/cart";
    }

    @PostMapping("/update/{ticketId}")
    public String updateQuantity(@PathVariable Long ticketId, @RequestParam int quantity) {
        cartService.updateQuantity(ticketId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{ticketId}")
    public String removeFromCart(@PathVariable Long ticketId) {
        cartService.removeTicket(ticketId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart() {
        cartService.clear();
        return "redirect:/cart";
    }

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Integer> getCartCount() {
        Map<String, Integer> response = new HashMap<>();
        response.put("count", cartService.getItemCount());
        return response;
    }
}