package com.herick.planner.trip;

import com.herick.planner.activity.ActivityData;
import com.herick.planner.activity.ActivityRequestPayload;
import com.herick.planner.activity.ActivityResponse;
import com.herick.planner.activity.ActivityService;
import com.herick.planner.link.LinkData;
import com.herick.planner.link.LinkRequestPayload;
import com.herick.planner.link.LinkResponse;
import com.herick.planner.link.LinkService;
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
  @Autowired
  private ActivityService activityService;
  @Autowired
  private LinkService linkService;

  // Endpoint trips

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

  // Endpoint Participants

  @PostMapping("/{tripId}/invite")
  public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID tripId, @RequestBody ParticipantRequestPayload payload) {
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

  // Endpoint Activities

  @PostMapping("/{tripId}/activities")
  public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID tripId, @RequestBody ActivityRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(tripId);

    if(trip.isPresent()){
      Trip rawTrip = trip.get();

      ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);

      return ResponseEntity.ok(activityResponse);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{tripId}/activities")
  public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID tripId){
    List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(tripId);

    return ResponseEntity.ok(activityDataList);
  }

  // Endpoint Links

  @PostMapping("/{tripId}/links")
  public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID tripId, @RequestBody LinkRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(tripId);

    if(trip.isPresent()){
      Trip rawTrip = trip.get();

      LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);

      return ResponseEntity.ok(linkResponse);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{tripId}/links")
  public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID tripId){
    List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(tripId);

    return ResponseEntity.ok(linkDataList);
  }

}
