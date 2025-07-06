package be.pxl.research.repository;

import be.pxl.research.domain.CurrentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentEventRepository extends JpaRepository<CurrentEvent, Long> {
}
