package ru.footballticket.controller;

import ru.footballticket.entity.Match;
import ru.footballticket.entity.StadiumSector;
import ru.footballticket.entity.Ticket;
import ru.footballticket.service.MatchService;
import ru.footballticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final TicketService ticketService;

    @GetMapping("/match/{id}")
    public String matchDetail(@PathVariable Long id,
                              @RequestParam(required = false) Long sectorId,
                              Model model) {
        Match match = matchService.findById(id);
        model.addAttribute("match", match);

        // Сектора стадиона
        List<StadiumSector> sectors = matchService.getSectorsByMatch(id);
        model.addAttribute("sectors", sectors);

        // Билеты (с фильтром по сектору или все)
        List<Ticket> tickets;
        if (sectorId != null && sectorId > 0) {
            tickets = ticketService.getTicketsByMatchAndSector(id, sectorId);
            model.addAttribute("selectedSectorId", sectorId);
        } else {
            tickets = ticketService.getAvailableTicketsByMatch(id);
        }
        model.addAttribute("tickets", tickets);

        // Диапазон цен
        model.addAttribute("minPrice", ticketService.getMinPriceByMatch(id));
        model.addAttribute("maxPrice", ticketService.getMaxPriceByMatch(id));

        return "match-detail";
    }
}