package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public abstract class Evaluation {

  private Integer id;
  private LocalDate visitDate;
  private Restaurant restaurant;

  public Evaluation(Integer id, LocalDate visitDate, Restaurant restaurant) {
    this.id = id;
    this.visitDate = visitDate;
    this.restaurant = restaurant;
  }

  public Integer getId() {
    return id;
  }

  public LocalDate getVisitDate() {
    return visitDate;
  }

  public Restaurant getRestaurant() {
    return restaurant;
  }
}