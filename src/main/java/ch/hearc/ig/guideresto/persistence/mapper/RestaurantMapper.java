package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class RestaurantMapper implements IMapper<Restaurant> {

    private Connection connection;

    private CityMapper cityMapper;

    private RestaurantTypeMapper restaurantTypeMapper;

    public RestaurantMapper(){
        this.connection = DbConnection.createConnection();
        this.cityMapper = new CityMapper();
        this.restaurantTypeMapper = new RestaurantTypeMapper();
    }

    @Override
    public Restaurant findByID(int pk) {
        try{
            PreparedStatement query = connection.prepareStatement("SELECT * FROM RESTAURANTS WHERE numero = ?");
            query.setInt(1, pk);
            ResultSet resultSet = query.executeQuery();
            //so on fetching the rest, we immediately grab its type and city, but not, so far, its eval.
            if (resultSet.next()){
                return new Restaurant(
                        resultSet.getInt("numero"),
                        resultSet.getString("NOM"),
                        resultSet.getString("DESCRIPTION"),
                        resultSet.getString("SITE_WEB"),
                        resultSet.getString("ADRESSE"),
                        cityMapper.findByID(resultSet.getInt("FK_VILL")),
                        restaurantTypeMapper.findByID(resultSet.getInt("FK_TYPE"))
                );
            }
            else {
                System.out.println("Aucun restaurant trouvé.");
                return null;
            }
        }catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public HashSet<Restaurant> findAll() {
        try {
            PreparedStatement query = connection.prepareStatement("SELECT * FROM RESTAURANTS");
            ResultSet resultSet = query.executeQuery();

            HashSet<Restaurant> retour = new HashSet(); //ideally here, I'd make "retour" a Set, and initialize it as a HashSet.
            while (resultSet.next()){
                Restaurant restaurant = new Restaurant(
                        resultSet.getInt("numero"),
                        resultSet.getString("NOM"),
                        resultSet.getString("DESCRIPTION"),
                        resultSet.getString("SITE_WEB"),
                        resultSet.getString("ADRESSE"),
                        cityMapper.findByID(resultSet.getInt("FK_VILL")),
                        restaurantTypeMapper.findByID(resultSet.getInt("FK_TYPE"))
                );
                retour.add(restaurant);
            }
            return retour;
        }catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    @Override
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
            return new Restaurant(
                    resultSet.getInt("numero"),
                    resultSet.getString("NOM"),
                    resultSet.getString("DESCRIPTION"),
                    resultSet.getString("SITE_WEB"),
                    resultSet.getString("ADRESSE"),
                    cityMapper.findByID(resultSet.getInt("FK_VILL")),
                    restaurantTypeMapper.findByID(resultSet.getInt("FK_TYPE"))
            );
        }catch (SQLException e){
            System.out.println("fetchback fucked up");
            System.out.println(e);
        }
        return null;
    }

    @Override
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
                return findByID(restaurant.getId());
            }

        }catch (SQLException e){
            System.out.println("Update fucked up");
            System.out.println(e);
        }
        return null;
    }

    @Override
    public void delete(Restaurant restaurant) {
        try {
            PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM RESTAURANTS WHERE numero = ?");
            deleteQuery.setInt(1, restaurant.getId());

            int check = deleteQuery.executeUpdate();
            if (check==0){
                throw new SQLException();
            }
        }catch (SQLException e){
            System.out.println("delete fucked up");
            System.out.println(e);
        }
    }
}
