package ru.footballticket.service;

import ru.footballticket.entity.Match;
import ru.footballticket.entity.StadiumSector;
import ru.footballticket.repository.MatchRepository;
import ru.footballticket.repository.StadiumSectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final StadiumSectorRepository stadiumSectorRepository;

    public Match findById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Матч не найден"));
    }

    public List<StadiumSector> getSectorsByMatch(Long matchId) {
        return stadiumSectorRepository.findByMatchId(matchId);
    }
}