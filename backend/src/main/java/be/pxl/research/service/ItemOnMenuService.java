package be.pxl.research.service;

import be.pxl.research.controller.response.ItemOnMenuDto;
import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.ItemOnMenu;
import be.pxl.research.repository.CurrentEventRepository;
import be.pxl.research.repository.ItemOnMenuRepository;
import be.pxl.research.controller.request.ItemOnMenuRequest;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class ItemOnMenuService {

    private final ItemOnMenuRepository menuItemRepository;
    private final CurrentEventRepository currentEventRepository;

    public ItemOnMenuService(ItemOnMenuRepository menuItemRepository,
                             CurrentEventRepository currentEventRepository) {
        this.menuItemRepository = menuItemRepository;
        this.currentEventRepository = currentEventRepository;
    }

    private String getCurrentEventName() {
        return currentEventRepository.findById(1L)
                .map(CurrentEvent::getName)
                .orElse(null);
    }

    public void addMenuItem(ItemOnMenuRequest itemOnMenuRequest) {
        String eventName = getCurrentEventName();
        if (eventName == null) return;

        ItemOnMenu itemOnMenu = new ItemOnMenu(
                itemOnMenuRequest.name(),
                itemOnMenuRequest.price(),
                itemOnMenuRequest.isChildFood(),
                eventName
        );

        menuItemRepository.save(itemOnMenu);
    }

    public List<ItemOnMenuDto> getAllChildMenuItems() {
        String eventName = getCurrentEventName();
        if (eventName == null) return List.of();

        return menuItemRepository.findAll().stream()
                .filter(item -> item.isChildFood() && item.getEventName().equals(eventName))
                .map(item -> new ItemOnMenuDto(item.getName(), item.getPrice(), item.isChildFood()))
                .toList();
    }

    public List<ItemOnMenuDto> getAllAdultMenuItems() {
        String eventName = getCurrentEventName();
        if (eventName == null) return List.of();

        return menuItemRepository.findAll().stream()
                .filter(item -> !item.isChildFood() && item.getEventName().equals(eventName))
                .map(item -> new ItemOnMenuDto(item.getName(), item.getPrice(), item.isChildFood()))
                .toList();
    }

    public void deleteMenuItem(String foodItem, boolean isChildFood) {
        String eventName = getCurrentEventName();
        if (eventName == null) return;

        List<ItemOnMenu> currentMenuItems = menuItemRepository.findItemOnMenuByEventName(eventName);

        if (currentMenuItems != null) {
            Iterator<ItemOnMenu> iterator = currentMenuItems.iterator();
            while (iterator.hasNext()) {
                ItemOnMenu item = iterator.next();
                if (item.getName().equals(foodItem) && item.isChildFood() == isChildFood) {
                    menuItemRepository.delete(item);
                    iterator.remove();
                }
            }
        }
    }
}
