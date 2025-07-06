package be.pxl.research.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemOnMenuRequest (
        @NotNull(message = "Menu item name is required") String name,
        @NotNull(message = "Menu Item price is required") @Min(value = 0, message = "Price should be at leaast 0") double price,
        boolean isChildFood
) {

}
