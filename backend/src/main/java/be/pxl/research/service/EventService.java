package be.pxl.research.service;

import be.pxl.research.domain.CurrentEvent;
import be.pxl.research.domain.Event;
import be.pxl.research.domain.ItemOnMenu;
import be.pxl.research.repository.CurrentEventRepository;
import be.pxl.research.repository.EventRepository;
import be.pxl.research.repository.ItemOnMenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ItemOnMenuRepository itemOnMenuRepository;
    private final CurrentEventRepository currentEventRepository;

    public EventService(EventRepository eventRepository,
                        ItemOnMenuRepository itemOnMenuRepository,
                        CurrentEventRepository currentEventRepository) {
        this.eventRepository = eventRepository;
        this.itemOnMenuRepository = itemOnMenuRepository;
        this.currentEventRepository = currentEventRepository;
    }

    public void changeCurrentEvent(String newEventName) {
        currentEventRepository.save(new CurrentEvent(newEventName));

        boolean eventExists = eventRepository.existsEventByLinkedEventName(newEventName);
        if (!eventExists) {
            Event newEvent = new Event(newEventName);
            eventRepository.save(newEvent);
        }
    }



    public String getCurrentEventName() {
        return currentEventRepository.findById(1L)
                .map(CurrentEvent::getName)
                .orElse(null);
    }

    public List<String> getTimeOptions() {
        String currentName = getCurrentEventName();
        if (currentName == null) return new ArrayList<>();

        Event event = eventRepository.findEventByLinkedEventName(currentName);
        if (event == null) return new ArrayList<>();

        return new ArrayList<>(event.getTimeOptions());
    }

    @Transactional
    public void addTimeOption(String timeOption) {
        String currentName = getCurrentEventName();
        Event event = eventRepository.findEventByLinkedEventName(currentName);
        if (event == null) {
            event = new Event(currentName);
        }
        event.addTimeOption(timeOption);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteOption(String timeOption) {
        String currentName = getCurrentEventName();
        Event event = eventRepository.findEventByLinkedEventName(currentName);
        if (event != null) {
            event.deleteOption(timeOption);
            eventRepository.save(event);
        }
    }

    @Transactional
    public void importItemPreviousEvent(String eventName) {
        String currentName = getCurrentEventName();
        if (!eventName.equals(currentName)) {
            List<ItemOnMenu> onMenuItems = itemOnMenuRepository.findItemOnMenuByEventName(eventName);
            for (ItemOnMenu itemOnMenu : onMenuItems) {
                ItemOnMenu newEventItem = new ItemOnMenu(
                        itemOnMenu.getName(),
                        itemOnMenu.getPrice(),
                        itemOnMenu.isChildFood(),
                        currentName
                );
                itemOnMenuRepository.save(newEventItem);
            }
        }
    }

    public List<String> getAllEventNames() {
        return eventRepository.findAll().stream()
                .map(Event::getLinkedEventName)
                .toList();
    }
}
