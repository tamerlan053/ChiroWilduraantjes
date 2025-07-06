package be.pxl.research.controller;

import be.pxl.research.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void changeCurrentEventShouldReturn201() throws Exception {
        String newEvent = "New Event";

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newEvent))
                .andExpect(status().isCreated());
    }

    @Test
    void getCurrentEvent_ShouldReturnCurrentEventName() throws Exception {
        when(eventService.getCurrentEventName()).thenReturn("Current Event");

        mockMvc.perform(get("/events/current"))
                .andExpect(status().isOk())
                .andExpect(content().string("Current Event"));
    }

    @Test
    void getEvents_ShouldReturnListOfEvents() throws Exception {
        when(eventService.getAllEventNames()).thenReturn(List.of("Event A", "Event B"));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Event A"))
                .andExpect(jsonPath("$[1]").value("Event B"));
    }

    @Test
    void getTimeOptions_ShouldReturnListOfTimeOptions() throws Exception {
        List<String> timeOptions = List.of("10:00", "12:00", "14:00");
        when(eventService.getTimeOptions()).thenReturn(timeOptions);

        mockMvc.perform(get("/events/timeoptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", containsInAnyOrder("10:00", "12:00", "14:00")));
    }

    @Test
    void addTimeOption_ShouldCallServiceAndReturn200() throws Exception {
        String option = "16:00";

        mockMvc.perform(post("/events/add-time-option")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(option))
                .andExpect(status().isOk());

        verify(eventService).addTimeOption(option);
    }

    @Test
    void deleteTimeOption_ShouldCallServiceAndReturn200() throws Exception {
        String option = "12:00";

        mockMvc.perform(delete("/events/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(option))
                .andExpect(status().isOk());

        verify(eventService).deleteOption(option);
    }

    @Test
    void importItemsPreviousEvent_ShouldCallServiceAndReturn200() throws Exception {
        String eventName = "Event A";

        mockMvc.perform(post("/events/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventName))
                .andExpect(status().isOk());

        verify(eventService).importItemPreviousEvent(eventName);
    }

    @Test
    void changeCurrentEvent_WithEmptyName_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTimeOptions_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        when(eventService.getTimeOptions()).thenReturn(List.of());

        mockMvc.perform(get("/events/timeoptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
