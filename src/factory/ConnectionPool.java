/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory;




import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.activation.DataSource;

/**
 *
 * @author luis_
 */
public class ConnectionPool {
     public static Connection obtenerConexion(String fuente) throws SQLException{
        DataSource ds=null;
        Connection conexion=null;
        try{
           DriverManager.registerDriver(new com.mysql.jdbc.Driver());

            conexion = DriverManager.getConnection (
                "jdbc:mysql://localhost/factura2023","root", "luis");
            
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
