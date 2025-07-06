package be.pxl.research.controller.request;

import java.util.List;

public record UpdateOrderRequest(
        List<MenuItemRequest> menuItems,
        int drinkTokens
) {

}
