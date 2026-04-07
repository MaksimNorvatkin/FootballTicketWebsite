package ru.footballticket.service;

import ru.footballticket.entity.Ticket;
import ru.footballticket.entity.Ticket.TicketStatus;
import ru.footballticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<Ticket> getAvailableTicketsByMatch(Long matchId) {
        return ticketRepository.findByMatchIdAndStatus(matchId, TicketStatus.AVAILABLE);
    }

    public List<Ticket> getTicketsByMatchAndSector(Long matchId, Long sectorId) {
        return ticketRepository.findByMatchIdAndSectorIdAndStatus(matchId, sectorId, TicketStatus.AVAILABLE);
    }

    public Double getMinPriceByMatch(Long matchId) {
        return ticketRepository.findMinPriceByMatchId(matchId);
    }

    public Double getMaxPriceByMatch(Long matchId) {
        return ticketRepository.findMaxPriceByMatchId(matchId);
    }
}