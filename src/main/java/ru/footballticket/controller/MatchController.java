package ru.footballticket.controller;

import ru.footballticket.entity.Match;
import ru.footballticket.entity.StadiumSector;
import ru.footballticket.entity.Ticket;
import ru.footballticket.service.MatchService;
import ru.footballticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final TicketService ticketService;

    // Загрузка страницы (первый раз)
    @GetMapping("/match/{id}")
    public String matchDetail(@PathVariable Long id,
                              @RequestParam(required = false) Long sectorId,
                              Model model) {
        Match match = matchService.findById(id);
        model.addAttribute("match", match);

        List<StadiumSector> sectors = matchService.getSectorsByMatch(id);
        model.addAttribute("sectors", sectors);

        // Первые 10 билетов
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ticket> ticketsPage;
        if (sectorId != null && sectorId > 0) {
            ticketsPage = ticketService.getTicketsByMatchAndSectorWithPagination(id, sectorId, pageable);
            model.addAttribute("selectedSectorId", sectorId);
        } else {
            ticketsPage = ticketService.getAvailableTicketsByMatchWithPagination(id, pageable);
        }

        model.addAttribute("tickets", ticketsPage.getContent());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalTickets", ticketsPage.getTotalElements());
        model.addAttribute("hasMore", ticketsPage.hasNext());

        model.addAttribute("minPrice", ticketService.getMinPriceByMatch(id));
        model.addAttribute("maxPrice", ticketService.getMaxPriceByMatch(id));

        return "match/match-detail";
    }

    // AJAX эндпоинт для фильтрации с пагинацией
    @GetMapping("/match/{id}/tickets")
    @ResponseBody
    public Map<String, Object> getTicketsBySector(@PathVariable Long id,
                                                  @RequestParam(required = false) Long sectorId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> ticketsPage;

        if (sectorId != null && sectorId > 0) {
            ticketsPage = ticketService.getTicketsByMatchAndSectorWithPagination(id, sectorId, pageable);
        } else {
            ticketsPage = ticketService.getAvailableTicketsByMatchWithPagination(id, pageable);
        }

        List<Map<String, Object>> ticketList = new ArrayList<>();
        for (Ticket ticket : ticketsPage.getContent()) {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("id", ticket.getId());
            ticketMap.put("sectorName", ticket.getSector().getName());
            ticketMap.put("rowNumber", ticket.getRowNumber());
            ticketMap.put("seatNumber", ticket.getSeatNumber());
            ticketMap.put("price", ticket.getPrice());
            ticketList.add(ticketMap);
        }

        response.put("tickets", ticketList);
        response.put("count", ticketList.size());
        response.put("totalCount", ticketsPage.getTotalElements());
        response.put("currentPage", page);
        response.put("hasMore", ticketsPage.hasNext());
        response.put("minPrice", ticketService.getMinPriceByMatch(id));
        response.put("maxPrice", ticketService.getMaxPriceByMatch(id));

        return response;
    }
}