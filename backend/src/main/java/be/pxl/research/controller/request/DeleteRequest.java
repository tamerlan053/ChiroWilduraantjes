package be.pxl.research.controller.request;

import jakarta.validation.constraints.NotNull;

public record DeleteRequest (@NotNull String itemName, boolean isChild) {

}
