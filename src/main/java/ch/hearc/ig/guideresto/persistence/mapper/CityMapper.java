package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.DbConnection;


import java.sql.*;
import java.util.*;

public final class CityMapper{
    private Connection connection;

    private static CityMapper INSTANCE;

    private Map<Integer, City> activeCity = new LinkedHashMap<>();
    
    private CityMapper(){
        this.connection = DbConnection.getConnection();
    }


    public static CityMapper getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new CityMapper();
        }
        return INSTANCE;
    }


    public City findByID(int pk) {
        if (!activeCity.containsKey(pk)){
            try{
                PreparedStatement query = connection.prepareStatement("SELECT * FROM VILLES WHERE numero = ?");
                query.setInt(1, pk);
                ResultSet resultSet = query.executeQuery();
                if (resultSet.next()){
                    return new City(
                            resultSet.getInt("numero"),
                            resultSet.getString("CODE_POSTAL"),
                            resultSet.getString("nom_ville")
                    );
                }
                else {
                    System.out.println("Aucune ville trouvée.");
                    return null;
                }
            }catch (SQLException e) {
                System.out.println(e);
                return null;
            }

        }
        return activeCity.get(pk);
    }

    public Set<City> findAll(){
        try {
            PreparedStatement query = connection.prepareStatement(
                    "SELECT * FROM VILLES"
            );
            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()){
                if (!activeCity.containsKey(resultSet.getInt("NUMERO"))){
                    activeCity.put(
                            resultSet.getInt("NUMERO"),
                            new City(
                                    resultSet.getInt("NUMERO"),
                                    resultSet.getString("CODE_POSTAL"),
                                    resultSet.getString("NOM_VILLE")
                            )
                    );

                }
            }
            return new HashSet<City>(activeCity.values());
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }

    private void insert(City city){
        try {
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO VILLES (NUMERO, CODE_POSTAL, NOM_VILLE)" +
                            "VALUES (?, ?, ?)"
            );
            insert.setInt(1, city.getId());
            insert.setString(2, city.getZipCode());
            insert.setString(3, city.getCityName());

            insert.executeUpdate();

        }catch (SQLException e){
            System.out.println(e);
            System.out.println("insert city no good");
        }

    }
    public City update(City city) {
        try {
            PreparedStatement update = connection.prepareStatement("UPDATE villes SET CODE_POSTAL = ?, NOM_VILLE = ? WHERE numero = ?");
            update.setString(1, city.getZipCode());
            update.setString(2, city.getCityName());
            update.setInt(3, city.getId());

            int check = update.executeUpdate();
            if (check==0){
                throw new SQLException();
            } else {
                return findByID(city.getId());
            }

        }catch (SQLException e){
            System.out.println("update fucked up");
            System.out.println(e);
            return null;
        }
    }


    public void delete(City city) {
        try {
            PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM villes WHERE numero = ?");
            deleteQuery.setInt(1, city.getId());

            int check = deleteQuery.executeUpdate();
            if (check==0){
                //executeUpdate should return 1 if the city is correctly deleted/updated. It'll return 0 if the query didn't work.
                throw new SQLException();
            }
        }catch (SQLException e){
            System.out.println("delete fucked up");
            System.out.println(e);
        }
    }

    public City createNewCity(String zipcode, String name){
        try {
            PreparedStatement getPK = connection.prepareStatement(
                    "SELECT SEQ_VILLES.NEXTVAL FROM DUAL"
            );

            ResultSet resPK = getPK.executeQuery();
            int pk = -1;
            if (resPK.next()){
                pk = resPK.getInt("NEXTVAL");
            }

            City city = new City(pk, zipcode, name);
            insert(city);
            activeCity.put(city.getId(), city);
            return city;
        }catch (SQLException e){
            System.out.println("create new city no good");
            System.out.println(e);
        }

        return null;
    }

    private City createCityFromRS(ResultSet rs) throws SQLException{
        City city = new City(
                rs.getInt("numero"),
                rs.getString("CODE_POSTAL"),
                rs.getString("nom_ville")
        );
        activeCity.put(city.getId(), city);
        return city;
    }


}
