package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;


import java.sql.*;
import java.util.*;

public final class CityMapper{
    private Connection connection;

    private static CityMapper INSTANCE;

    private Map<Integer, City> activeCity = new LinkedHashMap<>();
    
    private CityMapper(){
        this.connection = DbConnection.createConnection();
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
                if (activeCity.containsKey(resultSet.getInt("NUMERO"))){
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
    public City insert(City newCity) {
        try {
            PreparedStatement insert = connection.prepareStatement("INSERT INTO villes (CODE_POSTAL, NOM_VILLE) VALUES (?, ?)");
            insert.setString(1, newCity.getZipCode());
            insert.setString(2, newCity.getCityName());
            insert.executeQuery();
            System.out.println("Insert ok"); //hahaha j'aimerai bien avoir des logs maintenant
        }catch (SQLException e){
            System.out.println("Insert fucked up");
            System.out.println(e);
        }

        try {
            PreparedStatement fetchback = connection.prepareStatement("SELECT * FROM VILLES WHERE numero = (SELECT MAX(numero) FROM VILLES);");
            //because of how the trigger and the sequence for the PK of the tabke work, the PK of the latest inserted is always the biggest one.
            ResultSet resultSet = fetchback.executeQuery();

            if (resultSet.next()){
                City city = new City(
                        resultSet.getInt("numero"),
                        resultSet.getString("CODE_POSTAL"),
                        resultSet.getString("nom_ville")
                );
                return city; //by doing this we assure that the city returned has an ID
            }
        }catch (SQLException e){
            System.out.println("FetchBack fucked up");
            System.out.println(e);
        }
        return null;
    }

    public City insert(String name, String zipcode){
        try {
            PreparedStatement getPK = connection.prepareStatement(
                    "SELECT SEQ_VILLES.nextval FROM dual"
            );
            ResultSet resPK = getPK.executeQuery();
            int pk = -1;
            if (resPK.next()){
                pk = resPK.getInt("NEXTVAL");
            } else { throw new SQLException("next val didn't work");}
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO VILLES (NUMERO, CODE_POSTAL, NOM_VILLE) VALUES (?, ?, ?)"
            );
            insert.setInt(1, pk);
            insert.setString(2, zipcode);
            insert.setString(3, name);
            insert.executeQuery();

            City ret = findByID(pk);

            return ret;

        }catch (SQLException e){
            System.out.println("CITY INSERT FUCKED UP");
        }

        return null;
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


}
