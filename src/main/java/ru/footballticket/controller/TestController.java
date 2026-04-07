package ru.footballticket.controller;

import ru.footballticket.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final MatchRepository matchRepository;

    @GetMapping("/test-matches")
    public String testMatches(Model model) {
        model.addAttribute("matches", matchRepository.findAll());
        return "test-matches";
    }
}