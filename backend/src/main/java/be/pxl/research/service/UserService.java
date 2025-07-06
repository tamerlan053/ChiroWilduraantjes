package be.pxl.research.service;

import be.pxl.research.domain.User;
import be.pxl.research.exception.UserException;
import be.pxl.research.repository.UserRepository;
import be.pxl.research.controller.request.UserLoginRequest;
import be.pxl.research.controller.request.UserRegistrationRequest;
import be.pxl.research.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String loginUser(UserLoginRequest userLogin){
        Optional<User> userOptional = userRepository.findByUsername(userLogin.username());

        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(passwordEncoder.matches(userLogin.password(), user.getPassword())){
                return jwtUtil.generateToken(user.getUsername(), user.getRoles());
            } else if (!passwordEncoder.matches(userLogin.password(), user.getPassword())) {
                throw new UserException("Invalid credentials");

            }
        }

        return "done";
    }

    public User registerUser(UserRegistrationRequest newUser) {
        if(userRepository.findByUsername(newUser.username()).isPresent()){
            throw new UserException("Username is already in use");
        }

        User user = new User();
        user.setUsername(newUser.username());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setRoles(new HashSet<>());

        return userRepository.save(user);
    }
}
