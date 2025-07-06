package be.pxl.research.repository;

import be.pxl.research.domain.ItemOnMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOnMenuRepository extends JpaRepository<ItemOnMenu, Integer> {
    List<ItemOnMenu> findItemOnMenuByEventName(String eventName);
}
