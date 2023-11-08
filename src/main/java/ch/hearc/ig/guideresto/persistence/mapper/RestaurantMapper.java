package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public final class RestaurantMapper  {

    private Connection connection;
    private static RestaurantMapper INSTANCE;

    private Map<Integer, Restaurant> activeRestaurant = new LinkedHashMap<>();

    public RestaurantMapper(){
        this.connection = DbConnection.createConnection();
    }

    public static RestaurantMapper getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new RestaurantMapper();
        }
        return INSTANCE;
    }

    public Restaurant findByID(int pk){
        if (!activeRestaurant.containsKey(pk)){
            try {
                PreparedStatement query = connection.prepareStatement(
                        "SELECT * FROM RESTAURANTS WHERE NUMERO = ?"
                );
                query.setInt(1, pk);
                ResultSet resultSet = query.executeQuery();
                if (!activeRestaurant.containsKey(resultSet.getInt("NUMERO"))){
                    activeRestaurant.put(resultSet.getInt("NUMERO"),
                            getResFromRS(resultSet));
                }

            }catch (SQLException e){
                System.out.println(e);
            }
        }

        return activeRestaurant.get(pk);
    }
//    public Restaurant findByID(int pk) {
//        try{
//            PreparedStatement query = connection.prepareStatement("SELECT * FROM RESTAURANTS WHERE numero = ?");
//            query.setInt(1, pk);
//            ResultSet resultSet = query.executeQuery();
//            //so on fetching the rest, we immediately grab its type and city, but not, so far, its eval.
//            if (resultSet.next()){
//                return new Restaurant(
//                        resultSet.getInt("numero"),
//                        resultSet.getString("NOM"),
//                        resultSet.getString("DESCRIPTION"),
//                        resultSet.getString("SITE_WEB"),
//                        resultSet.getString("ADRESSE"),
//                        CityMapper.getINSTANCE().findByID(resultSet.getInt("FK_VILL")),
//                        RestaurantTypeMapper.getINSTANCE().findByID(resultSet.getInt("FK_TYPE"))
//                );
//            }
//            else {
//                System.out.println("Aucun restaurant trouvé.");
//                return null;
//            }
//        }catch (SQLException e) {
//            System.out.println(e);
//        }
//        return null;
//    }

    public Set<Restaurant> findAllRestaurant() {
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT * FROM RESTAURANTS"
            );
            ResultSet resultSet = query.executeQuery();
            while (resultSet.next()){
                Restaurant r = getResFromRS(resultSet);
                if (!activeRestaurant.containsKey(r.getId())){
                    activeRestaurant.put(r.getId(), r);
                }
            }
        }catch (SQLException e){
            System.out.println("findALl fucked up");
            System.out.println(e.getMessage());
        }
        for (Restaurant restaurant : activeRestaurant.values()){
            getAllEvalForRestaurant(restaurant);
        }

        return new HashSet<Restaurant>(activeRestaurant.values());
    }

//    public HashSet<Restaurant> findAll() {
//        try {
//            PreparedStatement query = connection.prepareStatement("SELECT * FROM RESTAURANTS");
//            ResultSet resultSet = query.executeQuery();
//
//            HashSet<Restaurant> retour = new HashSet(); //ideally here, I'd make "retour" a Set, and initialize it as a HashSet.
//            while (resultSet.next()){
//                Restaurant restaurant = new Restaurant(
//                        resultSet.getInt("numero"),
//                        resultSet.getString("NOM"),
//                        resultSet.getString("DESCRIPTION"),
//                        resultSet.getString("SITE_WEB"),
//                        resultSet.getString("ADRESSE"),
//                        CityMapper.getINSTANCE().findByID(resultSet.getInt("FK_VILL")),
//                        RestaurantTypeMapper.getINSTANCE().findByID(resultSet.getInt("FK_TYPE"))
//                );
//                retour.add(restaurant);
//            }
//            return retour;
//        }catch (SQLException e){
//            System.out.println(e);
//            return null;
//        }
//    }
    public Set<Restaurant> findResByName(String name){
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT * FROM RESTAURANTS WHERE NOM LIKE ?"
            );
            query.setString(1, "%" + name + "%");

            ResultSet resultSet = query.executeQuery();

            Set<Restaurant> restaurants = new HashSet<>();
            while (resultSet.next()){
                if (activeRestaurant.containsKey(resultSet.getInt("NUMERO"))){
                    restaurants.add(activeRestaurant.get(resultSet.getInt("NUMERO")));
                }
                else {
                    Restaurant r = getResFromRS(resultSet);
                    activeRestaurant.put(r.getId(), r);
                    restaurants.add(r); //This isn't wrong to do, right ? The activeRestaurant can still act as a registre for the restaurant,
                    //while the List makes it easier to work with in the presentation section ?

                }
            }

            return restaurants;
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }

    public Set<Restaurant> findResByCity(String cityName){
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT * \n" +
                            "FROM restaurants r  \n" +
                            "INNER JOIN villes v \n" +
                            "    on v.numero = r.fk_vill\n" +
                            "WHERE v.NOM_VILLE LIKE ?; "
            );
            query.setString(1, '%' + cityName + '%');

            ResultSet resultSet = query.executeQuery();
            Set<Restaurant> restaurants = new HashSet<>();
            while (resultSet.next()){
                if (activeRestaurant.containsKey(resultSet.getInt("NUMERO"))){
                    restaurants.add(activeRestaurant.get(resultSet.getInt("NUMERO")));
                } else { //I should make a Restaurant constructor out of a ResultSet...
                    Restaurant r = getResFromRS(resultSet);
                    activeRestaurant.put(r.getId(), r);
                    restaurants.add(r);
                }
            }
            return restaurants;
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }

    public Set<Restaurant> findResByType(String typeName){
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT * \n" +
                            "FROM restaurants r  \n" +
                            "INNER JOIN TYPES_GASTRONOMIQUES t \n" +
                            "    on t.numero = r.fk_type \n" +
                            "WHERE t.libelle LIKE ?; "
            );
            query.setString(1, '%' + typeName + '%');

            ResultSet resultSet = query.executeQuery();
            Set<Restaurant> restaurants = new HashSet<>();
            while (resultSet.next()){
                if (activeRestaurant.containsKey(resultSet.getInt("NUMERO"))){
                    restaurants.add(activeRestaurant.get(resultSet.getInt("NUMERO")));
                } else {
                    Restaurant r = getResFromRS(resultSet);
                    activeRestaurant.put(r.getId(), r);
                    restaurants.add(r);
                }
            }
            return restaurants;
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }


    public Restaurant insert(Restaurant restaurant) {
        try {
            PreparedStatement insertRes =
                    this.connection.prepareStatement(
                            "INSERT INTO RESTAURANTS (NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL) VALUES(?, ?, ?, ?, ?, ?) "
                    );
            insertRes.setString(1, restaurant.getName());
            insertRes.setString(2, restaurant.getStreet());
            insertRes.setString(3, restaurant.getDescription());
            insertRes.setString(4, restaurant.getWebsite());
            insertRes.setInt(5, restaurant.getType().getId());
            insertRes.setInt(6, restaurant.getAddress().getCity().getId());

            insertRes.executeUpdate();
        }catch (SQLException e){
            System.out.println(e);
        }

        try {
            PreparedStatement fetchBack = connection.prepareStatement("SELECT * FROM RESTAURANTS WHERE NUMERO = (SELECT MAX(NUMERO) FROM RESTAURANTS)");
            ResultSet resultSet = fetchBack.executeQuery();
            return getResFromRS(resultSet);
        }catch (SQLException e){
            System.out.println("fetchback fucked up");
            System.out.println(e);
        }
        return null;
    }


    public Restaurant update(Restaurant restaurant) {
        try {
            PreparedStatement update = connection.prepareStatement(
                    "UPDATE RESTAURANTS SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ?, FK_VILL = ? WHERE NUMERO = ?"
            );
            update.setString(1, restaurant.getName());
            update.setString(2, restaurant.getStreet());
            update.setString(3, restaurant.getDescription());
            update.setString(4, restaurant.getWebsite());
            update.setInt(5, restaurant.getType().getId());
            update.setInt(6, restaurant.getAddress().getCity().getId());

            int check = update.executeUpdate();

            if (check==0){
                throw new SQLException();
            } else {
                return null;// findByID(restaurant.getId());
            }

        }catch (SQLException e){
            System.out.println("Update fucked up");
            System.out.println(e);
        }
        return null;
    }


    public void delete(Restaurant restaurant) {
        if (activeRestaurant.containsKey(restaurant.getId())){
            try {
                PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM RESTAURANTS WHERE numero = ?");
                deleteQuery.setInt(1, restaurant.getId());

                int check = deleteQuery.executeUpdate();
                if (check==0){
                    throw new SQLException();
                }
                activeRestaurant.remove(restaurant.getId());

            }catch (SQLException e){
                System.out.println("delete fucked up");
                System.out.println(e);
            }
        }

    }

    public void getAllEvalForRestaurant(Restaurant restaurant){
        List<BasicEvaluation> basicEvals = BasicEvalMapper.getINSTANCE().findForRestaurant(restaurant);
        for (BasicEvaluation basicEvaluation : basicEvals){
            restaurant.getEvaluations().add(basicEvaluation);
        }
        List<CompleteEvaluation> completeEvaluations = CompleteEvalMapper.getINSTANCE().findForRestaurant(restaurant);
        for (CompleteEvaluation completeEvaluation : completeEvaluations){
            restaurant.getEvaluations().add(completeEvaluation);
        }
    }


    // TO NOTE :
    // IT IS VERY OBVIOUS TO MAKE An object constructor out of a resultset, for all classes.
    // I only thought of this late though, so some classes got forgotten in the process and don't have that
    // and have a lot of duplicate.
    // it would also make great sense to declare some final variable for the column name, so that they can be easily reused.
    // but same thing, I thought of this too late, and I don't have time.
    private Restaurant getResFromRS(ResultSet rs) throws SQLException{
        return new Restaurant(
                rs.getInt("NUMERO"),
                rs.getString("NOM"),
                rs.getString("DESCRIPTION"),
                rs.getString("SITE_WEB"),
                rs.getString("ADRESSE"),
                CityMapper.getINSTANCE().findByID(rs.getInt("FK_VILL")),
                RestaurantTypeMapper.getINSTANCE().findByID(rs.getInt("FK_TYPE"))
        );
    }
}
