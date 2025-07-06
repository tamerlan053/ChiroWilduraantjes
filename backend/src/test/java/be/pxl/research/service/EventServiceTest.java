package be.pxl.research.service;

import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.Event;
import be.pxl.research.domain.ItemOnMenu;
import be.pxl.research.repository.CurrentEventRepository;
import be.pxl.research.repository.EventRepository;
import be.pxl.research.repository.ItemOnMenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ItemOnMenuRepository itemOnMenuRepository;

    @Mock
    private CurrentEventRepository currentEventRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void changeCurrentEvent_createsEventIfNotExists() {
        String newEventName = "Test Event";
        when(eventRepository.existsEventByLinkedEventName(newEventName)).thenReturn(false);

        eventService.changeCurrentEvent(newEventName);

        verify(currentEventRepository).save(any(CurrentEvent.class));
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getCurrentEventName_returnsNameIfExists() {
        when(currentEventRepository.findById(1L)).thenReturn(Optional.of(new CurrentEvent("Test Event")));

        String result = eventService.getCurrentEventName();

        assertEquals("Test Event", result);
    }

    @Test
    void getCurrentEventName_returnsNullIfNotExists() {
        when(currentEventRepository.findById(1L)).thenReturn(Optional.empty());

        assertNull(eventService.getCurrentEventName());
    }

    @Test
    void getTimeOptions_returnsListIfEventFound() {
        String eventName = "Event 1";
        when(currentEventRepository.findById(1L)).thenReturn(Optional.of(new CurrentEvent(eventName)));
        Event event = new Event(List.of("10:00", "12:00"), eventName);
        when(eventRepository.findEventByLinkedEventName(eventName)).thenReturn(event);

        List<String> options = eventService.getTimeOptions();

        assertEquals(2, options.size());
    }

    @Test
    void addTimeOption_createsNewEventIfNotFound() {
        String eventName = "Event 2";
        String time = "14:00";
        when(currentEventRepository.findById(1L)).thenReturn(Optional.of(new CurrentEvent(eventName)));
        when(eventRepository.findEventByLinkedEventName(eventName)).thenReturn(null);

        eventService.addTimeOption(time);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void deleteOption_removesTimeOptionIfExists() {
        String eventName = "Event 3";
        Event event = new Event(new ArrayList<>(List.of("10:00", "12:00")), eventName);
        when(currentEventRepository.findById(1L)).thenReturn(Optional.of(new CurrentEvent(eventName)));
        when(eventRepository.findEventByLinkedEventName(eventName)).thenReturn(event);

        eventService.deleteOption("10:00");

        assertFalse(event.getTimeOptions().contains("10:00"));
        verify(eventRepository).save(event);
    }

    @Test
    void importItemPreviousEvent_doesNothingIfSameEventName() {
        String name = "Same Event";
        when(currentEventRepository.findById(1L)).thenReturn(Optional.of(new CurrentEvent(name)));

        eventService.importItemPreviousEvent(name);

        verify(itemOnMenuRepository, never()).findItemOnMenuByEventName(any());
    }

    @Test
    void importItemPreviousEvent_copiesItemsCorrectly() {
        String oldEvent = "Old Event";
        String currentEvent = "Current Event";
        when(currentEventRepository.findById(1L)).thenReturn(Optional.of(new CurrentEvent(currentEvent)));
        ItemOnMenu item = new ItemOnMenu("Fries", 3.5, true, oldEvent);
        when(itemOnMenuRepository.findItemOnMenuByEventName(oldEvent)).thenReturn(List.of(item));

        eventService.importItemPreviousEvent(oldEvent);

        verify(itemOnMenuRepository).save(argThat(newItem ->
                newItem.getName().equals("Fries") &&
                        newItem.getPrice() == 3.5 &&
                        newItem.isChildFood() &&
                        newItem.getEventName().equals(currentEvent)
        ));
    }

    @Test
    void getAllEventNames_returnsMappedNames() {
        when(eventRepository.findAll()).thenReturn(List.of(
                new Event("Event 1"), new Event("Event 2")
        ));

        List<String> names = eventService.getAllEventNames();

        assertEquals(List.of("Event 1", "Event 2"), names);
    }
}
