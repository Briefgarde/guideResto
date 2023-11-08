package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import javax.swing.plaf.basic.BasicIconFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;

public class BasicEvalMapper{
    //should this class even implement this interface, given that it likely will never use the findbyId and findAll method ?
    //more likely, it'll use a findForRestaurant(Restaurant r/int pkRestaurant) type of stuff...
    //granted, the findByID is useful for the fetchback thing I do in insert/update...

    private Connection connection;

    private static BasicEvalMapper INSTANCE;

    private BasicEvalMapper(){
        this.connection = DbConnection.createConnection();
    }

    public static BasicEvalMapper getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new BasicEvalMapper();
        }
        return INSTANCE;
    }

    public BasicEvaluation findByID(int pk) {
        try{
            PreparedStatement query = connection.prepareStatement("SELECT * FROM LIKES WHERE numero = ?");
            query.setInt(1, pk);
            ResultSet resultSet = query.executeQuery();
            if (resultSet.next()){

                BasicEvaluation retour = getBaseEvalFromRS(resultSet);

                retour.getRestaurant().getEvaluations().add(retour);
                return retour;

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

    public HashSet<BasicEvaluation> findAll() {
        try {
            PreparedStatement query = connection.prepareStatement("SELECT * FROM LIKES");
            ResultSet resultSet = query.executeQuery();

            HashSet<BasicEvaluation> retour = new HashSet(); //ideally here, I'd make "retour" a Set, and initialize it as a HashSet.
            while (resultSet.next()){


                BasicEvaluation base = getBaseEvalFromRS(resultSet);
                base.getRestaurant().getEvaluations().add(base);

                retour.add(base);
            }
            return retour;
        }catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    public List<BasicEvaluation> findForRestaurant(Restaurant restaurant){
        try {
            PreparedStatement findFRes = connection.prepareStatement(
                    "SELECT * FROM LIKES WHERE FK_REST = ?"
            );
            findFRes.setInt(1, restaurant.getId());

            ResultSet resultSet = findFRes.executeQuery();
            List<BasicEvaluation> retour = new ArrayList<>();
            while (resultSet.next()){
                retour.add(getBaseEvalFromRS(resultSet));
            }
            return retour;
        }catch (SQLException e){
            System.out.println("Find for res Basic fucked up");
            System.out.println(e);
        }

        return null;
    }

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
                BasicEvaluation base = getBaseEvalFromRS(resultSet);
                base.getRestaurant().getEvaluations().add(base);
                return base; //by doing this we assure that the city returned has an ID
            }
        }catch (SQLException e){
            System.out.println("FetchBack fucked up");
            System.out.println(e);
        }
        return null;
    }

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

    public void deleteForRestaurant(Restaurant restaurant){
        try {
            PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM LIKES WHERE FK_REST = ?"
            );
            delete.setInt(1, restaurant.getId());
            delete.executeUpdate();
        }catch (SQLException e){
            System.out.println(e);
        }
    }

    private BasicEvaluation getBaseEvalFromRS(ResultSet rs) throws SQLException{
        boolean liked;
        if (rs.getString("APPRECIATION").equals("T")){
            liked = true;
        }else {liked = false;}

        return new BasicEvaluation(
                rs.getInt("NUMERO"),
                rs.getDate("DATE_EVAL").toLocalDate(),
                RestaurantMapper.getINSTANCE().findByID(rs.getInt("FK_REST")),
                liked,
                rs.getString("ADRESSE_IP"));
    }
}
