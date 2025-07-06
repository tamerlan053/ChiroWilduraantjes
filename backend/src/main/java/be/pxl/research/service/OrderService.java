package be.pxl.research.service;

import be.pxl.research.controller.request.OrderRequest;
import be.pxl.research.controller.request.UpdateOrderRequest;
import be.pxl.research.controller.response.*;
import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.MenuItem;
import be.pxl.research.domain.Order;
import be.pxl.research.repository.CurrentEventRepository;
import be.pxl.research.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CurrentEventRepository currentEventRepository;

    public OrderService(OrderRepository orderRepository, CurrentEventRepository currentEventRepository) {
        this.orderRepository = orderRepository;
        this.currentEventRepository = currentEventRepository;
    }

    private String getCurrentEventName() {
        return currentEventRepository.findById(1L)
                .map(CurrentEvent::getName)
                .orElse(null);
    }

    public void saveOrder(OrderRequest orderRequest) {
        String eventName = getCurrentEventName();
        if (eventName == null) return;

        Order order = new Order(
                eventName,
                orderRequest.email(),
                orderRequest.arrivalTime(),
                orderRequest.familyName(),
                orderRequest.remarks(),
                orderRequest.drinkTokens()
        );

        if (orderRequest.menuItems() != null) {
            order.setMenuItems(orderRequest.menuItems().stream()
                    .map(menuItemRequest -> new MenuItem(
                            menuItemRequest.name(),
                            menuItemRequest.quantity(),
                            menuItemRequest.price()))
                    .toList());
        }

        orderRepository.save(order);
    }

    public List<TotalsDto> getTotals() {
        String eventName = getCurrentEventName();
        if (eventName == null) return List.of();

        List<Order> orders = orderRepository.getOrdersByEventName(eventName);
        Map<String, Integer> itemTotals = new HashMap<>();

        for (Order order : orders) {
            for (MenuItem item : order.getMenuItems()) {
                itemTotals.merge(item.getName(), item.getQuantity(), Integer::sum);
            }
        }

        return itemTotals.entrySet().stream()
                .map(entry -> new TotalsDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<OrderListDto> getAllOrdersNotInProgress() {
        String eventName = getCurrentEventName();
        if (eventName == null) return List.of();

        return orderRepository.findAll().stream()
                .filter(order -> !order.isInProgress() && eventName.equals(order.getEventName()))
                .map(OrderListDto::new)
                .toList();
    }

    public List<KitchenOrderListDTO> getAllOrdersInProgress() {
        String eventName = getCurrentEventName();
        if (eventName == null) return List.of();

        return orderRepository.findAll().stream()
                .filter(order -> order.isInProgress() && eventName.equals(order.getEventName()))
                .map(order -> new KitchenOrderListDTO(
                        order.getId(),
                        order.getFamilyName(),
                        order.getMenuItems(),
                        order.getRemarks(),
                        order.getTableNumber(),
                        order.isReadyToServe(),
                        order.getArrivalTime()
                ))
                .toList();
    }

    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.getOrderById(orderId);
        return convertToOrderDto(order);
    }

    private OrderDto convertToOrderDto(Order order) {
        List<MenuItemDto> menuItemDtos = order.getMenuItems().stream()
                .map(menuItem -> new MenuItemDto(menuItem.getName(), menuItem.getQuantity(), menuItem.getPrice()))
                .toList();

        return new OrderDto(order.getFamilyName(), menuItemDtos, order.getDrinkTokens(), order.getRemarks());
    }

    public void updateOrder(UpdateOrderRequest updatedOrder, Long orderId) {
        Order oldOrder = orderRepository.getOrderById(orderId);
        oldOrder.getMenuItems().clear();

        if (updatedOrder.menuItems() != null) {
            List<MenuItem> updatedItems = updatedOrder.menuItems().stream()
                    .map(req -> new MenuItem(req.name(), req.quantity(), req.price()))
                    .toList();
            oldOrder.setMenuItems(updatedItems);
        }

        oldOrder.setDrinkTokens(updatedOrder.drinkTokens());
        orderRepository.save(oldOrder);
    }

    public void updateTableNumber(Long orderId, String tableNumber) {
        Order order = orderRepository.getOrderById(orderId);
        order.setTableNumber(tableNumber);
        orderRepository.save(order);
    }

    public void payOrder(Long orderId) {
        Order order = orderRepository.getOrderById(orderId);
        order.setPayed(true);
        order.setArrivalTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    public void updateInProgressStatus(Long orderId, boolean inProgress) {
        Order order = orderRepository.getOrderById(orderId);
        order.setInProgress(inProgress);
        order.setArrivalTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    public void updateReadyToServeStatus(Long orderId, boolean readyToServe) {
        Order order = orderRepository.getOrderById(orderId);
        order.setReadyToServe(readyToServe);
        orderRepository.save(order);
    }
}
