package ru.footballticket.service;

import ru.footballticket.entity.Ticket;
import ru.footballticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@SessionScope  // Хранится в сессии для каждого пользователя
@RequiredArgsConstructor
public class CartService {

    private final TicketRepository ticketRepository;
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    // Добавить билет в корзину
    public void addTicket(Long ticketId) {
        CartItem existingItem = items.get(ticketId);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
        } else {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Билет не найден"));
            items.put(ticketId, new CartItem(ticket, 1));
        }
    }

    // Удалить билет из корзины
    public void removeTicket(Long ticketId) {
        items.remove(ticketId);
    }

    // Обновить количество
    public void updateQuantity(Long ticketId, int quantity) {
        if (quantity <= 0) {
            removeTicket(ticketId);
        } else {
            CartItem item = items.get(ticketId);
            if (item != null) {
                item.setQuantity(quantity);
            }
        }
    }

    // Получить все товары в корзине
    public Map<Long, CartItem> getItems() {
        return items;
    }

    // Получить общую сумму
    public double getTotalAmount() {
        return items.values().stream()
                .mapToDouble(item -> item.getTicket().getPrice() * item.getQuantity())
                .sum();
    }

    // Получить количество товаров
    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Очистить корзину
    public void clear() {
        items.clear();
    }

    // Вложенный класс для товара в корзине
    public static class CartItem {
        private Ticket ticket;
        private int quantity;

        public CartItem(Ticket ticket, int quantity) {
            this.ticket = ticket;
            this.quantity = quantity;
        }

        public Ticket getTicket() { return ticket; }
        public void setTicket(Ticket ticket) { this.ticket = ticket; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getSubtotal() {
            return ticket.getPrice() * quantity;
        }
    }
}