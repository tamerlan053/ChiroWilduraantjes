package be.pxl.research.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Event name is required")
    private String eventName;

    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Family name is required")
    private String familyName;

    private String remarks;

    private int drinkTokens;

    private boolean inProgress = false;

    private boolean readyToServe = false;

    private String tableNumber = "";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<MenuItem> menuItems;
    private boolean isPayed = false;


    public Order() {
    }

    public Order(String eventName, String email, LocalDateTime arrivalTime, String familyName, String remarks) {
        this(eventName, email, arrivalTime, familyName, remarks, 0);
    }

    public Order(String eventName, String email, LocalDateTime arrivalTime, String familyName, String remarks, int drinkTokens) {
        this.eventName = eventName;
        this.email = email;
        this.arrivalTime = arrivalTime;
        this.familyName = familyName;
        this.remarks = remarks;
        this.drinkTokens = drinkTokens;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        isPayed = payed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public void setReadyToServe(boolean readyToServe) {
        this.readyToServe = readyToServe;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public Long getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public boolean isReadyToServe() {
        return readyToServe;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public int getDrinkTokens() {
        return drinkTokens;
    }

    public void setDrinkTokens(int drinkTokens) {
        this.drinkTokens = drinkTokens;
    }
}
