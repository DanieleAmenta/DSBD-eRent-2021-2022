package com.erent.users_microservice.controller;

import com.erent.users_microservice.entities.AuthResponse;
import com.erent.users_microservice.entities.LoginRequest;
import com.erent.users_microservice.entities.User;
import com.erent.users_microservice.service.AuthService;
import com.erent.users_microservice.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/${api_base}")
public class UserController {

    @Autowired
    UserService service;

    @Autowired
    AuthService authService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${admin_user_id}")
    private String admin_user_id;

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }

    @PostMapping(path = "/register")
    public @ResponseBody
    String registerUser(@RequestBody User u) {

        Optional<User> user = service.getUserByEmail(u.getEmail());

        if (user.isPresent()) {
            String message = "Email already registered.";
            return message;
        }

        User newUser = service.AddUser(u);

        return new Gson().toJson(newUser);
    }

    @PostMapping(path = "/login")
    public @ResponseBody
    AuthResponse login(@RequestBody LoginRequest request,
                       HttpServletRequest servletRequest) {

        Optional<User> user = service.getUserByEmailAndPassword(request.getEmail(), request.getPassword());

        if (!user.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found."
            );
        }

        return authService.getAuthenticationResponse(user.get());
    }

    @GetMapping(path = "/id/{id}")
    public @ResponseBody
    String getUserById(@PathVariable String id,
                     @RequestHeader("X-User-ID") String user_id,
                     HttpServletRequest servletRequest) {

        Optional<User> user = service.getUserById(id);

        if (!user.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if (!user.get().getId().equals(user_id) && !user_id.equals(admin_user_id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        return new Gson().toJson(user.get());
    }

    @GetMapping(path = "/email/{email}")
    public @ResponseBody
    String getUser(@PathVariable String email,
                 @RequestHeader("X-User-ID") String user_id,
                 HttpServletRequest servletRequest) {

        Optional<User> user = service.getUserByEmail(email);

        if (!user.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if (!user.get().getId().equals(user_id) && !user_id.equals(admin_user_id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        return new Gson().toJson(user.get());
    }


    @GetMapping(path = "/users")
    public @ResponseBody
    String getAllUsers(@RequestHeader("X-User-ID") String user_id,
                       HttpServletRequest servletRequest) {

        if (!user_id.equals(admin_user_id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        List<User> usersList = service.getAllUsers();

        return new Gson().toJson(usersList);
    }
}
