package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestaurantTypeMapper implements IMapper<RestaurantType> {
    private final Connection connection;

    public RestaurantTypeMapper(){
        this.connection = DbConnection.createConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public RestaurantType findByID(int pk) {
        try {
            PreparedStatement query = connection.prepareStatement("SELECT * FROM TYPES_GASTRONOMIQUES WHERE numero = ?");
            query.setInt(1, pk);
            ResultSet resultSet = query.executeQuery();
            if (resultSet.next()){
                return new RestaurantType(
                        resultSet.getInt("numero"),
                        resultSet.getString("LIBELLE"),
                        resultSet.getString("DESCRIPTION")
                );
            }
            else {
                System.out.println("Aucun type de restaurant trouvé.");
                return null;
            }
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }

    @Override
    public HashSet<RestaurantType> findAll() {
        try {
            PreparedStatement query = connection.prepareStatement("SELECT * FROM TYPES_GASTRONOMIQUES");
            ResultSet resultSet = query.executeQuery();

            HashSet<RestaurantType> retour = new HashSet(); //ideally here, I'd make "retour" a Set, and initialize it as a HashSet.
            while (resultSet.next()){
                RestaurantType restaurantType = new RestaurantType(
                        resultSet.getInt("numero"),
                        resultSet.getString("LIBELLE"),
                        resultSet.getString("DESCRIPTION")
                );
                retour.add(restaurantType);
            }
            return retour;
        }catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    @Override
    public RestaurantType insert(RestaurantType type) {
        try {
            PreparedStatement insert = connection.prepareStatement("INSERT INTO TYPES_GASTRONOMIQUES (DESCRIPTION, LIBELLE) VVALUES (?, ?)");
            insert.setString(1, type.getDescription());
            insert.setString(2, type.getLabel());
            insert.executeQuery();
            System.out.println("Insert ok");
        }catch (SQLException e){
            System.out.println("Insert fucked up");
            System.out.println(e);
        }

        try {
            PreparedStatement fetchback = connection.prepareStatement("SELECT * FROM TYPES_GASTRONOMIQUES WHERE numero = (SELECT MAX(numero) FROM TYPES_GASTRONOMIQUES);");
            ResultSet resultSet = fetchback.executeQuery();

            if (resultSet.next()){
                RestaurantType type1 = new RestaurantType(
                        resultSet.getInt("numero"),
                        resultSet.getString("LIBELLE"),
                        resultSet.getString("DESCRIPTION")
                );
                return type1; //this probably isn't a reallllllly good way to do it, but if the thing works, it will go through a return statement without ever
                //returning null or activating any of the catch thing
            }
        }catch (SQLException e){
            System.out.println("FetchBack fucked up");
            System.out.println(e);
        }
        return null;
    }

    @Override
    public RestaurantType update(RestaurantType type) {
        try {
            PreparedStatement update = connection.prepareStatement("UPDATE TYPES_GASTRONOMIQUES SET DESCRIPTION = ?, LIBELLE = ? WHERE numero = ?");
            update.setString(1, type.getDescription());
            update.setString(2, type.getLabel());
            update.setInt(3, type.getId());

            int check = update.executeUpdate();
            if (check==0){
                throw new SQLException();
            } else {
                return findByID(type.getId());
            }

        }catch (SQLException e){
            System.out.println("i thru up");
            System.out.println(e);
            return null;
        }
    }

    @Override
    public void delete(RestaurantType type) {
        try {
            PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM TYPES_GASTRONOMIQUES WHERE numero = ?");
            deleteQuery.setInt(1, type.getId());

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
