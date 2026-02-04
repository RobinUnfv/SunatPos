
package factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import oracle.jdbc.OracleDriver;

public class ConnectionPool {

    private static String host = System.getenv("COMPUTERNAME");
    private static String puerto = "1521";
    private static String sid = "BDNX1";
    private static String usuario = "LLE";
    private static String password = "YVL";

     public static Connection obtenerConexion(String fuente) throws SQLException{
        //DataSource ds = null;
        Connection conexion = null;
        try{
            /*
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            conexion = DriverManager.getConnection ("jdbc:mysql://localhost/factura2023","root", "luis");
            */
            DriverManager.registerDriver(new OracleDriver());
            conexion = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":" + puerto + ":" + sid, usuario, password);

        }catch(Exception ex){
            throw new SQLException(ex);
        }
        return conexion;
    }
    public static Connection obtenerConexionMysql() throws SQLException{
        return obtenerConexion("jdbc/genxml");
    }
    
    public static void closeConexion(Connection conexion) {
        try {
            if (conexion != null) {
                conexion.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
