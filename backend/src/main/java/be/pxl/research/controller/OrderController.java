package be.pxl.research.controller;

import be.pxl.research.controller.request.*;
import be.pxl.research.controller.response.*;
import be.pxl.research.controller.response.OrderDto;
import be.pxl.research.service.OrderService;
import be.pxl.research.controller.socketdtos.OrderInProgressUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private final OrderService orderService;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public OrderController(OrderService orderService, SimpMessagingTemplate messagingTemplate) {
        this.orderService = orderService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderRequest orderRequest) {
        orderService.saveOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/totals")
    public List<TotalsDto> getTotals(){
        return orderService.getTotals();
    }

    @GetMapping
    public List<OrderListDto> getAllOrdersNotInProgress() {
        return orderService.getAllOrdersNotInProgress();
    }

    @GetMapping("/inProgress")
    public List<KitchenOrderListDTO> getAllOrdersInProgress(){
        return orderService.getAllOrdersInProgress();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);  // Get OrderDto directly from the service
        return ResponseEntity.ok(orderDto);
    }

    @PutMapping("/{orderid}")
    public ResponseEntity<Void> updateOrder(@RequestBody UpdateOrderRequest updatedOrder, @PathVariable Long orderid) {
        orderService.updateOrder(updatedOrder, orderid);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/{orderId}/table")
    public ResponseEntity<Map<String, Object>> updateTableNumber(@PathVariable Long orderId, @RequestBody UpdateTableNumberRequest request) {
        orderService.updateTableNumber(orderId, request.tableNumber());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tafelnummer is aangepast. Nieuw tafelnummer: " + request.tableNumber());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/pay")
    public void payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
    }

    @PutMapping("/{orderId}/setInProgress")
    public ResponseEntity<HttpStatus> updateOrderInProgressStatus(@PathVariable Long orderId, @RequestBody UpdateInProgressRequest request){
        orderService.updateInProgressStatus(orderId, request.inProgress());

        OrderInProgressUpdateDTO dto = new OrderInProgressUpdateDTO(orderId, request.inProgress());
        messagingTemplate.convertAndSend("/topic/orders/inProgress", dto);

        return ResponseEntity.ok(HttpStatus.OK);
    }
    @PutMapping("/{orderId}/serveOrder")
    public ResponseEntity<HttpStatus> updateReadyToServeStatus(@PathVariable Long orderId, @RequestBody UpdateReadyToServeRequest request) {
        orderService.updateReadyToServeStatus(orderId, request.readyToServe());
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
