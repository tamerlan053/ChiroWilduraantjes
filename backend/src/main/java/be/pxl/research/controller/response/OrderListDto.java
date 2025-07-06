package be.pxl.research.controller.response;

import be.pxl.research.domain.Order;

import java.time.LocalDateTime;

public class OrderListDto {
    private final Long id;
    private final String familyName;
    private final String tableNumber;
    private final LocalDateTime arrivalTime;
    private final boolean isPayed;

    public OrderListDto(Order order) {
        this.id = order.getId();
        this.familyName = order.getFamilyName();
        this.tableNumber = order.getTableNumber();
        this.isPayed = order.isPayed();
        this.arrivalTime = order.getArrivalTime();
    }

    public Long getId() {
        return id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }
}
