package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDate;
import java.sql.Date;

public class BasicEvalMapper{
    private Connection connection;

    private Map<Integer, BasicEvaluation> activeBaseEval = new LinkedHashMap<>();
    private static BasicEvalMapper INSTANCE;

    private BasicEvalMapper(){
        this.connection = DbConnection.getConnection();
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
                BasicEvaluation eval = getBaseEvalFromRS(resultSet);
                if (activeBaseEval.containsKey(eval.getId())){
                    retour.add(activeBaseEval.get(eval.getId()));
                } else {
                    activeBaseEval.put(eval.getId(), eval);
                    retour.add(eval);
                }
            }
            return retour;
        }catch (SQLException e){
            System.out.println("Find for res Basic fucked up");
            System.out.println(e);
        }

        return null;
    }

    //To note, the insert method here is only ever supposed to be used when creating a basic eval
    //as per the method createBasicEval below, where the Eval has a PK already.
    private void insert(BasicEvaluation basicEvaluation) {
        try {
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO LIKES (NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST)" +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            insert.setInt(1, basicEvaluation.getId());
            insert.setString(2, boolToCharLiker(basicEvaluation));
            insert.setDate(3, Date.valueOf(basicEvaluation.getVisitDate()));
            insert.setString(4, basicEvaluation.getIpAddress());
            insert.setInt(5, basicEvaluation.getRestaurant().getId());

            insert.executeUpdate();

            findByID(basicEvaluation.getId());

        }catch (SQLException e){
            System.out.println(e);
            System.out.println("Basic eval insert noooo good");
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
        return new BasicEvaluation(
                rs.getInt("NUMERO"),
                rs.getDate("DATE_EVAL").toLocalDate(),
                RestaurantMapper.getINSTANCE().findByID(rs.getInt("FK_REST")),
                charToBoolLiker(rs),
                rs.getString("ADRESSE_IP"));
    }

    public BasicEvaluation createBasicEval(Restaurant restaurant, Boolean like, String ip){
        try {
            PreparedStatement getPK = connection.prepareStatement(
                    "SELECT SEQ_EVAL.NEXTVAL from dual"
            );
            ResultSet resPK = getPK.executeQuery();
            int pk = -1;
            if (resPK.next()){
                pk = resPK.getInt("NEXTVAL");
            }

            BasicEvaluation basicEvaluation = new BasicEvaluation(pk, LocalDate.now(), restaurant, like, ip);
            insert(basicEvaluation);
            return basicEvaluation;



        }catch (SQLException e){
            System.out.println(e);
            System.out.println("No basic eval creation");
        }
        return null;
    }

    private String boolToCharLiker(BasicEvaluation basicEvaluation){
        return basicEvaluation.isLikeRestaurant() ? "T" : "F";
    }

    private boolean charToBoolLiker(ResultSet rs) throws SQLException{
        return rs.getString("APPRECIATION").equals("T") ? true : false;
    }
}
