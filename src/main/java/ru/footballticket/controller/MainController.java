package ru.footballticket.controller;

import org.springframework.web.bind.annotation.ResponseBody;
import ru.footballticket.entity.Match;
import ru.footballticket.entity.Stadium;
import ru.footballticket.entity.Team;
import ru.footballticket.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.footballticket.repository.StadiumRepository;
import ru.footballticket.repository.TeamRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Популярные матчи (топ-5, покажем 3)
        // то до немножко изменил и теперь тут популярные билеты, так что название немного не верно
        List<Match> popularMatches = matchRepository.findTop3ByOrderByTicketsSoldDesc();
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

        // Добавляем списки для выпадающих меню
        List<Team> allTeams = teamRepository.findAll();
        List<Stadium> allStadiums = stadiumRepository.findAll();
        model.addAttribute("allTeams", allTeams);
        model.addAttribute("allStadiums", allStadiums);

        return "match/matches";
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
        return "search/search-results";
    }
    @GetMapping("/matches/filter")
    @ResponseBody
    public Map<String, Object> filterMatches(@RequestParam(required = false) String team,
                                             @RequestParam(required = false) String stadium,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        List<Match> matches = matchRepository.findByFilters(null, team, stadium, fromDateTime, toDateTime);

        List<Map<String, Object>> matchList = new ArrayList<>();
        for (Match match : matches) {
            Map<String, Object> matchMap = new HashMap<>();
            matchMap.put("id", match.getId());
            matchMap.put("homeTeam", match.getHomeTeam().getName());
            matchMap.put("awayTeam", match.getAwayTeam().getName());
            matchMap.put("stadium", match.getStadium().getName());
            matchMap.put("city", match.getStadium().getCity());
            matchMap.put("month", match.getMatchDateTime().getMonth().toString().substring(0, 3));
            matchMap.put("day", String.format("%02d", match.getMatchDateTime().getDayOfMonth()));
            matchMap.put("year", match.getMatchDateTime().getYear());
            matchMap.put("time", String.format("%02d:%02d",
                    match.getMatchDateTime().getHour(),
                    match.getMatchDateTime().getMinute()));
            matchList.add(matchMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("matches", matchList);
        response.put("count", matchList.size());

        return response;
    }
}