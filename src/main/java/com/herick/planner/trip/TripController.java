package com.herick.planner.trip;

import com.herick.planner.participant.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

  @Autowired
  private ParticipantService participantService;

  @Autowired
  private TripRepository repository;

  @PostMapping
  public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {

    Trip newTrip = new Trip(payload);
    this.repository.save(newTrip);

    this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

    return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
  }

  @GetMapping("/{tripId}")
  public ResponseEntity<Trip> getTripDetails(@PathVariable UUID tripId) {
    Optional<Trip> trip = this.repository.findById(tripId);

    return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

  }

  @PutMapping("/{tripId}")
  public ResponseEntity<Trip> updateTrip(@PathVariable UUID tripId, @RequestBody TripRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(tripId);

    if(trip.isPresent()){
      Trip rawTrip = trip.get();
      rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
      rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
      rawTrip.setDestination(payload.destination());

      this.repository.save(rawTrip);

      return ResponseEntity.ok(rawTrip);
    }

    return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{tripId}/confirm")
  public ResponseEntity<Trip> confirmTrip(@PathVariable UUID tripId) {
    Optional<Trip> trip = this.repository.findById(tripId);

    if(trip.isPresent()){
      Trip rawTrip = trip.get();
      rawTrip.setIsConfirmed(true);

      this.repository.save(rawTrip);
      this.participantService.triggerConfirmationEmailToParticipants(tripId);

      return ResponseEntity.ok(rawTrip);
    }

    return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

}
