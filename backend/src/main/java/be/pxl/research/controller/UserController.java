package be.pxl.research.controller;

import be.pxl.research.controller.request.UserLoginRequest;
import be.pxl.research.controller.request.UserRegistrationRequest;
import be.pxl.research.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody @Valid UserLoginRequest loginRequest) {
        try {
            String token = userService.loginUser(loginRequest);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody UserRegistrationRequest newUser, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            Map<String,String> errorMap = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }

        try {
            userService.registerUser(newUser);
            return new ResponseEntity<>("User registered successfully",HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/secure")
    @PreAuthorize("hasRole('HALL')")
    public String securedEndpoint() {
        return "JWT authentication successful! You have access to this protected resource.";
    }
}
