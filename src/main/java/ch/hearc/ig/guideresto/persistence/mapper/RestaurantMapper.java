package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class RestaurantMapper  {

    private Connection connection;
    private static RestaurantMapper INSTANCE;

    private Map<Integer, Restaurant> activeRestaurant = new LinkedHashMap<>();

    public RestaurantMapper(){
        this.connection = DbConnection.getConnection();
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
                    getAllEvalForRestaurant(r);
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
                    "SELECT * FROM restaurants r " +
                            "INNER JOIN villes v " +
                            "   on v.numero = r.fk_vill " +
                            "WHERE v.NOM_VILLE LIKE ?"
            );
            query.setString(1, '%' + cityName + '%');

            ResultSet resultSet = query.executeQuery();
            Set<Restaurant> restaurants = new HashSet<>();
            while (resultSet.next()){
                if (activeRestaurant.containsKey(resultSet.getInt("NUMERO"))){
                    restaurants.add(activeRestaurant.get(resultSet.getInt("NUMERO")));
                } else { //I should make a Restaurant constructor out of a ResultSet...
                    Restaurant r = getResFromRS(resultSet);
                    getAllEvalForRestaurant(r);
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
                            "WHERE t.libelle LIKE ? "
            );
            query.setString(1, '%' + typeName + '%');

            ResultSet resultSet = query.executeQuery();
            Set<Restaurant> restaurants = new HashSet<>();
            while (resultSet.next()){
                if (activeRestaurant.containsKey(resultSet.getInt("NUMERO"))){
                    restaurants.add(activeRestaurant.get(resultSet.getInt("NUMERO")));
                } else {
                    Restaurant r = getResFromRS(resultSet);
                    getAllEvalForRestaurant(r);
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

    private void insert(Restaurant restaurant){
        try {
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO RESTAURANTS (NUMERO, NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setInt(1, restaurant.getId());
            insert.setString(2, restaurant.getName());
            insert.setString(3, restaurant.getStreet());
            insert.setString(4, restaurant.getDescription());
            insert.setString(5, restaurant.getWebsite());
            insert.setInt(6, restaurant.getType().getId());
            insert.setInt(7, restaurant.getAddress().getCity().getId());

            insert.executeUpdate();

        }catch (SQLException e){
            System.out.println("insert Rest no good");
            System.out.println(e);
        }
    }


    public boolean update(Restaurant restaurant) {
        try {
            PreparedStatement update = connection.prepareStatement(
                    "UPDATE RESTAURANTS SET NOM = ?,  DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ? WHERE NUMERO = ?"
            );
            update.setString(1, restaurant.getName());
            update.setString(2, restaurant.getDescription());
            update.setString(3, restaurant.getWebsite());
            update.setInt(4, restaurant.getType().getId());
            update.setInt(5, restaurant.getId());

            int check = update.executeUpdate();

            if (check==0){
                return false;
            } else {
                return true;
            }

        }catch (SQLException e){
            System.out.println("Update fucked up");
            System.out.println(e);
        }
        return false;
    }

    public boolean updateRestAdresse(Restaurant restaurant){
        try {
            PreparedStatement update = connection.prepareStatement(
                    "UPDATE RESTAURANTS SET ADRESSE = ?, FK_VILL = ? WHERE NUMERO = ?"
            );
            update.setString(1, restaurant.getStreet());
            update.setInt(2, restaurant.getAddress().getCity().getId());
            update.setInt(3, restaurant.getId());

            int check = update.executeUpdate();

            if (check==0){
                return false;
            } else {
                return true;
            }

        }catch (SQLException e){
            System.out.println("Update rest adress nono");
            System.out.println(e);
        }

        return false;
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
        if (!restaurant.getEvaluations().isEmpty()){
            restaurant.getEvaluations().removeAll(restaurant.getEvaluations());
        }

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
    // it would also make great sense to declare some final variable for the column name, so that they can be easily reused.
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

    public Restaurant createRestaurant(String name, String description, String website, String street, City city, RestaurantType type){
        try {
            PreparedStatement getPK = connection.prepareStatement(
                    "SELECT SEQ_RESTAURANTS.NEXTVAL FROM DUAL"
            );

            ResultSet resPK = getPK.executeQuery();
            int pk = -1;
            if (resPK.next()){
                pk = resPK.getInt("NEXTVAL");
            }

            Restaurant restaurant = new Restaurant(pk, name, description, website, street, city, type);
            insert(restaurant);
            activeRestaurant.put(restaurant.getId(), restaurant);
            return restaurant;
        }catch (SQLException e){
            System.out.println(e);
            System.out.println("Create rest no good");
        }
        return null;
    }
}
