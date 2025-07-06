package be.pxl.research.controller;

import be.pxl.research.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Void> changeCurrentEvent(@RequestBody String newEventName) {
        eventService.changeCurrentEvent(newEventName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentEvent() {
        return ResponseEntity.ok(eventService.getCurrentEventName());
    }

    @GetMapping
    public List<String> getEvents() {
        return eventService.getAllEventNames();
    }

    @GetMapping("/timeoptions")
    public List<String> getTimeOptions() {
        return eventService.getTimeOptions();
    }

    @PostMapping("/add-time-option")
    public ResponseEntity<Void> addTimeOptions(@RequestBody @Valid String timeOption){
        eventService.addTimeOption(timeOption);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteTimeOption(@RequestBody @Valid String timeOption){
        eventService.deleteOption(timeOption);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importItemsPreviousEvent(@RequestBody @Valid String eventName){
        eventService.importItemPreviousEvent(eventName);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
