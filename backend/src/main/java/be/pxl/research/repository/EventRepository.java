package be.pxl.research.repository;

import be.pxl.research.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Event findEventByLinkedEventName(String eventName);
    Boolean existsEventByLinkedEventName(String eventName);
}
