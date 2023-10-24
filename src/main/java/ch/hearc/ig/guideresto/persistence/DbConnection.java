package ch.hearc.ig.guideresto.persistence;

import java.sql.*;
import static java.sql.DriverManager.*;

public class DbConnection {
    public static Connection createConnection(){
        try {
            registerDriver(new oracle.jdbc.OracleDriver());
            String url = "jdbc:oracle:thin:@db.ig.he-arc.ch:1521:ens";
            Connection connection = getConnection(url, "nemo_vollert", "nemo_vollert");
            connection.setAutoCommit(true);
            return connection;
        }
        catch (SQLException sqlException){
            System.out.println(sqlException);
            return null;
        }
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
