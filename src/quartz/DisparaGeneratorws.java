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

public class DisparaGeneratorws {

    public synchronized static void generator() {

        Connection conn = null;
        try {
            //System.out.println("generator - conectar a MySQl");
            conn = ConnectionPool.obtenerConexionMysql();
            System.out.println("__Ejecución disparar " + new Date().toString());
            //System.out.println("generator - buscar pendientes");
            CabeceraBean item = DElectronicoDespachador.pendienteDocElectronico(conn);
            String tipodoc = null;
            String iddoc = null;
            String result = "x";
            if (item != null) {
                System.out.println("generator - Existe pendiente");
                iddoc = item.getDocu_codigo().trim();
                tipodoc = item.getDocu_tipodocumento().trim();//  BOLETA 03   FACTURAS 01    NOTAS CRED 07

                System.out.println("Tipo Doc: " + tipodoc + " NoFactu: " + iddoc);

                switch (tipodoc) {
                    case "03":
                        System.out.println("ENVIAR LA BOLETA : "+iddoc);
                        result = BoletaElectronica.generarXMLZipiadoBoleta( iddoc, conn);
                        //result = ResBolElectronica.generarXMLZipiadoBoleta(iddoc, conn);
                        System.out.println("RESULTADO BOLETA : "+result);
                        break;
                    case "01":
                        System.out.println("ENVIAR LA FACTURA : "+item.getDocu_numero());
                       // System.out.println("ENVIAR LA FACTURA : "+item.getDocu_numero());
                        result = FacturaElectronica.generarXMLZipiadoFactura(iddoc, conn);
                        result = DarBajaDocElectronica.generarXMLZipiadoBoleta(iddoc, conn);
                        System.out.println("RESULTADO  FACTURA : "+result);

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
                System.out.println("Resultado => " + result);
            }

        } catch (Exception er) {
            er.printStackTrace();
            System.out.println("generator - error " + er.toString());
            
        } finally {
            ConnectionPool.closeConexion(conn);
        }
    }

}
