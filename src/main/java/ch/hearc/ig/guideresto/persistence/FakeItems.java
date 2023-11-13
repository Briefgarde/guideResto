package ch.hearc.ig.guideresto.persistence;

import static java.util.stream.Collectors.toSet;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FakeItems {
    
    private final Set<RestaurantType> types;
    private final Set<Restaurant> restaurants;
    private final Set<EvaluationCriteria> criterias;
    private final Set<City> cities;

    public FakeItems() {
        RestaurantType typeSuisse = new RestaurantType(1, "Cuisine suisse", "Cuisine classique et plats typiquement suisses");
        RestaurantType typeGastro = new RestaurantType(2, "Restaurant gastronomique", "Restaurant gastronomique de haut standing");
        RestaurantType typePizzeria = new RestaurantType(3, "Pizzeria", "Pizzas et autres spécialités italiennes");

        EvaluationCriteria critService = new EvaluationCriteria(1, "Service", "Qualité du service");
        EvaluationCriteria critCuisine = new EvaluationCriteria(2, "Cuisine", "Qualité de la nourriture");
        EvaluationCriteria critCadre = new EvaluationCriteria(3, "Cadre", "L'ambiance et la décoration sont-elles bonnes ?");

        City city = new City(1, "2000", "Neuchatel");
        City city2 = new City(2, "5000", "Bienne");

        Restaurant restaurant = new Restaurant(1, "Fleur-de-Lys", "Pizzeria au centre de Neuchâtel", "http://www.pizzeria-neuchatel.ch/", "Rue du Bassin 10", city, typeSuisse);
        city.getRestaurants().add(restaurant);
        typeSuisse.getRestaurants().add(restaurant);
        restaurant.getEvaluations().add(new BasicEvaluation(1, LocalDate.now(), restaurant, true, "1.2.3.4"));
        restaurant.getEvaluations().add(new BasicEvaluation(2, LocalDate.now(), restaurant, true, "1.2.3.5"));
        restaurant.getEvaluations().add(new BasicEvaluation(3, LocalDate.now(), restaurant, false, "1.2.3.6"));

        CompleteEvaluation ce = new CompleteEvaluation(1, LocalDate.now(), restaurant, "Génial !", "Toto");
        ce.getGrades().add(new Grade(1, 4, ce, critService));
        ce.getGrades().add(new Grade(2, 5, ce, critCuisine));
        ce.getGrades().add(new Grade(3, 4, ce, critCadre));
        restaurant.getEvaluations().add(ce);

        ce = new CompleteEvaluation(2, LocalDate.now(), restaurant, "Très bon", "Titi");
        ce.getGrades().add(new Grade(4, 4, ce, critService));
        ce.getGrades().add(new Grade(5, 4, ce, critCuisine));
        ce.getGrades().add(new Grade(6, 4, ce, critCadre));
        restaurant.getEvaluations().add(ce);

        Restaurant restaurant2 = new Restaurant(2, "La Maison du Prussien", "Restaurant gastronomique renommé de Neuchâtel", "www.hotel-prussien.ch/‎", "Rue des Tunnels 11", city, typeGastro);
        typeGastro.getRestaurants().add(restaurant2);
        restaurant2.getEvaluations().add(new BasicEvaluation(4, LocalDate.now(), restaurant2, true, "1.2.3.7"));
        restaurant2.getEvaluations().add(new BasicEvaluation(5, LocalDate.now(), restaurant2, true, "1.2.3.8"));
        restaurant2.getEvaluations().add(new BasicEvaluation(6, LocalDate.now(), restaurant2, true, "1.2.3.9"));
        ce = new CompleteEvaluation(3, LocalDate.now(), restaurant2, "Un régal !", "Dupont");
        ce.getGrades().add(new Grade(7, 5, ce, critService));
        ce.getGrades().add(new Grade(8, 5, ce, critCuisine));
        ce.getGrades().add(new Grade(9, 5, ce, critCadre));
        restaurant2.getEvaluations().add(ce);

        ce = new CompleteEvaluation(2, LocalDate.now(), restaurant2, "Rien à dire, le top !", "Dupasquier");
        ce.getGrades().add(new Grade(10, 5, ce, critService));
        ce.getGrades().add(new Grade(11, 5, ce, critCuisine));
        ce.getGrades().add(new Grade(12, 5, ce, critCadre));
        restaurant2.getEvaluations().add(ce);

        this.types = Set.of(typeSuisse, typeGastro, typePizzeria);
        this.criterias = Set.of(critService, critCuisine, critCadre);
        this.cities = Set.of(city, city2);
        this.restaurants = Set.of(restaurant, restaurant2);
    }

    public Set<Restaurant> getAllRestaurants(){
        return restaurants;
    }
    
    public Set<EvaluationCriteria> getEvaluationCriterias(){
        return criterias;
    }
    
    public Set<RestaurantType> getRestaurantTypes(){
        return types;
    }
    
    public Set<City> getCities(){
        return cities;
    }
    
}
