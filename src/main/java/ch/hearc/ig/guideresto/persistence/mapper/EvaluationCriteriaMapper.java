package ch.hearc.ig.guideresto.persistence.mapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.persistence.DbConnection;

public class EvaluationCriteriaMapper implements IMapper<EvaluationCriteria> {

    private final Connection connection;
    
    public EvaluationCriteriaMapper(){
        this.connection = DbConnection.createConnection();
    }

    public Connection getConnection() {
        return connection;
    } 

    @Override
    public EvaluationCriteria findByID(int pk) {
        try{
            PreparedStatement query = connection.prepareStatement("SELECT * FROM CRITERES_EVALUATION WHERE numero = ?");
            query.setInt(1, pk);
            ResultSet resultSet = query.executeQuery();
            if (resultSet.next()){
                return new EvaluationCriteria(
                        resultSet.getInt("numero"),
                        resultSet.getString("NOM"),
                        resultSet.getString("DESCRIPTION")
                );
            }
            else {
                System.out.println("Aucun critères trouvés.");
                return null;
            }
        }catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public HashSet<EvaluationCriteria> findAll() {
        try {
            PreparedStatement query = connection.prepareStatement("SELECT * FROM CRITERES_EVALUATION");
            ResultSet resultSet = query.executeQuery();

            HashSet<EvaluationCriteria> retour = new HashSet<EvaluationCriteria>(); //ideally here, I'd make "retour" a Set, and initialize it as a HashSet.
            while (resultSet.next()){
                EvaluationCriteria evalCrit = new EvaluationCriteria(
                        resultSet.getInt("numero"),
                        resultSet.getString("NOM"),
                        resultSet.getString("DESCRIPTION")
                );
                retour.add(evalCrit);
            }
            return retour;
        }catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    @Override
    public EvaluationCriteria insert(EvaluationCriteria newEvalCrit) {
        try {
            PreparedStatement insert = connection.prepareStatement("INSERT INTO CRITERES_EVALUATION (NOM, DESCRIPTION) VVALUES (?, ?)");
            insert.setString(1, newEvalCrit.getName());
            insert.setString(2, newEvalCrit.getDescription());
            insert.executeQuery();
            System.out.println("Insert ok"); //hahaha j'aimerai bien avoir des logs maintenant
        }catch (SQLException e){
            System.out.println("Insert fucked up");
            System.out.println(e);
        }

        try {
            PreparedStatement fetchback = connection.prepareStatement("SELECT * FROM CRITERES_EVALUATION WHERE numero = (SELECT MAX(numero) FROM CRITERES_EVALUATION);");
            //because of how the trigger and the sequence for the PK of the tabke work, the PK of the latest inserted is always the biggest one.
            ResultSet resultSet = fetchback.executeQuery();

            if (resultSet.next()){
                EvaluationCriteria evaluationCriteria = new EvaluationCriteria(
                        resultSet.getInt("numero"),
                        resultSet.getString("NOM"),
                        resultSet.getString("DESCRIPTION")
                );
                return evaluationCriteria; 

            }
        }catch (SQLException e){
            System.out.println("FetchBack fucked up");
            System.out.println(e);
        }
        return null;
    }

    @Override
    public EvaluationCriteria update(EvaluationCriteria uCriteria) {
        try {
            PreparedStatement update = connection.prepareStatement("UPDATE CRITERES_EVALUATION SET NOM = ?, DESCRIPTION = ? WHERE numero = ?");
            update.setString(1, uCriteria.getName());
            update.setString(2, uCriteria.getDescription());
            update.setInt(3, uCriteria.getId());

            int check = update.executeUpdate();
            if (check==0){
                throw new SQLException();
            } else {
                return findByID(uCriteria.getId());
            }

        }catch (SQLException e){
            System.out.println("update fucked up");
            System.out.println(e);
            return null;
        }
    }

    @Override
    public void delete(EvaluationCriteria dCriteria) {
        try {
            PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM CRITERES_EVALUATION WHERE numero = ?");
            deleteQuery.setInt(1, dCriteria.getId());

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
