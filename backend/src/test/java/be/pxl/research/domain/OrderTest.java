package be.pxl.research.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private LocalDateTime arrivalTime;

    @BeforeEach
    void setUp() {
        arrivalTime = LocalDateTime.now();
        order = new Order("Test Event", "test@example.com", arrivalTime, "Doe", "No remarks");
    }

    @Test
    void constructor_InitializesFieldsCorrectly() {
        assertEquals("Test Event", order.getEventName());
        assertEquals("test@example.com", order.getEmail());
        assertEquals(arrivalTime, order.getArrivalTime());
        assertEquals("Doe", order.getFamilyName());
        assertEquals("No remarks", order.getRemarks());
        assertEquals(0, order.getDrinkTokens());
    }

    @Test
    void constructor_WithDrinkTokens_SetsValueCorrectly() {
        Order customOrder = new Order("Test Event", "test@example.com", arrivalTime, "Doe", "Some remarks", 5);
        assertEquals(5, customOrder.getDrinkTokens());
    }

    @Test
    void defaultFlags_ShouldBeFalseOrEmpty() {
        assertFalse(order.isInProgress());
        assertFalse(order.isReadyToServe());
        assertFalse(order.isPayed());
        assertEquals("", order.getTableNumber());
    }

    @Test
    void setAndGet_Payed_WorksCorrectly() {
        order.setPayed(true);
        assertTrue(order.isPayed());
    }

    @Test
    void setAndGet_InProgress_WorksCorrectly() {
        order.setInProgress(true);
        assertTrue(order.isInProgress());
    }

    @Test
    void setAndGet_ReadyToServe_WorksCorrectly() {
        order.setReadyToServe(true);
        assertTrue(order.isReadyToServe());
    }

    @Test
    void setAndGet_TableNumber_WorksCorrectly() {
        order.setTableNumber("A12");
        assertEquals("A12", order.getTableNumber());
    }

    @Test
    void setAndGet_MenuItems_WorksCorrectly() {
        MenuItem item = new MenuItem();
        order.setMenuItems(Collections.singletonList(item));
        assertEquals(1, order.getMenuItems().size());
        assertSame(item, order.getMenuItems().get(0));
    }

    @Test
    void setAndGet_DrinkTokens_WorksCorrectly() {
        order.setDrinkTokens(10);
        assertEquals(10, order.getDrinkTokens());
    }

    @Test
    void setAndGet_Remarks_WorksCorrectly() {
        order.setRemarks("New remarks");
        assertEquals("New remarks", order.getRemarks());
    }
}
