package ru.footballticket.repository;

import ru.footballticket.entity.Ticket;
import ru.footballticket.entity.Ticket.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByMatchIdAndStatus(Long matchId, TicketStatus status);

    List<Ticket> findByMatchIdAndSectorIdAndStatus(Long matchId, Long sectorId, TicketStatus status);

    @Query("SELECT MIN(t.price) FROM Ticket t WHERE t.match.id = :matchId AND t.status = 'AVAILABLE'")
    Double findMinPriceByMatchId(@Param("matchId") Long matchId);

    @Query("SELECT MAX(t.price) FROM Ticket t WHERE t.match.id = :matchId AND t.status = 'AVAILABLE'")
    Double findMaxPriceByMatchId(@Param("matchId") Long matchId);
}