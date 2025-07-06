package be.pxl.research;

import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.Event;
import be.pxl.research.domain.Order;
import be.pxl.research.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

class DataInitializerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OrderRepository orderRepository;
    private ItemOnMenuRepository itemOnMenuRepository;
    private EventRepository eventRepository;
    private CurrentEventRepository currentEventRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        orderRepository = mock(OrderRepository.class);
        itemOnMenuRepository = mock(ItemOnMenuRepository.class);
        eventRepository = mock(EventRepository.class);
        currentEventRepository = mock(CurrentEventRepository.class);

        dataInitializer = new DataInitializer(
                userRepository,
                passwordEncoder,
                orderRepository,
                itemOnMenuRepository,
                eventRepository,
                currentEventRepository
        );
    }

    @Test
    void run_allRepositoriesEmpty_createsData() throws Exception {
        when(currentEventRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(itemOnMenuRepository.count()).thenReturn(0L);
        when(orderRepository.count()).thenReturn(0L);

        dataInitializer.run();

        verify(currentEventRepository).save(any(CurrentEvent.class));
        verify(userRepository).saveAll(anyList());
        verify(itemOnMenuRepository).saveAll(anyList());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void run_ifUsersAndOrdersAndItemsExist_doesNotCreateThem() throws Exception {
        when(currentEventRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(1L);
        when(itemOnMenuRepository.count()).thenReturn(1L);
        when(orderRepository.count()).thenReturn(1L);

        dataInitializer.run();

        verify(currentEventRepository, never()).save(any());
        verify(userRepository, never()).saveAll(anyList());
        verify(itemOnMenuRepository, never()).saveAll(anyList());
        verify(orderRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }
}
