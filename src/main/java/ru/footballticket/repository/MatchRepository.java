package ru.footballticket.repository;

import ru.footballticket.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // Сложная фильтрация для главного поиска
    @Query("SELECT m FROM Match m WHERE " +
            "(:city IS NULL OR LOWER(m.stadium.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:team IS NULL OR LOWER(m.homeTeam.name) LIKE LOWER(CONCAT('%', :team, '%')) OR LOWER(m.awayTeam.name) LIKE LOWER(CONCAT('%', :team, '%'))) AND " +
            "(:stadium IS NULL OR LOWER(m.stadium.name) LIKE LOWER(CONCAT('%', :stadium, '%'))) AND " +
            "(:fromDate IS NULL OR m.matchDateTime >= :fromDate) AND " +
            "(:toDate IS NULL OR m.matchDateTime <= :toDate)")
    List<Match> findByFilters(@Param("city") String city,
                              @Param("team") String team,
                              @Param("stadium") String stadium,
                              @Param("fromDate") LocalDateTime fromDate,
                              @Param("toDate") LocalDateTime toDate);

    // Популярные матчи
    List<Match> findTop5ByOrderByPopularityScoreDesc();

    // Поиск по всем полям для header
    @Query("SELECT m FROM Match m WHERE " +
            "LOWER(m.homeTeam.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.awayTeam.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.stadium.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.stadium.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Match> searchMatches(@Param("query") String query);
}