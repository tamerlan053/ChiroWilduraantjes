package be.pxl.research.controller.response;

import be.pxl.research.domain.MenuItem;

import java.time.LocalDateTime;
import java.util.List;

public record KitchenOrderListDTO(
        Long id,
        String familyName,
        List<MenuItem> menuItems,
        String remarks,
        String tableNumber,
        boolean readyToServe,
        LocalDateTime arrivalTime
) {

}
