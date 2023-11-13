package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class RestaurantTypeMapper {
    private Connection connection;

    private static RestaurantTypeMapper INSTANCE;

    private Map<Integer, RestaurantType> activeRestaurantType = new LinkedHashMap<>();

    private RestaurantTypeMapper(){
        this.connection = DbConnection.getConnection();
    }

    public static RestaurantTypeMapper getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new RestaurantTypeMapper();
        }
        return INSTANCE;
    }

    public Set<RestaurantType> findAll(){
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT * FROM TYPES_GASTRONOMIQUES"
            );
            ResultSet resultSet = query.executeQuery();
            Set<RestaurantType> restaurantTypes = new HashSet<>();
            while (resultSet.next()){
                if (!activeRestaurantType.containsKey(resultSet.getInt("NUMERO"))){
                    activeRestaurantType.put(
                            resultSet.getInt("NUMERO"),
                            new RestaurantType(
                                    resultSet.getInt("NUMERO"),
                                    resultSet.getString("LIBELLE"),
                                    resultSet.getString("DESCRIPTION")
                            )
                    );
                }
            }
        }catch (SQLException e){
            System.out.println(e);
        }
        return new HashSet<RestaurantType>(activeRestaurantType.values());
    }

    public RestaurantType findByID(int pk){
        if (!activeRestaurantType.containsKey(pk)){
            try {
                PreparedStatement query = connection.prepareStatement(
                        "SELECT * FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?"
                );
                query.setInt(1, pk);
                ResultSet resultSet = query.executeQuery();
                while (resultSet.next()){
                    if (!activeRestaurantType.containsKey(resultSet.getInt("NUMERO"))){
                        activeRestaurantType.put(
                                resultSet.getInt("NUMERO"),
                                new RestaurantType(
                                        resultSet.getInt("NUMERO"),
                                        resultSet.getString("LIBELLE"),
                                        resultSet.getString("DESCRIPTION")
                                )
                        );
                    }
                }

            }catch (SQLException e) {
                System.out.println(e);
            }

        }
        return activeRestaurantType.get(pk);
    }
}
