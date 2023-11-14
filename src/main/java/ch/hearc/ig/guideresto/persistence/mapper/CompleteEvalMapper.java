package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public final class CompleteEvalMapper {

    private Connection connection;

    private Map<Integer, CompleteEvaluation> activeEvaluation = new LinkedHashMap<>();

    private Map<Integer, Grade> activeGrade = new LinkedHashMap<>();
    private static CompleteEvalMapper INSTANCE;

    //In this use case, a registre for Complete Eval doesn't feel necessary, as they can not be modified.

    private CompleteEvalMapper(){
        this.connection = DbConnection.getConnection();
    }



    public static CompleteEvalMapper getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new CompleteEvalMapper();
        }
        return INSTANCE;
    }

    public CompleteEvaluation findByID(int pk) {
        try {
            PreparedStatement queryCommentaire = connection.prepareStatement("SELECT * FROM COMMENTAIRES WHERE numero = ?");
            queryCommentaire.setInt(1, pk);
            PreparedStatement queryNote = connection.prepareStatement("SELECT * FROM NOTES WHERE FK_COMM = ?");
            queryNote.setInt(1, pk);

            ResultSet resultSetCommentaire = queryCommentaire.executeQuery();
            ResultSet resultSetNote = queryNote.executeQuery();

            if (resultSetCommentaire.next()){
                CompleteEvaluation retour = new CompleteEvaluation(
                        resultSetCommentaire.getInt("numero"),
                        resultSetCommentaire.getDate("DATE_EVAL").toLocalDate(),
                        RestaurantMapper.getINSTANCE().findByID(resultSetCommentaire.getInt("FK_REST")),
                        resultSetCommentaire.getString("COMMENTAIRE"),
                        resultSetCommentaire.getString("NOM_UTILISATEUR")
                );

                while (resultSetNote.next()){
                    retour.getGrades().add(getGradeFromRS(resultSetNote, retour));
                }

                retour.getRestaurant().getEvaluations().add(retour);
                return retour;
            }


        }catch (SQLException e){
            System.out.println(e);
        }

        return null;
    }


    // I don't know why I though it was a good idea to make it a list of Complete eval btw
    public List<CompleteEvaluation> findForRestaurant(Restaurant restaurant){
        try {
            PreparedStatement queryComms = connection.prepareStatement(
                    "SELECT * FROM restaurants r\n" +
                            "INNER JOIN commentaires c \n" +
                            "    on c.FK_REST = r.NUMERO\n" +
                            "WHERE r.numero = ?"
            );
            queryComms.setInt(1, restaurant.getId());
            ResultSet resultSetComms = queryComms.executeQuery();
            List<CompleteEvaluation> completeEvaluations = new ArrayList<>();

            while (resultSetComms.next()){
                CompleteEvaluation eval = getEvalFromRS(resultSetComms);

                if (!activeEvaluation.containsKey(eval.getId())){
                    activeEvaluation.put(eval.getId(), eval);
                } // ^^ this is ultimately useless as I don't use it later on...
                PreparedStatement queryNote = connection.prepareStatement(
                        "SELECT * FROM NOTES WHERE FK_COMM = ?"
                );
                queryNote.setInt(1, resultSetComms.getInt("NUMERO"));
                ResultSet resultSetNote = queryNote.executeQuery();

                while (resultSetNote.next()){
                    Grade grade = getGradeFromRS(resultSetNote, eval);
                    eval.getGrades().add(grade);
                }
                completeEvaluations.add(eval);
            }
            return completeEvaluations;
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }
    public boolean insert(CompleteEvaluation eval){
        try {
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO COMMENTAIRES (NUMERO, DATE_EVAL, COMMENTAIRE, NOM_UTILISATEUR, FK_REST)" +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            insert.setInt(1, eval.getId());
            insert.setDate(2, Date.valueOf(eval.getVisitDate()));
            insert.setString(3, eval.getComment());
            insert.setString(4, eval.getUsername());
            insert.setInt(5, eval.getRestaurant().getId());

            //quick check to see if it did create the thing correctly
            if (insert.executeUpdate() > 0){
                activeEvaluation.put(eval.getId(), eval);
                return true;
            }
        }
        catch (SQLException e){
            System.out.println(e);
            System.out.println("INSERT complete eval no good");
        }
        return false;
    }
        public void deleteForRestaurant(Restaurant restaurant){
        try {
            PreparedStatement getCommsPK = connection.prepareStatement(
                    "SELECT NUMERO FROM COMMENTAIRES WHERE FK_REST = ?"
            );
            getCommsPK.setInt(1, restaurant.getId());
            ResultSet resultSetPK = getCommsPK.executeQuery();
            while (resultSetPK.next()){
                //This delete every notes related to the comments
                PreparedStatement deleteNote = connection.prepareStatement(
                        "DELETE FROM NOTES WHERE FK_COMM = ?"
                );
                deleteNote.setInt(1, resultSetPK.getInt("NUMERO"));
                deleteNote.executeUpdate();
            }
            //once every related notes are deleted, all of the comms can be deleted too
            PreparedStatement deleteComms = connection.prepareStatement(
                    "DELETE FROM commentaires WHERE FK_REST = ?"
            );
            deleteComms.setInt(1, restaurant.getId());
            deleteComms.executeUpdate();

        }catch (SQLException e){
            System.out.println("Delete completeEval for Rest nnonono no good");
            System.out.println(e);
        }
    }

    public CompleteEvaluation createEvaluation(Restaurant restaurant, String username, String comment){
        try {
            PreparedStatement getPK = connection.prepareStatement(
                    "SELECT SEQ_EVAL.NEXTVAL from dual"
            );
            ResultSet resPK = getPK.executeQuery();
            int pk = -1;
            if (resPK.next()){
                pk = resPK.getInt("NEXTVAL");
            }

            CompleteEvaluation completeEvaluation = new CompleteEvaluation(
                    pk,
                    LocalDate.now(),
                    restaurant,
                    comment,
                    username);
            insert(completeEvaluation);

            return completeEvaluation;
        }catch (SQLException e){
            System.out.println(e);
            System.out.println("Create Full eval no good");
        }
        return null;
    }

    public Grade createGradeForEval(int note, CompleteEvaluation eval, EvaluationCriteria criteria){
        try {
            PreparedStatement getPK = connection.prepareStatement(
                    "SELECT SEQ_NOTES.NEXTVAL FROM dual"
            );
            ResultSet resPK = getPK.executeQuery();
            int pk = -1;
            if (resPK.next()){
                pk = resPK.getInt("NEXTVAL");
            }
            Grade grade = new Grade(pk, note, eval, criteria);
            insertGrade(grade);
            return grade;

        }catch (SQLException e){
            System.out.println(e);
            System.out.println("Create grade no good");
        }
        return null;
    }

    private boolean insertGrade (Grade grade){
        try {
            PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO notes (NUMERO, NOTE, FK_COMM, FK_CRIT)" +
                            "VALUES (?, ?, ?, ?)"
            );
            insert.setInt(1, grade.getId());
            insert.setInt(2, grade.getGrade());
            insert.setInt(3, grade.getEvaluation().getId());
            insert.setInt(4, grade.getCriteria().getId());

            if (insert.executeUpdate() > 0){
                activeGrade.put(grade.getId(), grade);
                return true;
            }
        }catch (SQLException e){
            System.out.println(e);
            System.out.println("insert grade no good");
        }

        return false;
    }

    private CompleteEvaluation getEvalFromRS(ResultSet rs) throws SQLException{
        return new CompleteEvaluation(
                rs.getInt("numero"),
                rs.getDate("DATE_EVAL").toLocalDate(),
                RestaurantMapper.getINSTANCE().findByID(rs.getInt("FK_REST")),
                rs.getString("COMMENTAIRE"),
                rs.getString("NOM_UTILISATEUR")
        );
    }
    private Grade getGradeFromRS(ResultSet rs, CompleteEvaluation eval) throws SQLException{
        return new Grade(
                rs.getInt("NUMERO"),
                rs.getInt("NOTE"),
                eval,
                EvaluationCriteriaMapper.getINSTANCE().findByID(rs.getInt("FK_CRIT"))
        );
    }
}
