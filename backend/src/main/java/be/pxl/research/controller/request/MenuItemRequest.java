package be.pxl.research.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MenuItemRequest(
        @NotNull(message = "Menu item name is required") String name,
        @Min(value = 0, message = "Quantity should be at least 0") int quantity,
        @NotNull(message = "Menu Item price is required") @Min(value = 0, message = "Price should be at least 0") double price
) {

}
