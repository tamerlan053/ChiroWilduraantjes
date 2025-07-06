package be.pxl.research.controller.socketdtos;

public record OrderInProgressUpdateDTO(Long orderId, boolean inProgress) {
}
