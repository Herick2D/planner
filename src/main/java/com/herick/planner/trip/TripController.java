package com.herick.planner.trip;

import com.herick.planner.participant.Participant;
import com.herick.planner.participant.ParticipantCreateResponse;
import com.herick.planner.participant.ParticipantData;
import com.herick.planner.participant.ParticipantRequestPayload;
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
import java.util.ArrayList;
import java.util.List;
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

  @PostMapping("/{tripId}/invite")
  public ResponseEntity<ParticipantCreateResponse> something(@PathVariable UUID tripId, @RequestBody ParticipantRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(tripId);

    if(trip.isPresent()){
      Trip rawTrip = trip.get();

      ParticipantCreateResponse participantResponse =  this.participantService.registerParticipantToEvent(payload.email(), rawTrip);

      if(rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payload.email());

      return ResponseEntity.ok(participantResponse);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{tripId}/participants")
  public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID tripId){
    List<ParticipantData> participantList = this.participantService.getAllParticipantsFromTrip(tripId);

    return ResponseEntity.ok(participantList);
  }
}
