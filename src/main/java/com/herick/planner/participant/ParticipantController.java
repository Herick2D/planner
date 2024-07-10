package com.herick.planner.participant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/participants")
public class ParticipantController {

  @Autowired
  private ParticipantRepository repository;

  @PostMapping("/{participantId}/confirm")
  public ResponseEntity<Participant> confirmParticipant(@PathVariable UUID participantId, @RequestBody ParticipantRequestPayload payload){
    Optional<Participant> participant = this.repository.findById(participantId);

    if(participant.isPresent()){
      Participant rawParticipant = participant.get();
      rawParticipant.setIsConfirmed(true);
      rawParticipant.setName(payload.name());

      this.repository.save(rawParticipant);

      return ResponseEntity.ok(rawParticipant);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}