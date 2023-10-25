package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.time.LocalDate;
import java.sql.Date;

public class BasicEvalMapper implements IMapper<BasicEvaluation> {
    //should this class even implement this interface, given that it likely will never use the findbyId and findAll method ?
    //more likely, it'll use a findForRestaurant(Restaurant r/int pkRestaurant) type of stuff...
    //granted, the findByID is useful for the fetchback thing I do in insert/update...

    private Connection connection;

    private RestaurantMapper restaurantMapper;

    public BasicEvalMapper(){
        this.connection = DbConnection.createConnection();
        this.restaurantMapper = new RestaurantMapper();
    }

    @Override
    public BasicEvaluation findByID(int pk) {
        try{
            PreparedStatement query = connection.prepareStatement("SELECT * FROM LIKES WHERE numero = ?");
            query.setInt(1, pk);
            ResultSet resultSet = query.executeQuery();
            if (resultSet.next()){
                boolean liked;
                if (resultSet.getString("APPRECIATION") == "T"){
                    liked = true;
                }else {liked = false;}

                return new BasicEvaluation(
                        resultSet.getInt("NUMERO"),
                        resultSet.getDate("DATE_EVAL").toLocalDate(),
                        restaurantMapper.findByID(resultSet.getInt("FK_REST")),
                        liked,
                        resultSet.getString("ADRESSE_IP")
                );
            }
            else {
                System.out.println("Aucune eval trouvée.");
                return null;
            }
        }catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public HashSet<BasicEvaluation> findAll() {
        try {
            PreparedStatement query = connection.prepareStatement("SELECT * FROM LIKES");
            ResultSet resultSet = query.executeQuery();

            HashSet<BasicEvaluation> retour = new HashSet(); //ideally here, I'd make "retour" a Set, and initialize it as a HashSet.
            while (resultSet.next()){
                boolean liked;
                if (resultSet.getString("APPRECIATION") == "T"){
                    liked = true;
                }else {liked = false;}

                BasicEvaluation base = new BasicEvaluation(
                        resultSet.getInt("NUMERO"),
                        resultSet.getDate("DATE_EVAL").toLocalDate(),
                        restaurantMapper.findByID(resultSet.getInt("FK_REST")),
                        liked,
                        resultSet.getString("ADRESSE_IP")
                );
                retour.add(base);
            }
            return retour;
        }catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    @Override
    public BasicEvaluation insert(BasicEvaluation basicEvaluation) {
        try {
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO LIKES (APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST) " +
                            "VALUES (?, ?, ?, ? )"
            );
            String liked;
            if (basicEvaluation.isLikeRestaurant()){
                liked = "T";
            }else {liked = "F";}

            insert.setString(1, liked);
            insert.setDate(2, Date.valueOf(basicEvaluation.getVisitDate()));
            insert.setString(3, basicEvaluation.getIpAddress());
            insert.setInt(4, basicEvaluation.getRestaurant().getId());

            insert.executeQuery();
            System.out.println("Insert ok");
        }catch (SQLException e){
            System.out.println("Insert fucked up");
            System.out.println(e);
        }
        try {
            PreparedStatement fetchback = connection.prepareStatement("SELECT * FROM LIKES WHERE numero = (SELECT MAX(numero) FROM LIKES);");
            //because of how the trigger and the sequence for the PK of the tabke work, the PK of the latest inserted is always the biggest one.
            ResultSet resultSet = fetchback.executeQuery();

            if (resultSet.next()){
                boolean liked;
                if (resultSet.getString("APPRECIATION") == "T"){
                    liked = true;
                }else {liked = false;}

                BasicEvaluation base = new BasicEvaluation(
                        resultSet.getInt("NUMERO"),
                        resultSet.getDate("DATE_EVAL").toLocalDate(),
                        restaurantMapper.findByID(resultSet.getInt("FK_REST")),
                        liked,
                        resultSet.getString("ADRESSE_IP")
                );
                return base; //by doing this we assure that the city returned has an ID
            }
        }catch (SQLException e){
            System.out.println("FetchBack fucked up");
            System.out.println(e);
        }
        return null;
    }

    @Override
    public BasicEvaluation update(BasicEvaluation basicEvaluation) {
        try {
            PreparedStatement update = connection.prepareStatement(
                    "INSERT INTO LIKES (APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST) " +
                            "VALUES (?, ?, ?, ? )"
            );
            String liked;
            if (basicEvaluation.isLikeRestaurant()){
                liked = "T";
            }else {liked = "F";}

            update.setString(1, liked);
            update.setDate(2, Date.valueOf(basicEvaluation.getVisitDate()));
            update.setString(3, basicEvaluation.getIpAddress());
            update.setInt(4, basicEvaluation.getRestaurant().getId());

            int check = update.executeUpdate();
            if (check==0){
                throw new SQLException();
            } else {
                return findByID(basicEvaluation.getId());
            }

        }catch (SQLException e){
            System.out.println("update fucked up");
            System.out.println(e);
            return null;
        }
    }

    @Override
    public void delete(BasicEvaluation basicEvaluation) {
        try {
            PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM LIKES WHERE numero = ?");
            deleteQuery.setInt(1, basicEvaluation.getId());

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
