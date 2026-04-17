package ru.footballticket.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.footballticket.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Отключаем CSRF для разработки
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                        // Публичные страницы
                        .requestMatchers("/", "/index", "/matches/**", "/match/**",
                                "/search", "/track-order", "/login", "/register",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        // Админ
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Менеджер
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        // Корзина и заказы требуют авторизации
                        .requestMatchers("/cart/**", "/checkout/**").authenticated()
                        // Все остальное требует авторизации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(86400) // 24 часа
                );

        return http.build();
    }
}