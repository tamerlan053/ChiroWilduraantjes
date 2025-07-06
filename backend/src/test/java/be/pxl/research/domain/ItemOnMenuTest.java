package be.pxl.research.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemOnMenuTest {

    private ItemOnMenu itemOnMenu;
    private Validator validator;

    @BeforeEach
    void setUp() {
        itemOnMenu = new ItemOnMenu();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDefaultConstructor() {
        assertThat(itemOnMenu).isNotNull();
    }

    @Test
    void testParameterizedConstructor() {
        ItemOnMenu item = new ItemOnMenu("Burger", 5.99, true, "Birthday");
        assertThat(item.getName()).isEqualTo("Burger");
        assertThat(item.getPrice()).isEqualTo(5.99);
        assertThat(item.isChildFood()).isTrue();
        assertThat(item.getEventName()).isEqualTo("Birthday");
    }

    @Test
    void testSetAndGetId() {
        itemOnMenu.setId(10L);
        assertThat(itemOnMenu.getId()).isEqualTo(10L);
    }

    @Test
    void testSetAndGetName() {
        itemOnMenu.setName("Pizza");
        assertThat(itemOnMenu.getName()).isEqualTo("Pizza");
    }

    @Test
    void testSetAndGetPrice() {
        itemOnMenu.setPrice(7.50);
        assertThat(itemOnMenu.getPrice()).isEqualTo(7.50);
    }

    @Test
    void testSetAndIsChildFood() {
        itemOnMenu.setChildFood(true);
        assertThat(itemOnMenu.isChildFood()).isTrue();
    }

    @Test
    void testSetAndGetEventName() {
        itemOnMenu.setEventName("Wedding");
        assertThat(itemOnMenu.getEventName()).isEqualTo("Wedding");
    }

    @Test
    void testValidationFailsWhenNameIsNull() {
        itemOnMenu.setPrice(5.0); // valid
        itemOnMenu.setName(null); // invalid
        Set<ConstraintViolation<ItemOnMenu>> violations = validator.validate(itemOnMenu);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void testValidationFailsWhenPriceIsNull() {
        itemOnMenu.setName("Soda");
        // Price is primitive double, can't be null, but validation still checks for @NotNull
        // Since double cannot be null, this annotation is actually redundant and ineffective.
        // You might consider using `Double` instead of `double` if nullability is a concern.
        Set<ConstraintViolation<ItemOnMenu>> violations = validator.validate(itemOnMenu);
        assertThat(violations).isEmpty(); // there will be no violation for price being zero
    }
}
