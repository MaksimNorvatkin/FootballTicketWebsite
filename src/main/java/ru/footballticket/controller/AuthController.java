package ru.footballticket.controller;

import ru.footballticket.dto.UserRegistrationDto;
import ru.footballticket.entity.User;
import ru.footballticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

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
    public String registerForm(@ModelAttribute("user") UserRegistrationDto userDto) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email уже используется");
            return "redirect:/register";
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFullName(userDto.getFullName());
        user.setRole(User.Role.USER);

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Регистрация успешна! Войдите в систему.");
        return "redirect:/login";
    }
}



//
//import ru.footballticket.entity.User;
//import ru.footballticket.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @GetMapping("/login")
//    public String login() {
//        return "auth/login";
//    }
//
//    @GetMapping("/register")
//    public String registerForm() {
//        return "auth/register";
//    }
//
//    @PostMapping("/register")
//    public String register(@RequestParam String email,
//                           @RequestParam String password,
//                           @RequestParam String fullName,
//                           RedirectAttributes redirectAttributes) {
//
//        if (userRepository.existsByEmail(email)) {
//            redirectAttributes.addFlashAttribute("error", "Email уже используется");
//            return "redirect:/register";
//        }
//
//        User user = new User();
//        user.setEmail(email);
//        user.setPassword(passwordEncoder.encode(password));
//        user.setFullName(fullName);
//        user.setRole(User.Role.USER);
//
//        userRepository.save(user);
//        redirectAttributes.addFlashAttribute("success", "Регистрация успешна! Войдите в систему.");
//        return "redirect:/login";
//    }
//}
