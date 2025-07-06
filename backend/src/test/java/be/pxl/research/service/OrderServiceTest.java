package be.pxl.research.service;

import be.pxl.research.controller.request.MenuItemRequest;
import be.pxl.research.controller.request.OrderRequest;
import be.pxl.research.controller.request.UpdateOrderRequest;
import be.pxl.research.controller.response.TotalsDto;
import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.MenuItem;
import be.pxl.research.domain.Order;
import be.pxl.research.repository.CurrentEventRepository;
import be.pxl.research.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CurrentEventRepository currentEventRepository;

    @InjectMocks
    private OrderService orderService;

    private final String CURRENT_EVENT_NAME = "Test";

    @BeforeEach
    void init() {
        lenient().when(currentEventRepository.findById(1L))
                .thenReturn(Optional.of(new CurrentEvent(CURRENT_EVENT_NAME)));
    }

    @Test
    void testSaveOrderSuccessfully() {
        OrderRequest orderRequest = new OrderRequest(
                "test@example.com",
                LocalDateTime.now(),
                "Smith",
                "No salt",
                List.of(
                        new MenuItemRequest("Pizza", 2, 15.0),
                        new MenuItemRequest("Soda", 3, 2.5)
                ),
                5,
                0
        );

        orderService.saveOrder(orderRequest);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order saved = captor.getValue();
        assertThat(saved.getEventName()).isEqualTo(CURRENT_EVENT_NAME);
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getFamilyName()).isEqualTo("Smith");
        assertThat(saved.getMenuItems()).hasSize(2);
    }

    @Test
    void testSaveOrderWithNoMenuItems() {
        OrderRequest request = new OrderRequest(
                "user@example.com",
                LocalDateTime.now(),
                "Johnson",
                "No onions",
                null,
                0,
                0
        );

        orderService.saveOrder(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order saved = captor.getValue();
        assertThat(saved.getMenuItems()).isNullOrEmpty();
    }

    @Test
    void getOrderTotalsTest() {
        List<Order> orders = getOrders();
        when(orderRepository.getOrdersByEventName(CURRENT_EVENT_NAME)).thenReturn(orders);

        List<TotalsDto> totals = orderService.getTotals();
        assertThat(totals)
                .isNotNull()
                .anySatisfy(t -> {
                    assertThat(t.name()).isEqualTo("Mosselen");
                    assertThat(t.quantity()).isEqualTo(16);
                })
                .anySatisfy(t -> {
                    assertThat(t.name()).isEqualTo("Frieten");
                    assertThat(t.quantity()).isEqualTo(4);
                });

    }

    private List<Order> getOrders() {
        MenuItem item1 = new MenuItem("Mosselen", 10);
        MenuItem item2 = new MenuItem("Mosselen", 6);
        MenuItem item3 = new MenuItem("Frieten", 4);

        Order o1 = new Order(CURRENT_EVENT_NAME, "a@a.be", LocalDateTime.now(), "Family1", "None");
        Order o2 = new Order(CURRENT_EVENT_NAME, "b@b.be", LocalDateTime.now(), "Family2", "None");
        o1.setMenuItems(List.of(item1, item3));
        o2.setMenuItems(List.of(item2));

        return List.of(o1, o2);
    }

    @Test
    void testGetAllOrdersNotInProgress() {
        Order o1 = new Order(CURRENT_EVENT_NAME, "a@a.be", LocalDateTime.now(), "Family1", "");
        Order o2 = new Order(CURRENT_EVENT_NAME, "b@b.be", LocalDateTime.now(), "Family2", "");
        o1.setInProgress(false);
        o2.setInProgress(true);
        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));

        var result = orderService.getAllOrdersNotInProgress();
        assertThat(result).hasSize(1);
    }

    @Test
    void testGetAllOrdersInProgress() {
        Order o = new Order(CURRENT_EVENT_NAME, "a@a.be", LocalDateTime.now(), "Doe", "Allergic to nuts");
        o.setInProgress(true);
        o.setTableNumber("T2");
        o.setMenuItems(List.of(new MenuItem("Fish", 1, 11.0)));

        when(orderRepository.findAll()).thenReturn(List.of(o));

        var result = orderService.getAllOrdersInProgress();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).tableNumber()).isEqualTo("T2");
    }

    @Test
    void testUpdateOrder() {
        Order o = new Order(CURRENT_EVENT_NAME, "x@y.com", LocalDateTime.now(), "Doe", "Initial");
        o.setMenuItems(new ArrayList<>(List.of(new MenuItem("Old", 1, 5.0))));

        UpdateOrderRequest req = new UpdateOrderRequest(
                List.of(new MenuItemRequest("New", 2, 7.5)), 3
        );

        when(orderRepository.getOrderById(1L)).thenReturn(o);

        orderService.updateOrder(req, 1L);

        verify(orderRepository).save(o);
        assertThat(o.getMenuItems()).hasSize(1);
        assertThat(o.getDrinkTokens()).isEqualTo(3);
    }

    @Test
    void testUpdateTableNumber() {
        Order o = new Order(CURRENT_EVENT_NAME, "x@y.com", LocalDateTime.now(), "Family", "");
        when(orderRepository.getOrderById(1L)).thenReturn(o);

        orderService.updateTableNumber(1L, "B5");

        assertThat(o.getTableNumber()).isEqualTo("B5");
    }

    @Test
    void testPayOrder() {
        Order o = new Order(CURRENT_EVENT_NAME, "x@y.com", LocalDateTime.now(), "Family", "");
        when(orderRepository.getOrderById(1L)).thenReturn(o);

        orderService.payOrder(1L);

        assertTrue(o.isPayed());
        assertThat(o.getArrivalTime()).isNotNull();
    }

    @Test
    void testUpdateInProgressStatus() {
        Order o = new Order(CURRENT_EVENT_NAME, "x@y.com", LocalDateTime.now(), "Family", "");
        when(orderRepository.getOrderById(1L)).thenReturn(o);

        orderService.updateInProgressStatus(1L, true);

        assertTrue(o.isInProgress());
    }

    @Test
    void testUpdateReadyToServeStatus() {
        Order o = new Order(CURRENT_EVENT_NAME, "x@y.com", LocalDateTime.now(), "Family", "");
        when(orderRepository.getOrderById(1L)).thenReturn(o);

        orderService.updateReadyToServeStatus(1L, true);

        assertTrue(o.isReadyToServe());
    }
}
