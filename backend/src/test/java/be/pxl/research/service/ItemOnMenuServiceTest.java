package be.pxl.research.service;

import be.pxl.research.controller.request.ItemOnMenuRequest;
import be.pxl.research.controller.response.ItemOnMenuDto;
import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.ItemOnMenu;
import be.pxl.research.repository.CurrentEventRepository;
import be.pxl.research.repository.ItemOnMenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemOnMenuServiceTest {

    @Mock
    private ItemOnMenuRepository menuItemRepository;

    @Mock
    private CurrentEventRepository currentEventRepository;

    @InjectMocks
    private ItemOnMenuService itemOnMenuService;

    private final String EVENT_NAME = "Test Event";

    @BeforeEach
    void setup() {
        when(currentEventRepository.findById(1L))
                .thenReturn(Optional.of(new CurrentEvent(EVENT_NAME)));
    }

    @Test
    void addMenuItem_ShouldSaveItemWithCorrectValues() {
        ItemOnMenuRequest request = new ItemOnMenuRequest("Fries", 3.5, true);

        itemOnMenuService.addMenuItem(request);

        verify(menuItemRepository).save(argThat(item ->
                item.getName().equals("Fries") &&
                        item.getPrice() == 3.5 &&
                        item.isChildFood() &&
                        item.getEventName().equals(EVENT_NAME)
        ));
    }

    @Test
    void addMenuItem_ShouldDoNothingWhenNoCurrentEvent() {
        when(currentEventRepository.findById(1L)).thenReturn(Optional.empty());
        ItemOnMenuRequest request = new ItemOnMenuRequest("Burger", 5.0, false);

        itemOnMenuService.addMenuItem(request);

        verify(menuItemRepository, never()).save(any());
    }

    @Test
    void getAllChildMenuItems_ShouldReturnOnlyChildItemsForCurrentEvent() {
        List<ItemOnMenu> allItems = List.of(
                new ItemOnMenu("Child Pizza", 4.5, true, EVENT_NAME),
                new ItemOnMenu("Adult Pizza", 8.5, false, EVENT_NAME),
                new ItemOnMenu("Wrong Event Item", 4.5, true, "Other Event")
        );
        when(menuItemRepository.findAll()).thenReturn(allItems);

        List<ItemOnMenuDto> result = itemOnMenuService.getAllChildMenuItems();

        assertEquals(1, result.size());
        assertEquals("Child Pizza", result.get(0).name());
    }

    @Test
    void getAllAdultMenuItems_ShouldReturnOnlyAdultItemsForCurrentEvent() {
        List<ItemOnMenu> allItems = List.of(
                new ItemOnMenu("Child Pizza", 4.5, true, EVENT_NAME),
                new ItemOnMenu("Adult Pizza", 8.5, false, EVENT_NAME),
                new ItemOnMenu("Wrong Event Item", 8.5, false, "Other Event")
        );
        when(menuItemRepository.findAll()).thenReturn(allItems);

        List<ItemOnMenuDto> result = itemOnMenuService.getAllAdultMenuItems();

        assertEquals(1, result.size());
        assertEquals("Adult Pizza", result.get(0).name());
    }

    @Test
    void deleteMenuItem_ShouldDeleteMatchingItem() {
        ItemOnMenu item = new ItemOnMenu("Fries", 3.5, true, EVENT_NAME);
        when(menuItemRepository.findItemOnMenuByEventName(EVENT_NAME))
                .thenReturn(new ArrayList<>(List.of(item)));

        itemOnMenuService.deleteMenuItem("Fries", true);

        verify(menuItemRepository).delete(item);
    }

    @Test
    void deleteMenuItem_ShouldNotDeleteIfNoMatchByName() {
        ItemOnMenu item = new ItemOnMenu("Burger", 5.0, true, EVENT_NAME);
        when(menuItemRepository.findItemOnMenuByEventName(EVENT_NAME))
                .thenReturn(new ArrayList<>(List.of(item)));

        itemOnMenuService.deleteMenuItem("Fries", true);

        verify(menuItemRepository, never()).delete(any());
    }

    @Test
    void deleteMenuItem_ShouldNotDeleteIfNoMatchByType() {
        ItemOnMenu item = new ItemOnMenu("Fries", 3.5, false, EVENT_NAME);
        when(menuItemRepository.findItemOnMenuByEventName(EVENT_NAME))
                .thenReturn(new ArrayList<>(List.of(item)));

        itemOnMenuService.deleteMenuItem("Fries", true);

        verify(menuItemRepository, never()).delete(any());
    }

    @Test
    void deleteMenuItem_ShouldDoNothingWhenNoItems() {
        when(menuItemRepository.findItemOnMenuByEventName(EVENT_NAME))
                .thenReturn(null);

        itemOnMenuService.deleteMenuItem("Fries", true);

        verify(menuItemRepository, never()).delete(any());
    }

    @Test
    void deleteMenuItem_ShouldDoNothingWhenCurrentEventIsNull() {
        when(currentEventRepository.findById(1L)).thenReturn(Optional.empty());

        itemOnMenuService.deleteMenuItem("Fries", true);

        verify(menuItemRepository, never()).delete(any());
    }
}
