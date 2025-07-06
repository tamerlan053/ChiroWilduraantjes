package be.pxl.research.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "event_time_options", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "time_option")
    private Set<String> timeOptions = new HashSet<>();

    private String linkedEventName;

    // Default constructor for JPA
    public Event() {
    }

    public Event(String linkedEventName) {
        this.linkedEventName = linkedEventName;
    }

    public Event(List<String> timeOptions, String linkedEventName) {
        this.timeOptions = new HashSet<>(timeOptions);
        this.linkedEventName = linkedEventName;
    }

    public Set<String> getTimeOptions() {
        return timeOptions;
    }

    public void setTimeOptions(Set<String> timeOptions) {
        this.timeOptions = timeOptions;
    }

    public String getLinkedEventName() {
        return linkedEventName;
    }

    public void setLinkedEventName(String linkedEventName) {
        this.linkedEventName = linkedEventName;
    }

    public void addTimeOption(String timeOption) {
        if (timeOption != null && !timeOption.trim().isEmpty() && !this.timeOptions.contains(timeOption)) {
            this.timeOptions.add(timeOption);
        }
    }

    public void deleteOption(String timeOption) {
        if (timeOption != null && !timeOption.trim().isEmpty()) {
            this.timeOptions.remove(timeOption);
        }
    }
}
