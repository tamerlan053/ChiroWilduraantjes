package be.pxl.research.controller.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record OrderRequest(
        @NotNull(message = "Email is required") String email,
        @NotNull(message = "Arrival time is required") LocalDateTime arrivalTime,
        @NotNull(message = "Family name is required") String familyName,
        String remarks,
        List<MenuItemRequest> menuItems,
        int drinkTokens,
        int isPayed
) {

}
