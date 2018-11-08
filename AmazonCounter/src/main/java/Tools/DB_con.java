package Tools;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by 1 on 2017/7/13.
 */
public class DB_con {

    private static String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static String url="jdbc:sqlserver://192.168.1.178;databaseName=Sushi";
    private static String userName="sa";
    private static String password = "sa";

    static{
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Connection getCon() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, userName, password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return connection;
    }

    public PreparedStatement getPs(String sql) {

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getCon().prepareStatement(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return preparedStatement;
    }
}
