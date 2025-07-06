package be.pxl.research.controller;

import be.pxl.research.controller.request.*;
import be.pxl.research.controller.response.KitchenOrderListDTO;
import be.pxl.research.controller.response.OrderDto;
import be.pxl.research.controller.response.OrderListDto;
import be.pxl.research.controller.response.TotalsDto;
import be.pxl.research.domain.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import be.pxl.research.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.List;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecurityFilterChain securityFilterChain;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testCreateOrder() throws Exception {
        OrderRequest request = new OrderRequest(
                "test@example.com",
                LocalDateTime.now(),
                "Smith",
                "No salt",
                Collections.emptyList(),
                0,
                0
        );

        Mockito.doNothing().when(orderService).saveOrder(Mockito.any(OrderRequest.class));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // or isNoContent(), depending on how your controller is set up

        // Optionally verify interaction with the service
        Mockito.verify(orderService).saveOrder(Mockito.any(OrderRequest.class));
    }

    @Test
    void getTotalsTest() throws Exception {
        TotalsDto totalsDto1 = new TotalsDto("Mosselen", 16);
        TotalsDto totalsDto2 = new TotalsDto("Frieten", 4);
        List<TotalsDto> totals = List.of(totalsDto1, totalsDto2);

        Mockito.when(orderService.getTotals()).thenReturn(totals);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/totals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(totals)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mosselen"))
                .andExpect(jsonPath("$[0].quantity").value(16))
                .andExpect(jsonPath("$[1].name").value("Frieten"))
                .andExpect(jsonPath("$[1].quantity").value(4));
    }

    @Test
    void getAllOrdersNotInProgressTest() throws Exception {
        Order fakeOrder = Mockito.mock(Order.class);
        Mockito.when(fakeOrder.getId()).thenReturn(1L);
        Mockito.when(fakeOrder.getFamilyName()).thenReturn("Test");
        Mockito.when(fakeOrder.getTableNumber()).thenReturn("5");
        Mockito.when(fakeOrder.isPayed()).thenReturn(false);
        Mockito.when(fakeOrder.getArrivalTime()).thenReturn(LocalDateTime.now());

        OrderListDto orderDto = new OrderListDto(fakeOrder);

        Mockito.when(orderService.getAllOrdersNotInProgress()).thenReturn(List.of(orderDto));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].familyName").value("Test"))
                .andExpect(jsonPath("$[0].tableNumber").value("5"))
                .andExpect(jsonPath("$[0].payed").value(false));
    }

    @Test
    void getAllOrdersInProgressTest() throws Exception {
        KitchenOrderListDTO order = new KitchenOrderListDTO(
                1L,
                "Smith",
                List.of(),
                "No onions",
                "Table 4",
                false,
                LocalDateTime.now()
        );

        Mockito.when(orderService.getAllOrdersInProgress()).thenReturn(List.of(order));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/inProgress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getOrderByIdTest() throws Exception {
        OrderDto dto = new OrderDto("Smith", List.of(), 3, "No peanuts");

        Mockito.when(orderService.getOrderById(1L)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familyName").value("Smith"));
    }

    @Test
    void updateOrderTest() throws Exception {
        UpdateOrderRequest request = new UpdateOrderRequest(Collections.emptyList(), 3);
        Mockito.doNothing().when(orderService).updateOrder(Mockito.any(), Mockito.eq(1L));

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void updateTableNumberTest() throws Exception {
        UpdateTableNumberRequest request = new UpdateTableNumberRequest("7");
        Mockito.doNothing().when(orderService).updateTableNumber(1L, "7");

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tafelnummer is aangepast. Nieuw tafelnummer: 7"));
    }

    @Test
    void payOrderTest() throws Exception {
        Mockito.doNothing().when(orderService).payOrder(1L);

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/pay"))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderInProgressStatusTest() throws Exception {
        UpdateInProgressRequest request = new UpdateInProgressRequest(true);
        Mockito.doNothing().when(orderService).updateInProgressStatus(1L, true);

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/setInProgress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(simpMessagingTemplate).convertAndSend(Mockito.eq("/topic/orders/inProgress"), Mockito.any(Object.class));
    }

    @Test
    void updateReadyToServeStatusTest() throws Exception {
        UpdateReadyToServeRequest request = new UpdateReadyToServeRequest(true);
        Mockito.doNothing().when(orderService).updateReadyToServeStatus(1L, true);

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/serveOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
