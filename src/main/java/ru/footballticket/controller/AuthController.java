package ru.footballticket.controller;

import ru.footballticket.entity.User;
import ru.footballticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           RedirectAttributes redirectAttributes) {

        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email уже используется");
            return "redirect:/register";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(User.Role.USER);

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Регистрация успешна! Войдите в систему.");
        return "redirect:/login";
    }
}