package ru.footballticket.service;

import ru.footballticket.entity.Ticket;
import ru.footballticket.entity.Ticket.TicketStatus;
import ru.footballticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<Ticket> getAvailableTicketsByMatch(Long matchId) {
        return ticketRepository.findByMatchIdAndStatus(matchId, TicketStatus.AVAILABLE);
    }

    public Page<Ticket> getAvailableTicketsByMatchWithPagination(Long matchId, Pageable pageable) {
        return ticketRepository.findByMatchIdAndStatus(matchId, TicketStatus.AVAILABLE, pageable);
    }

    public List<Ticket> getTicketsByMatchAndSector(Long matchId, Long sectorId) {
        return ticketRepository.findByMatchIdAndSectorIdAndStatus(matchId, sectorId, TicketStatus.AVAILABLE);
    }

    public Page<Ticket> getTicketsByMatchAndSectorWithPagination(Long matchId, Long sectorId, Pageable pageable) {
        return ticketRepository.findByMatchIdAndSectorIdAndStatus(matchId, sectorId, TicketStatus.AVAILABLE, pageable);
    }

    public Double getMinPriceByMatch(Long matchId) {
        return ticketRepository.findMinPriceByMatchId(matchId);
    }

    public Double getMaxPriceByMatch(Long matchId) {
        return ticketRepository.findMaxPriceByMatchId(matchId);
    }
}