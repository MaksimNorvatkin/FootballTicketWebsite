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
        return "order/cart";
    }

    @PostMapping("/add/{ticketId}")
    public String addToCart(@PathVariable Long ticketId, @RequestParam(required = false) Long matchId) {
        cartService.addTicket(ticketId);
        if (matchId != null) {
            return "redirect:/match/" + matchId;
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{ticketId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(@PathVariable Long ticketId) {
        cartService.removeTicket(ticketId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("itemCount", cartService.getItemCount());
        response.put("totalAmount", cartService.getTotalAmount());
        return response;
    }

    @PostMapping("/update/{ticketId}")
    public String updateQuantity(@PathVariable Long ticketId, @RequestParam int quantity) {
        cartService.updateQuantity(ticketId, quantity);
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

    @GetMapping("/check/{ticketId}")
    @ResponseBody
    public Map<String, Boolean> checkInCart(@PathVariable Long ticketId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("inCart", cartService.getItems().containsKey(ticketId));
        return response;
    }
}