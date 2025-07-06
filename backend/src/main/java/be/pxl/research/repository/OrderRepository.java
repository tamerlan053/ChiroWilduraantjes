package be.pxl.research.repository;

import be.pxl.research.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> getOrdersByEventName(String eventName);
    Order getOrderById(Long id);
}
