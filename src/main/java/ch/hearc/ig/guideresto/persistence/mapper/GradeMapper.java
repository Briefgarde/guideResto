package ch.hearc.ig.guideresto.persistence.mapper;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.persistence.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public final class GradeMapper implements IMapper<Grade>{

    private Connection connection;

    private static GradeMapper INSTANCE;

    private GradeMapper(){
        this.connection = DbConnection.getConnection();
    }

    public static GradeMapper getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE = new GradeMapper();
        }
        return INSTANCE;
    }

    @Override
    public Grade findByID(int pk) {
        return null;
    }

    @Override
    public HashSet<Grade> findAll() {
        return null;
    }

    @Override
    public Grade insert(Grade grade) {
        return null;
    }

    @Override
    public Grade update(Grade grade) {
        return null;
    }

    @Override
    public void delete(Grade grade) {

    }

    public Set<Grade> findForCompleteEval (CompleteEvaluation eval){
        try {
            PreparedStatement query = connection.prepareStatement(
              "SELECT * FROM NOTE WHERE FK_COMM = ?"
            );
            query.setInt(1, eval.getId());

            ResultSet resultSet = query.executeQuery();

            Set<Grade> retour = new HashSet<>();
            while (resultSet.next()){
                retour.add(new Grade(
                        resultSet.getInt("numero"),
                        resultSet.getInt("note"),
                        eval,
                        EvaluationCriteriaMapper.getINSTANCE().findByID(resultSet.getInt("FK_CRIT"))
                        // it is ok to create a criteria here, since it's the place it's bound to be used ?
                        //OF COURSE NOT
                        //NOW I STILL CAN'T update a criteria globally... //JAAAAAAAAAAAAAAAAAAAAAAAAA HAHAHAHA I don't like this...
                ));
            }
            return retour;
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }

    public Set<Grade> insertForCompleteEval (CompleteEvaluation evaluation){
        //Just for the record, I do NOT think I'm on the correct path here.
        try {
            Set<Grade> retour = new HashSet<>();
            for (Grade grade : evaluation.getGrades()){
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO NOTES (note, FK_COMM, FK_CRIT)" +
                                "VALUES (?, ?, ?)"
                );
                insert.setInt(1, grade.getGrade());
                insert.setInt(2, grade.getEvaluation().getId());
                insert.setInt(3, grade.getCriteria().getId());
                insert.executeUpdate();

                PreparedStatement fetchback = connection.prepareStatement(
                        "SELECT * FROM notes WHERE NUMERO = (SELECT MAX(NUMERO) FROM NOTES)"
                );
                ResultSet resultSet = fetchback.executeQuery();
                while (resultSet.next()){
                    retour.add(new Grade(
                            resultSet.getInt("numero"),
                            resultSet.getInt("note"),
                            evaluation,
                            EvaluationCriteriaMapper.getINSTANCE().findByID(resultSet.getInt("FK_CRIT"))
                            // it is ok to create a criteria here, since it's the place it's bound to be used ?
                            //OF COURSE NOT
                            //NOW I STILL CAN'T update a criteria globally... //JAAAAAAAAAAAAAAAAAAAAAAAAA HAHAHAHA I don't like this...
                    ));
                }
            }
            return retour;


        }catch (SQLException e){
            System.out.println(e);
        }
        return null;
    }
}
