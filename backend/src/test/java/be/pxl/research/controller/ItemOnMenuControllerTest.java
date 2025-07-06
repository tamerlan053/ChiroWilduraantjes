package be.pxl.research.controller;

import be.pxl.research.controller.request.DeleteRequest;
import be.pxl.research.controller.request.ItemOnMenuRequest;
import be.pxl.research.controller.response.ItemOnMenuDto;
import be.pxl.research.service.ItemOnMenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemOnMenuControllerTest {

    @Mock
    private ItemOnMenuService menuItemService;

    @InjectMocks
    private ItemOnMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdd() {
        ItemOnMenuRequest request = new ItemOnMenuRequest("Burger", 9.99, true);
        doNothing().when(menuItemService).addMenuItem(request);

        ResponseEntity<Void> response = controller.add(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(menuItemService, times(1)).addMenuItem(request);
    }

    @Test
    void testDelete() {
        DeleteRequest request = new DeleteRequest("Burger", true);
        doNothing().when(menuItemService).deleteMenuItem("Burger", true);

        ResponseEntity<Void> response = controller.delete(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(menuItemService, times(1)).deleteMenuItem("Burger", true);
    }

    @Test
    void testGetAllChildMenuItems() {
        List<ItemOnMenuDto> expectedItems = Arrays.asList(
                new ItemOnMenuDto("Kid's Burger", 5.99, true),
                new ItemOnMenuDto("Kid's Pasta", 4.99, true)
        );
        when(menuItemService.getAllChildMenuItems()).thenReturn(expectedItems);

        List<ItemOnMenuDto> result = controller.getAllChildMenuItems();

        assertEquals(expectedItems, result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isChildFood());
        assertTrue(result.get(1).isChildFood());
        verify(menuItemService, times(1)).getAllChildMenuItems();
    }

    @Test
    void testGetAllAdultMenuItem() {
        List<ItemOnMenuDto> expectedItems = Arrays.asList(
                new ItemOnMenuDto("Burger", 9.99, false),
                new ItemOnMenuDto("Pizza", 12.99, false),
                new ItemOnMenuDto("Steak", 15.99, false)
        );
        when(menuItemService.getAllAdultMenuItems()).thenReturn(expectedItems);

        List<ItemOnMenuDto> result = controller.getAllAdultMenuItem();

        assertEquals(expectedItems, result);
        assertEquals(3, result.size());
        assertFalse(result.get(0).isChildFood());
        assertFalse(result.get(1).isChildFood());
        assertFalse(result.get(2).isChildFood());
        verify(menuItemService, times(1)).getAllAdultMenuItems();
    }

    @Test
    void testGetAllChildMenuItems_EmptyList() {
        List<ItemOnMenuDto> expectedItems = List.of();
        when(menuItemService.getAllChildMenuItems()).thenReturn(expectedItems);

        List<ItemOnMenuDto> result = controller.getAllChildMenuItems();

        assertTrue(result.isEmpty());
        verify(menuItemService, times(1)).getAllChildMenuItems();
    }

    @Test
    void testGetAllAdultMenuItem_EmptyList() {
        List<ItemOnMenuDto> expectedItems = List.of();
        when(menuItemService.getAllAdultMenuItems()).thenReturn(expectedItems);

        List<ItemOnMenuDto> result = controller.getAllAdultMenuItem();

        assertTrue(result.isEmpty());
        verify(menuItemService, times(1)).getAllAdultMenuItems();
    }
}
