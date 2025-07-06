package be.pxl.research;
import be.pxl.research.domain.*;
import be.pxl.research.repository.*;
import be.pxl.research.domain.Event;
import be.pxl.research.domain.MenuItem;
import be.pxl.research.repository.EventRepository;
import be.pxl.research.repository.ItemOnMenuRepository;
import be.pxl.research.repository.OrderRepository;
import be.pxl.research.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component

public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final ItemOnMenuRepository itemOnMenuRepository;
    private final EventRepository eventRepository;
    private final CurrentEventRepository currentEventRepository;

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());
    private static final String FRIETEN = "Friejtes";
    private static final String MOSSELENNATUUR = "Mosselen Natuur";

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, OrderRepository orderRepository, ItemOnMenuRepository itemOnMenuRepository, EventRepository eventRepository, CurrentEventRepository currentEventRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
        this.itemOnMenuRepository = itemOnMenuRepository;
        this.eventRepository = eventRepository;
        this.currentEventRepository = currentEventRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String eventName = "Mosselsouper 2025";

        if (currentEventRepository.count() == 0) {
            currentEventRepository.save(new CurrentEvent(eventName));
        }

        if (userRepository.count() == 0) {
            createPreMadeUsers();
        }
        if(itemOnMenuRepository.count() == 0) {
            createPreMadeItemsOnMenu(eventName);
        }
        if(orderRepository.count() == 0) {
            createPreMadeOrders(eventName);
        }
    }

    private void createPreMadeUsers() {
        User kitchenUser = new User();
        kitchenUser.setUsername("kitchen_user");
        kitchenUser.setPassword(passwordEncoder.encode("kitchen123"));
        kitchenUser.setRoles(Set.of(Roles.KITCHEN));

        User hallUser = new User();
        hallUser.setUsername("hall_user");
        hallUser.setPassword(passwordEncoder.encode("hall123"));
        hallUser.setRoles(Set.of(Roles.HALL));

        User cashierUser = new User();
        cashierUser.setUsername("cashier_user");
        cashierUser.setPassword(passwordEncoder.encode("cashier123"));
        cashierUser.setRoles(Set.of(Roles.CASHIER));

        User adminUser = new User();
        adminUser.setUsername("admin_user");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(Set.of(Roles.ADMIN, Roles.KITCHEN, Roles.HALL, Roles.CASHIER)); // Admin has all roles

        List<User> users = List.of(kitchenUser, hallUser, cashierUser, adminUser);
        userRepository.saveAll(users);

        logger.info("✅ Pre-made users have been added to the database!");

    }

    private void createPreMadeOrders(String eventName){
        List<String> seedTimeOptions = List.of("zondag 18:00",
                "maandag 18:00",
                "donderdag 12:20",
                "vrijdag 13:50");

        Event seedEvent = new Event(seedTimeOptions, eventName);
        eventRepository.save(seedEvent);

        MenuItem item1 = new MenuItem(FRIETEN, 4);
        MenuItem item2 = new MenuItem(MOSSELENNATUUR, 4);
        MenuItem item3 = new MenuItem("Balletjes in Tomatensaus", 2);
        MenuItem item4 = new MenuItem("Mosselen Look", 4);
        MenuItem item5 = new MenuItem("Frieten", 4);
        MenuItem item6 = new MenuItem("Balletjes in Luikse Saus", 2);
        MenuItem item7 = new MenuItem("Kroketten", 4);
        MenuItem item8 = new MenuItem("Koninginnenhapje", 2);
        MenuItem item9 = new MenuItem(FRIETEN, 5);
        MenuItem item10 = new MenuItem(MOSSELENNATUUR, 5);
        MenuItem item11 = new MenuItem("Balletjes in Tomatensaus", 2);
        MenuItem item12 = new MenuItem(FRIETEN, 3);
        MenuItem item13 = new MenuItem(MOSSELENNATUUR, 1);

        List<MenuItem> order1MenuItems = List.of(item1, item2, item3);
        List<MenuItem> order2MenuItems = List.of(item4, item5);
        List<MenuItem> order3MenuItems = List.of(item6, item7, item8);
        List<MenuItem> order4MenuItems = List.of(item9, item10);
        List<MenuItem> order5MenuItems = List.of(item11, item12, item13);

        Order order1 = new Order(
                eventName,
                "melon@example.com",
                LocalDateTime.now().plusDays(2),
                "Melon",
                "Vegetarische optie voor 1 volwassene",
                8
        );
        order1.setMenuItems(order1MenuItems);
        order1.setArrivalTime(LocalDateTime.now().plusMinutes(23));

        Order order2 = new Order(
                eventName,
                "jacques@example.com",
                LocalDateTime.now().plusDays(4),
                "Jacques",
                "",
                3
        );
        order2.setMenuItems(order2MenuItems);
        order2.setArrivalTime(LocalDateTime.now().plusMinutes(-50));
        Order order3 = new Order(
                eventName,
                "aslakhanov@example.com",
                LocalDateTime.now().plusDays(5),
                "Aslakhanov",
                "1 kind heeft een notenallergie",
                10
        );
        order3.setMenuItems(order3MenuItems);
        order3.setArrivalTime(LocalDateTime.now().plusMinutes(8));
        Order order4 = new Order(
                eventName,
                "groven@example.com",
                LocalDateTime.now().plusDays(1),
                "Groven",
                "",
                6
        );
        order4.setMenuItems(order4MenuItems);
        order4.setArrivalTime(LocalDateTime.now().plusMinutes(44));

        Order order5 = new Order(
                eventName,
                "vervloessem@example.com",
                LocalDateTime.now().plusDays(3),
                "Vervloessem",
                "Hoge stoel nodig voor een kind",
                12

        );
        order5.setMenuItems(order5MenuItems);

        order1.setPayed(true);
        order2.setPayed(true);
        order3.setPayed(true);
        order4.setPayed(true);
        order5.setPayed(true);
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);
        orderRepository.save(order5);
        logger.info("✅ Pre-made orders have been added to the database!");
    }
    private void createPreMadeItemsOnMenu(String eventName) {

        ItemOnMenu item1 = new ItemOnMenu(
                MOSSELENNATUUR,
                25.50,
                false,
                eventName
        );

        ItemOnMenu item2 = new ItemOnMenu(
                FRIETEN,
                4.50,
                true,
                eventName
        );
        List<ItemOnMenu> itemsOnMenu = List.of(item1, item2);
        itemOnMenuRepository.saveAll(itemsOnMenu);
        logger.info("✅ Pre-made items on menu have been added to the database!");
    }
}
