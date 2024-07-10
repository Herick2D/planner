package com.herick.planner.link;

import com.herick.planner.trip.Trip;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "links")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Link {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false)
  private String url;

  @Column(nullable = false)
  private String title;

  @ManyToOne
  @JoinColumn(name = "trip_id", nullable = false)
  private Trip trip;

}
