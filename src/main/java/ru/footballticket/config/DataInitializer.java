package ru.footballticket.config;

import ru.footballticket.entity.*;
import ru.footballticket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final StadiumSectorRepository sectorRepository;
    private final TicketRepository ticketRepository;
    //private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Создаем пользователей
        User admin = new User();
        admin.setEmail("admin@footballticket.ru");
        admin.setPassword("admin123");
        admin.setRole(User.Role.ADMIN);
        admin.setFullName("System Administrator");
        userRepository.save(admin);

        User manager = new User();
        manager.setEmail("manager@footballticket.ru");
        manager.setPassword("manager123");
        manager.setRole(User.Role.MANAGER);
        manager.setFullName("Ticket Manager");
        userRepository.save(manager);

        // Создаем матчи
        Match match1 = createMatch("Liverpool", "Manchester City",
                "Liverpool", "Anfield Road",
                LocalDateTime.now().plusDays(7), 100);

        Match match2 = createMatch("Arsenal", "Chelsea",
                "London", "Emirates Stadium",
                LocalDateTime.now().plusDays(14), 95);

        Match match3 = createMatch("Manchester United", "Tottenham",
                "Manchester", "Old Trafford",
                LocalDateTime.now().plusDays(21), 88);

        // Создаем 6 секторов и билеты для каждого матча
        String[] sectors = {"North", "South", "East", "West", "NorthEast", "SouthWest"};
        Double[] multipliers = {1.0, 1.0, 0.8, 0.8, 1.5, 1.5};

        for (Match match : Arrays.asList(match1, match2, match3)) {
            for (int i = 0; i < sectors.length; i++) {
                StadiumSector sector = new StadiumSector();
                sector.setName(sectors[i]);
                sector.setColor(getColorForSector(i));
                sector.setPriceMultiplier(multipliers[i]);
                sector.setMatch(match);
                sectorRepository.save(sector);

                // Создаем билеты (по 10 билетов в сектор)
                double basePrice = 50.0;
                for (int row = 1; row <= 10; row++) {
                    for (int seat = 1; seat <= 5; seat++) {
                        Ticket ticket = new Ticket();
                        ticket.setTicketNumber(String.format("TKT-%d-%s-%d-%d",
                                match.getId(), sectors[i], row, seat));
                        ticket.setRowNumber(row);
                        ticket.setSeatNumber(seat);
                        ticket.setPrice(basePrice * multipliers[i]);
                        ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
                        ticket.setMatch(match);
                        ticket.setSector(sector);
                        ticketRepository.save(ticket);
                    }
                }
            }
        }

        System.out.println("✅ База данных заполнена тестовыми данными!");
        System.out.println("👑 Admin: admin@footballticket.ru / admin123");
        System.out.println("📋 Manager: manager@footballticket.ru / manager123");
    }

    private Match createMatch(String home, String away, String city,
                              String stadium, LocalDateTime date, int popularity) {
        Match match = new Match();
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setCity(city);
        match.setStadium(stadium);
        match.setMatchDateTime(date);
        match.setDescription(home + " vs " + away + " - " + stadium);
        match.setPopularityScore(popularity);
        return matchRepository.save(match);
    }

    private String getColorForSector(int index) {
        String[] colors = {"#3498db", "#2ecc71", "#f39c12", "#e74c3c", "#9b59b6", "#1abc9c"};
        return colors[index % colors.length];
    }
}