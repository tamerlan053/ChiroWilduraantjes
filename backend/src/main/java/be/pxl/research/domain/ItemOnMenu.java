package be.pxl.research.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Entity
public class ItemOnMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Menu Item name is required")
    private String name;

    @NotNull(message = "Menu Item price is required")
    private double price;

    private boolean childFood;

    private String eventName;

    public ItemOnMenu() {

    }

    public ItemOnMenu(String name, double price, boolean isChildFood, String eventName) {
        this.name = name;
        this.price = price;
        this.childFood = isChildFood;
        this.eventName = eventName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isChildFood() {
        return childFood;
    }

    public void setChildFood(boolean childFood) {
        this.childFood = childFood;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
