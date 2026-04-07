package ru.footballticket.controller;

import ru.footballticket.entity.Match;
import ru.footballticket.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MatchRepository matchRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Популярные матчи (топ-5, покажем 3)
        List<Match> popularMatches = matchRepository.findTop5ByOrderByPopularityScoreDesc();
        model.addAttribute("popularMatches", popularMatches.stream().limit(3).toList());

        // Ближайшие матчи (следующие 3)
        List<Match> allMatches = matchRepository.findAll();
        List<Match> upcomingMatches = allMatches.stream()
                .filter(m -> m.getMatchDateTime().isAfter(LocalDateTime.now()))
                .limit(3)
                .toList();
        model.addAttribute("upcomingMatches", upcomingMatches);

        return "index";
    }

    @GetMapping("/matches")
    public String matches(@RequestParam(required = false) String city,
                          @RequestParam(required = false) String team,
                          @RequestParam(required = false) String stadium,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                          Model model) {

        // Конвертируем LocalDate в LocalDateTime для запроса
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        // Получаем отфильтрованные матчи
        List<Match> matches = matchRepository.findByFilters(city, team, stadium, fromDateTime, toDateTime);
        model.addAttribute("matches", matches);

        // Сохраняем параметры фильтрации для отображения в форме
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedTeam", team);
        model.addAttribute("selectedStadium", stadium);
        model.addAttribute("selectedFromDate", fromDate);
        model.addAttribute("selectedToDate", toDate);

        // Добавляем счетчик результатов
        model.addAttribute("matchesCount", matches.size());

        return "matches";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            List<Match> searchResults = matchRepository.searchMatches(query.trim());
            model.addAttribute("matches", searchResults);
            model.addAttribute("searchQuery", query);
            model.addAttribute("matchesCount", searchResults.size());
        } else {
            model.addAttribute("matches", List.of());
            model.addAttribute("matchesCount", 0);
        }
        return "search-results";
    }
}