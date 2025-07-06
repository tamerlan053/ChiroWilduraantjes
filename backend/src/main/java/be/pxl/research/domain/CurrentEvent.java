package be.pxl.research.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CurrentEvent {
    @Id
    private Long id = 1L;

    private String name;

    public CurrentEvent() {
    }

    public CurrentEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
