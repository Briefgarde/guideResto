package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.DbConnection;
import oracle.jdbc.proxy.annotation.Pre;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public final class CompleteEvalMapper {

    private Connection connection;

    private static CompleteEvalMapper INSTANCE;

    //In this use case, a registre for Complete Eval doesn't feel necessary, as they can not be modified.

    private CompleteEvalMapper(){
        this.connection = DbConnection.createConnection();
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
                    retour.getGrades().add(new Grade(
                            resultSetNote.getInt("numero"),
                            resultSetNote.getInt("NOTE"),
                            retour,
                            EvaluationCriteriaMapper.getINSTANCE().findByID(resultSetNote.getInt("FK_CRIT"))
                    ));
                }

                retour.getRestaurant().getEvaluations().add(retour);
                return retour;
            }


        }catch (SQLException e){
            System.out.println(e);
        }

        return null;
    }


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
                CompleteEvaluation eval = new CompleteEvaluation(
                        resultSetComms.getInt("numero"),
                        resultSetComms.getDate("DATE_EVAL").toLocalDate(),
                        RestaurantMapper.getINSTANCE().findByID(resultSetComms.getInt("FK_REST")),
                        resultSetComms.getString("COMMENTAIRE"),
                        resultSetComms.getString("NOM_UTILISATEUR")
                );
                PreparedStatement queryNote = connection.prepareStatement(
                        "SELECT * FROM NOTES WHERE FK_COMM = ?"
                );
                queryNote.setInt(1, resultSetComms.getInt("NUMERO"));
                ResultSet resultSetNote = queryNote.executeQuery();

                while (resultSetNote.next()){
                    eval.getGrades().add(new Grade(
                            resultSetNote.getInt("NUMERO"),
                            resultSetNote.getInt("NOTE"),
                            eval,
                            EvaluationCriteriaMapper.getINSTANCE().findByID(resultSetNote.getInt("FK_CRIT"))
                    ));
                }
                completeEvaluations.add(eval);
            }
            return completeEvaluations;
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }


    public CompleteEvaluation insert(CompleteEvaluation completeEvaluation) {
        try{
            connection.setAutoCommit(false);
            //since this works on more than one table, auto commit is disabled in case we need to roll back stuff.
            PreparedStatement insertIntoCommentaire = connection.prepareStatement(
                    "INSERT INTO COMMENTAIRES (date_eval, commentaire, NOM_UTILISATEUR, fk_rest)" +
                            "VALUES (?, ?, ?, ?)"
            );
            insertIntoCommentaire.setDate(1, Date.valueOf(completeEvaluation.getVisitDate()));
            insertIntoCommentaire.setString(2, completeEvaluation.getComment());
            insertIntoCommentaire.setString(3, completeEvaluation.getUsername());
            insertIntoCommentaire.setInt(4, completeEvaluation.getRestaurant().getId());

            insertIntoCommentaire.executeUpdate();
            //send the complete eval to DB to get ID

            PreparedStatement fetchback = connection.prepareStatement(
                    "SELECT * FROM COMMENTAIRES WHERE NUMERO = (SELECT MAX(NUMERO) FROM COMMENTAIRES)"
            );
            ResultSet resultSet = fetchback.executeQuery();

            CompleteEvaluation retour = new CompleteEvaluation(
                    resultSet.getInt("numero"),
                    resultSet.getDate("DATE_EVAL").toLocalDate(),
                    RestaurantMapper.getINSTANCE().findByID(resultSet.getInt("FK_REST")),
                    resultSet.getString("COMMENTAIRE"),
                    resultSet.getString("NOM_UTILISATEUR")
            );

            Set<Grade> grades = GradeMapper.getINSTANCE().findForCompleteEval(retour);

            for (Grade grade : grades){
                retour.getGrades().add(grade);
            }
            return retour;




        }catch (SQLException e){
            System.out.println("yeah something exploded");
            System.out.println(e);
            try {
                connection.rollback();
            }catch (SQLException ex){
                System.out.println("rollback machine BBBBBroke");
                System.out.println(ex);
            }
        }

        return null;
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
}
