package ru.footballticket.controller;

import ru.footballticket.entity.*;
import ru.footballticket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final StadiumSectorRepository sectorRepository;
    private final OrderRepository orderRepository;

    /**
     * Главная страница админ-панели (дашборд)
     */
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalMatches", matchRepository.count());
        model.addAttribute("totalTickets", ticketRepository.count());
        model.addAttribute("totalOrders", 0); // TODO: добавить OrderRepository
        return "admin/dashboard";
    }

    /**
     * Управление пользователями
     */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    /**
     * Выдать/изменить роль пользователя (AJAX)
     */
    @PostMapping("/users/{id}/role")
    @ResponseBody
    public Map<String, Object> changeUserRole(@PathVariable Long id, @RequestParam User.Role role) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(id).orElseThrow();
            user.setRole(role);
            userRepository.save(user);
            response.put("success", true);
            response.put("message", "Роль пользователя " + user.getEmail() + " изменена на " + role);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    /**
     * удаление юзера (AJAX)
     */
    @DeleteMapping("/users/{id}")
    @ResponseBody
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(id).orElseThrow();
            String email = user.getEmail();
            userRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Пользователь " + email + " удалён");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Управление матчами
     */
    @GetMapping("/matches")
    public String manageMatches(Model model) {
        model.addAttribute("matches", matchRepository.findAll());
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("stadiums", stadiumRepository.findAll());
        return "admin/matches";
    }

    /**
     * Получить все команды (AJAX)
     */
    @GetMapping("/teams")
    @ResponseBody
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * Получить все стадионы (AJAX)
     */
    @GetMapping("/stadiums")
    @ResponseBody
    public List<Stadium> getAllStadiums() {
        return stadiumRepository.findAll();
    }

    /**
     * Создать новый матч (AJAX)
     * При создании матча автоматически создаются билеты для всех секторов
     */
    @PostMapping("/matches/create")
    @ResponseBody
    public Map<String, Object> createMatch(@RequestParam Long homeTeamId,
                                           @RequestParam Long awayTeamId,
                                           @RequestParam Long stadiumId,
                                           @RequestParam String matchDateTime) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Проверка на дублирование (нельзя создать матч с теми же командами на том же стадионе в то же время)
            LocalDateTime dateTime = LocalDateTime.parse(matchDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            boolean exists = matchRepository.findAll().stream().anyMatch(m ->
                    m.getHomeTeam().getId().equals(homeTeamId) &&
                            m.getAwayTeam().getId().equals(awayTeamId) &&
                            m.getStadium().getId().equals(stadiumId) &&
                            m.getMatchDateTime().isEqual(dateTime)
            );

            if (exists) {
                response.put("success", false);
                response.put("message", "Такой матч уже существует!");
                return response;
            }

            Team homeTeam = teamRepository.findById(homeTeamId).orElseThrow();
            Team awayTeam = teamRepository.findById(awayTeamId).orElseThrow();
            Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow();

            Match match = new Match();
            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);
            match.setStadium(stadium);
            match.setMatchDateTime(dateTime);
            match.setDescription(homeTeam.getName() + " vs " + awayTeam.getName() + " - " + stadium.getName());

            Match savedMatch = matchRepository.save(match);

//            // Создаём билеты для всех секторов (по умолчанию)
//            generateTicketsForMatch(savedMatch);

            response.put("success", true);
            response.put("message", "Матч успешно создан");
            response.put("matchId", savedMatch.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Генерация билетов для матча
     */
    private void generateTicketsForMatch(Match match) {
        List<StadiumSector> sectors = sectorRepository.findByMatchId(match.getId());
        if (sectors.isEmpty()) {
            // Создаём стандартные сектора, если их нет
            String[] sectorNames = {"North", "NorthEast", "East", "West", "South", "SouthWest"};
            Double[] multipliers = {1.0, 1.5, 0.8, 0.8, 1.0, 1.5};
            String[] colors = {"#e74c3c", "#e67e22", "#f1c40f", "#2ecc71", "#3498db", "#9b59b6"};

            for (int i = 0; i < sectorNames.length; i++) {
                StadiumSector sector = new StadiumSector();
                sector.setName(sectorNames[i]);
                sector.setColor(colors[i]);
                sector.setPriceMultiplier(multipliers[i]);
                sector.setMatch(match);
                sector = sectorRepository.save(sector);
                sectors.add(sector);
            }
        }

        double basePrice = 50.0;
        for (StadiumSector sector : sectors) {
            for (int row = 1; row <= 10; row++) {
                for (int seat = 1; seat <= 5; seat++) {
                    Ticket ticket = new Ticket();
                    ticket.setTicketNumber(String.format("TKT-%d-%s-%d-%d",
                            match.getId(), sector.getName(), row, seat));
                    ticket.setRowNumber(row);
                    ticket.setSeatNumber(seat);
                    ticket.setPrice(basePrice * sector.getPriceMultiplier());
                    ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
                    ticket.setMatch(match);
                    ticket.setSector(sector);
                    ticketRepository.save(ticket);
                }
            }
        }
    }

    /**
     * Страница управления билетами для конкретного матча
     */
    @GetMapping("/matches/{id}/tickets")
    public String manageMatchTickets(@PathVariable Long id, Model model) {
        Match match = matchRepository.findById(id).orElseThrow();
        List<StadiumSector> sectors = sectorRepository.findByMatchId(id);

        // Если секторов нет - создаём стандартные
        if (sectors.isEmpty()) {
            createDefaultSectors(match);
            sectors = sectorRepository.findByMatchId(id);
        }

        // Получаем билеты для каждого сектора
        Map<String, Object> sectorData = new HashMap<>();
        for (StadiumSector sector : sectors) {
            List<Ticket> tickets = ticketRepository.findByMatchIdAndSectorId(id, sector.getId());
            sectorData.put(sector.getName(), Map.of(
                    "sector", sector,
                    "tickets", tickets,
                    "priceMultiplier", sector.getPriceMultiplier(),
                    "availableCount", tickets.stream().filter(t -> t.getStatus() == Ticket.TicketStatus.AVAILABLE).count()
            ));
        }

        model.addAttribute("match", match);
        model.addAttribute("sectors", sectors);
        model.addAttribute("sectorData", sectorData);

        return "admin/match-tickets";
    }

    /**
     * Создание стандартных секторов для матча
     */
    private void createDefaultSectors(Match match) {
        String[] sectorNames = {"North", "NorthEast", "East", "West", "South", "SouthWest"};
        String[] colors = {"#e74c3c", "#e67e22", "#f1c40f", "#2ecc71", "#3498db", "#9b59b6"};

        for (int i = 0; i < sectorNames.length; i++) {
            StadiumSector sector = new StadiumSector();
            sector.setName(sectorNames[i]);
            sector.setColor(colors[i]);
            sector.setPriceMultiplier(1.0);
            sector.setMatch(match);
            sectorRepository.save(sector);
        }
    }

    /**
     * Генерация билетов для сектора (AJAX)
     */
    @PostMapping("/matches/{matchId}/sectors/{sectorId}/generate")
    @ResponseBody
    public Map<String, Object> generateTickets(@PathVariable Long matchId,
                                               @PathVariable Long sectorId,
                                               @RequestParam(defaultValue = "10") int rows,
                                               @RequestParam(defaultValue = "5") int seatsPerRow,
                                               @RequestParam Double basePrice) {
        Map<String, Object> response = new HashMap<>();
        try {
            Match match = matchRepository.findById(matchId).orElseThrow();
            StadiumSector sector = sectorRepository.findById(sectorId).orElseThrow();

            // Проверяем, есть ли оплаченные билеты в этом секторе
            List<Ticket> paidTickets = ticketRepository.findByMatchIdAndSectorIdAndStatus(
                    matchId, sectorId, Ticket.TicketStatus.PAID);

            if (!paidTickets.isEmpty()) {
                response.put("success", false);
                response.put("message", "Нельзя удалить сектор с оплаченными билетами! (" + paidTickets.size() + " шт.)");
                return response;
            }

            // Удаляем только доступные билеты (AVAILABLE и RESERVED)
            List<Ticket> availableTickets = ticketRepository.findByMatchIdAndSectorIdAndStatus(
                    matchId, sectorId, Ticket.TicketStatus.AVAILABLE);
            ticketRepository.deleteAll(availableTickets);

            // Создаём новые билеты
            int count = 0;
            for (int row = 1; row <= rows; row++) {
                for (int seat = 1; seat <= seatsPerRow; seat++) {
                    Ticket ticket = new Ticket();
                    ticket.setTicketNumber(String.format("TKT-%d-%s-R%d-S%d",
                            match.getId(), sector.getName(), row, seat));
                    ticket.setRowNumber(row);
                    ticket.setSeatNumber(seat);
                    ticket.setPrice(basePrice);
                    ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
                    ticket.setMatch(match);
                    ticket.setSector(sector);
                    ticketRepository.save(ticket);
                    count++;
                }
            }

            response.put("success", true);
            response.put("message", "Создано " + count + " билетов");
            response.put("count", count);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Обновление цены билетов в секторе (AJAX)
     */
    @PostMapping("/matches/{matchId}/sectors/{sectorId}/update-price")
    @ResponseBody
    public Map<String, Object> updateSectorPrice(@PathVariable Long matchId,
                                                 @PathVariable Long sectorId,
                                                 @RequestParam Double newPrice) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Ticket> tickets = ticketRepository.findByMatchIdAndSectorId(matchId, sectorId);
            for (Ticket ticket : tickets) {
                ticket.setPrice(newPrice);
            }
            ticketRepository.saveAll(tickets);

            response.put("success", true);
            response.put("message", "Цены обновлены для " + tickets.size() + " билетов");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Удалить матч (AJAX)
     */
    @DeleteMapping("/matches/{id}")
    @ResponseBody
    public Map<String, Object> deleteMatch(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            matchRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Матч удалён");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    /**
     * Страница управления заказами
     */
    @GetMapping("/orders")
    public String manageOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalPaid", orderRepository.countByStatus(Order.OrderStatus.PAID));
        model.addAttribute("totalProcessing", orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        model.addAttribute("totalShipped", orderRepository.countByStatus(Order.OrderStatus.SHIPPED));
        model.addAttribute("totalCancelled", orderRepository.countByStatus(Order.OrderStatus.CANCELLED));
        return "admin/orders";
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

    /**
     * Получить детали заказа (AJAX)
     */
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
    @GetMapping("/orders/stats")
    @ResponseBody
    public Map<String, Object> getOrdersStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalPaid", orderRepository.countByStatus(Order.OrderStatus.PAID));
        stats.put("totalProcessing", orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        stats.put("totalShipped", orderRepository.countByStatus(Order.OrderStatus.SHIPPED));
        stats.put("totalCancelled", orderRepository.countByStatus(Order.OrderStatus.CANCELLED));
        return stats;
    }
}