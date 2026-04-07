package ru.footballticket.repository;

import ru.footballticket.entity.StadiumSector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StadiumSectorRepository extends JpaRepository<StadiumSector, Long> {
    List<StadiumSector> findByMatchId(Long matchId);
}