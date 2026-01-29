 package quartz;

import factory.ConnectionPool;
import Modelo.Beans.CabeceraBean;
import Modelo.Dispatchers.DElectronicoDespachador;
import ws.BoletaElectronica;
import ws.DarBajaDocElectronica;
import ws.FacturaElectronica;
import ws.ResBolElectronica;
import java.sql.Connection;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ws.DarBajaDocElectronica;
import ws.NotaCred;
import ws.ResBolElectronica;


/**
 * Clase que implementa la tarea final a ejecutar
 *
 * @author Luisinho
 *
 */
public class DisparaGeneratorws {

private static Log log = LogFactory.getLog(DisparaGeneratorws.class);

    public synchronized static void generator() {
        //log.info("generator");
        Connection conn = null;
        try {
            //log.info("generator - conectar a MySQl");
            conn = ConnectionPool.obtenerConexionMysql();
            System.out.println("__Ejecución disparar " + new Date().toString());
            //log.info("generator - buscar pendientes");
            CabeceraBean item = DElectronicoDespachador.pendienteDocElectronico(conn);
            String iddoc = null;
            String tipodoc = null;
            String result = "x";
            if (item != null && item.getDocu_tipodocumento().trim() != null) {
                log.info("generator - Existe pendiente");
                iddoc = item.getDocu_codigo()+"";
                //tipodoc = Integer.valueOf(item.getDocu_tipodocumento()).toString().trim();
                tipodoc = item.getDocu_tipodocumento().trim();//  BOLETA 03   FACTURAS 01    NOTAS CRED 07

                //System.out.println("___Preparando el doc. " + Util.equivalenciaTipoDocNombre(tipodoc) + " " + iddoc);
                System.out.println("___Preparando el doc. " + tipodoc + " " + iddoc);
                log.info("generator - extrayendo datos");
                switch (tipodoc) {
                    case "03":
                        
                        //result = BoletaElectronica.generarXMLZipiadoBoleta(iddoc, conn);
                        //result = ResBolElectronica.generarXMLZipiadoBoleta(iddoc, conn);
                       //System.out.println("ENVIAR LA BOLETA : "+item.getDocu_numero());
                        break;
                    case "01":
                       // System.out.println("ENVIAR LA FACTURA : "+item.getDocu_numero());
                        result = FacturaElectronica.generarXMLZipiadoFactura(iddoc, conn);
                       //result = DarBajaDocElectronica.generarXMLZipiadoBoleta(iddoc, conn);
                            
                       
                        break;
                    case "07":
                        result = NotaCred.generarXMLZipiadoNotaCred(iddoc, conn);
//                        
                        break;
                    default:
                        result = "0100|Operacion nula";
                        
                        break;

                }
            }
            if (!result.equals("x")) {
                System.out.println("Resultado " + result);
            }

        } catch (Exception er) {
            er.printStackTrace();
                log.error("generator - error " + er.toString());
            
        } finally {
            ConnectionPool.closeConexion(conn);
        }
    }

}
