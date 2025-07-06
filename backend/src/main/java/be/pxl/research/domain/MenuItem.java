package be.pxl.research.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Menu Item name is required")
    private String name;

    @Min(value = 0, message = "Should be at least 0")
    private int quantity;

    @NotNull(message = "Menu Item price is required")
    private double price;

    public MenuItem() {
    }

    // This constructor is used for the totals since price doesn't matter here
    public MenuItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public MenuItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
