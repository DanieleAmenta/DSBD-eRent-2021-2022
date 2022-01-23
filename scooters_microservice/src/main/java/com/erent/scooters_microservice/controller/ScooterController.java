package com.erent.scooters_microservice.controller;

import com.erent.scooters_microservice.data.ScooterRepository;
import com.erent.scooters_microservice.entities.Scooter;
import com.erent.scooters_microservice.entities.ScooterStatus;
import com.erent.scooters_microservice.service.ScooterService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(path = "/${api_base}")
public class ScooterController {

    @Autowired
    ScooterRepository repository;

    @Autowired
    ScooterService service;

    @Value("${admin_user_id}")
    private String admin_user_id;

    @Value("${center_lat}")
    private Double center_lat;

    @Value("${center_lon}")
    private Double center_lon;

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }

    @PostMapping(path = "/add")
    public @ResponseBody
    String addScooter(@RequestBody Scooter scooter,
                      @RequestHeader("X-User-ID") String user_id,
                      HttpServletRequest servletRequest) {

        if (!user_id.equals(admin_user_id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        if (scooter.getCode() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Malformed request"
            );
        }

        scooter.setScooterStatus(ScooterStatus.LOCKED);
        scooter.setLat(center_lat);
        scooter.setLon(center_lon);
        Scooter scooterAdded = service.AddScooter(scooter);

        if (scooterAdded == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad Code"
            );
        }

        return new Gson().toJson(scooterAdded);
    }

    @GetMapping(path = "/scooters")
    public @ResponseBody
    String getScooters(HttpServletRequest servletRequest) {

        List<Scooter> scootersList = service.SearchScooters();

        if (scootersList.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ""
            );
        }

        return new Gson().toJson(scootersList);
    }
}
