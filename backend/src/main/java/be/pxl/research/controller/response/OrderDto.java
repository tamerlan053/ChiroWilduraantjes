package be.pxl.research.controller.response;

import java.util.List;

public record OrderDto(String familyName, List<MenuItemDto> menuItems, int drinkTokens, String remarks) {

}
