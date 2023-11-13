package ch.hearc.ig.guideresto.persistence;

import java.sql.*;
import static java.sql.DriverManager.*;

public class DbConnection {

    private static Connection connection;

    private DbConnection(){
        try {
            registerDriver(new oracle.jdbc.OracleDriver());
            String url = "jdbc:oracle:thin:@db.ig.he-arc.ch:1521:ens";
            Connection connection = DriverManager.getConnection(url, "nemo_vollert", "nemo_vollert");
            connection.setAutoCommit(true);
            this.connection = connection;
        }
        catch (SQLException sqlException){
            System.out.println(sqlException);
        }
    }
    public static Connection getConnection(){
        if (connection == null){
            new DbConnection();
        }
        return connection;
    }
    public static void dropSession(Connection connection){
        try{
            connection.close();
        }
        catch (SQLException sqlException){
            System.out.println(sqlException);
        }

    }

}
