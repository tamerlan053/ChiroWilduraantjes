package be.pxl.research.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event("TestEvent");
    }

    @Test
    void timeOptions_ShouldBeEmptyInitially() {
        assertTrue(event.getTimeOptions().isEmpty(), "Time options should be empty by default");
    }

    @Test
    void addTimeOption_ShouldIgnoreNull() {
        event.addTimeOption(null);

        assertTrue(event.getTimeOptions().isEmpty());
    }

    @Test
    void addTimeOption_ShouldIgnoreEmptyString() {
        event.addTimeOption("");

        assertTrue(event.getTimeOptions().isEmpty());
    }

    @Test
    void deleteTimeOption_ShouldRemoveIfExists() {
        event.addTimeOption("12:00");

        event.deleteOption("12:00");

        assertFalse(event.getTimeOptions().contains("12:00"));
    }

    @Test
    void deleteTimeOption_ShouldIgnoreNonExistentOption() {
        event.addTimeOption("13:00");

        event.deleteOption("99:99");

        assertEquals(1, event.getTimeOptions().size());
    }

    @Test
    void deleteTimeOption_ShouldIgnoreEmptyString() {
        event.addTimeOption("14:00");

        event.deleteOption("");

        assertEquals(1, event.getTimeOptions().size());
    }

    @Test
    void getLinkedEventName_ShouldReturnValueFromConstructor() {
        assertEquals("TestEvent", event.getLinkedEventName());
    }

    @Test
    void setLinkedEventName_ShouldUpdateValue() {
        event.setLinkedEventName("UpdatedEvent");

        assertEquals("UpdatedEvent", event.getLinkedEventName());
    }
}
