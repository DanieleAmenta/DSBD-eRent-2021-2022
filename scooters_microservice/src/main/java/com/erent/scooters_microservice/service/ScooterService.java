package com.erent.scooters_microservice.service;


import com.erent.scooters_microservice.data.ScooterRepository;
import com.erent.scooters_microservice.entities.Scooter;
import com.erent.scooters_microservice.entities.ScooterStatus;
import com.erent.shared_entities.scooter.ScooterOperation;
import com.erent.shared_entities.scooter.ScooterRequest;
import com.erent.shared_entities.scooter.ScooterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

@Service
public class ScooterService {

    @Autowired
    ScooterRepository repository;

    @Value("${center_lat}")
    private Double center_lat;

    @Value("${center_lon}")
    private Double center_lon;

    @Value("${min_distance}")
    private Integer min_dist;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ScooterResponse SwitchScooterStatus(ScooterRequest request) {
        ScooterResponse response = new ScooterResponse();
        response.setScooter_id(request.getScooter_id());
        response.setSuccess(false);

        Optional<Scooter> scooter = repository.findById(request.getScooter_id());

        if (!scooter.isPresent()) {
            response.setMessage("Scooter not found");
            return response;
        }

        if (GetDistance(request.getLat(), request.getLon()) > min_dist) {
            response.setMessage("Outside the area");
            return response;
        }

        if (!request.getScooterCode().equals(scooter.get().getCode())) {
            response.setMessage("Wrong scooter code");
            return response;
        }

        if (request.getOperation() == ScooterOperation.LOCK) {
            if (scooter.get().getScooterStatus() == ScooterStatus.LOCKED) {
                response.setMessage("Scooter already locked");
                return response;
            }

            scooter.get().setScooterStatus(ScooterStatus.LOCKED);
            scooter.get().setLat(request.getLat());
            scooter.get().setLon(request.getLon());

        } else {

            if (scooter.get().getScooterStatus() == ScooterStatus.UNLOCKED) {
                response.setMessage("Scooter already unlocked");
                return response;
            }

            scooter.get().setScooterStatus(ScooterStatus.UNLOCKED);
        }

        repository.save(scooter.get());

        response.setMessage("Operation " + request.getOperation().toString() + " done successfully");
        response.setSuccess(true);
        return response;
    }

    public Scooter AddScooter(Scooter scooter) {
        Optional<Scooter> scooterByCode = repository.findByCode(scooter.getCode());
        if (scooterByCode.isPresent()) {
            return null;
        }
        return repository.save(scooter);
    }

    // Calculate the geometric distance between the scooter and the client on the basis of the "lat" and "long" parameters.
    // SOURCE: https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
    public Double GetDistance(Double userLat, Double userLong) {
        Integer R = 6371; // Radius of the earth in km
        Double pigreco = 3.1415927;

        /* Convert degrees to radians */
        Double lat_alfa = pigreco * userLat / 180;
        Double lat_beta = pigreco * center_lat / 180;
        Double lon_alfa = pigreco * userLong / 180;
        Double lon_beta = pigreco * center_lon / 180;

        /* Calculate the angle including fi */
        Double fi = Math.abs(lon_alfa - lon_beta);

        /* Calculate the third side of the spherical triangle */
        Double c = Math.acos(sin(lat_beta) * sin(lat_alfa) + cos(lat_beta) * cos(lat_alfa) * cos(fi));

        /* Calculate the distance on the earth's surface */
        Double d = c * R;
        return d;
    }

    public List<Scooter> SearchScooters() {
        return repository.findByScooterStatus(ScooterStatus.LOCKED);
    }

    public Optional<Scooter> SearchScooterById(String scooter_id) {
        return repository.findByScooterId(scooter_id);
    }
}

