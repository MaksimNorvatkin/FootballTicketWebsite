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
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    //private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Создаём стадионы
        Stadium anfield = createStadium("Anfield Road", "Liverpool", 53394, "/images/stadiums/anfield.jpg");
        Stadium emirates = createStadium("Emirates Stadium", "London", 60704, "/images/stadiums/emirates.jpg");
        Stadium oldTrafford = createStadium("Old Trafford", "Manchester", 74879, "/images/stadiums/old-trafford.jpg");

        // 2. Создаём команды
        Team liverpool = createTeam("Liverpool", "Liverpool", anfield);
        Team manCity = createTeam("Manchester City", "Manchester", null);
        Team arsenal = createTeam("Arsenal", "London", emirates);
        Team chelsea = createTeam("Chelsea", "London", null);
        Team manUnited = createTeam("Manchester United", "Manchester", oldTrafford);
        Team tottenham = createTeam("Tottenham", "London", null);

        // 3. Создаём матчи
        Match match1 = createMatch(liverpool, manCity, anfield, LocalDateTime.now().plusDays(7), 100);
        Match match2 = createMatch(arsenal, chelsea, emirates, LocalDateTime.now().plusDays(14), 95);
        Match match3 = createMatch(manUnited, tottenham, oldTrafford, LocalDateTime.now().plusDays(21), 88);


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
        // Создаем 6 секторов и билеты для каждого матча
        String[] sectors = {"North", "NorthEast", "East", "West", "South", "SouthWest"};
        Double[] multipliers = {1.0, 1.5, 0.8, 0.8, 1.0, 1.5};

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

    private Match createMatch(Team home, Team away, Stadium stadium, LocalDateTime date, int popularity) {
        Match match = new Match();
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setStadium(stadium);
        match.setMatchDateTime(date);
        match.setDescription(home.getName() + " vs " + away.getName() + " - " + stadium.getName());
        match.setPopularityScore(popularity);
        return matchRepository.save(match);
    }

    private Stadium createStadium(String name, String city, int capacity, String imageUrl) {
        Stadium stadium = new Stadium();
        stadium.setName(name);
        stadium.setCity(city);
        stadium.setCapacity(capacity);
        stadium.setImageUrl(imageUrl);
        return stadiumRepository.save(stadium);
    }

    private Team createTeam(String name, String city, Stadium stadium) {
        Team team = new Team();
        team.setName(name);
        team.setCity(city);
        team.setStadium(stadium);
        return teamRepository.save(team);
    }

    private String getColorForSector(int index) {
        String[] colors = {
                "#e74c3c",  // North - красный
                "#e67e22",  // NorthEast - оранжевый
                "#f1c40f",  // East - жёлтый
                "#2ecc71",  // West - зелёный
                "#3498db",  // South - синий
                "#9b59b6"   // SouthWest - фиолетовый
        };
        return colors[index % colors.length];
    }
}