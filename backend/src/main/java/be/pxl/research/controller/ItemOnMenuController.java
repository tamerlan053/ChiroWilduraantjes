package be.pxl.research.controller;
import be.pxl.research.controller.request.DeleteRequest;
import be.pxl.research.controller.request.ItemOnMenuRequest;
import be.pxl.research.controller.response.ItemOnMenuDto;
import be.pxl.research.service.ItemOnMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu-items")
public class ItemOnMenuController {
    @Autowired
    private final ItemOnMenuService menuItemService;

    public ItemOnMenuController(ItemOnMenuService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping("/add")
    public ResponseEntity<Void> add(@RequestBody ItemOnMenuRequest itemOnMenuRequest) {
        menuItemService.addMenuItem(itemOnMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody DeleteRequest deleteRequest) {
        menuItemService.deleteMenuItem(deleteRequest.itemName(), deleteRequest.isChild());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/child")
    public List<ItemOnMenuDto> getAllChildMenuItems() {
        return menuItemService.getAllChildMenuItems();
    }

    @GetMapping("/adult")
    public List<ItemOnMenuDto> getAllAdultMenuItem(){
        return menuItemService.getAllAdultMenuItems();
    }
}
