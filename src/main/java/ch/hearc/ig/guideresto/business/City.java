package ch.hearc.ig.guideresto.business;

import java.util.HashSet;
import java.util.Set;

public class City {
    private Integer id;
    private String zipCode;
    private String cityName;
    private Set<Restaurant> restaurants;

    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
        this.restaurants = new HashSet<>();
    }
    
    public String getZipCode() {
        return zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public Integer getId() {
        return id;
    }


}