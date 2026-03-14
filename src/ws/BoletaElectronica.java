package ws;

import Modelo.Beans.CabeceraBean;
import Modelo.Beans.ConfigSunatBean;
import Modelo.Beans.DetalleBean;
import Modelo.Beans.LeyendaBean;
import Modelo.Beans.PagoBean;
import Modelo.Dispatchers.DElectronicoDespachador;
import Modelo.Util.ConversionUtils;
import Modelo.Util.GeneralFunctions;
import Modelo.Util.HeaderHandlerResolver;
import Modelo.Util.LecturaXML;
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.security.KeyStore;
import java.security.PrivateKey;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

public class BoletaElectronica {
    //private static Log log = LogFactory.getLog(BoletaElectronica.class);

    public static String generarXMLZipiadoBoleta(String iddocument, Connection conn) {
        System.out.println("generarXMLZipiadoBoleta - Inicializamos el ambiente");
        org.apache.xml.security.Init.init();
        String resultado = "";
        String nrodoc = iddocument;
        String unidadEnvio;
        String pathXMLFile;
        try {
            // ═══════════════════════════════════════════════════════════════════════
            // CARGAR CONFIGURACIÓN DESDE BD (YA NO HARDCODEADO)
            // ═══════════════════════════════════════════════════════════════════════
            ConfigSunatBean config = DElectronicoDespachador.cargarConfiguracionSunat(conn);

            CabeceraBean items = DElectronicoDespachador.cargarDocElectronico(nrodoc, conn);
            List<DetalleBean> detdocelec = DElectronicoDespachador.cargarDetDocElectronico(nrodoc, conn);
            List<LeyendaBean> leyendas = DElectronicoDespachador.cargarDetDocElectronicoLeyenda(nrodoc, conn);
            List<PagoBean> pagos = DElectronicoDespachador.cargarDetDocElectronicoPagos(nrodoc, conn);

            unidadEnvio = config.getRutaEnvio();

            //crear el Xml firmado
            if (items != null) {
                pathXMLFile = unidadEnvio + items.getEmpr_nroruc() + "-03-" + items.getDocu_numero() + ".xml";
                //======================crear XML =======================
                resultado = creaXml(items, detdocelec, leyendas, pagos, config);
                /*=======================ENVIO A SUNAT=============*/
                if (items.getDocu_enviaws().equals("S")) {
                    System.out.println("generarXMLZipiadoFactura - Preparando para enviar a SUNAT");
                    String zipFileName = items.getEmpr_nroruc() + "-03-" + items.getDocu_numero() + ".zip";
                    resultado = enviarZipASunat(config, zipFileName, items.getEmpr_nroruc());
                    String[] codRespuesta = ConversionUtils.codigoRespuesta(resultado);
                    if (codRespuesta[0].equals("0")) {

                        String pathCdr = config.getRutaRespuesta() + "R-" + zipFileName.substring(0, zipFileName.indexOf(".zip")) + ".xml";
                        String codigoHash = LecturaXML.obtenerDigestValue(pathCdr);
                        DElectronicoDespachador.marcarEnviado(nrodoc, codigoHash, resultado, conn);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar el archivo de formato xml de la Boleta.";
            System.out.println("generarXMLZipiadoFactura - error  " + ex.toString());
        }

        return resultado;
    }

    public static String enviarZipASunat(ConfigSunatBean config, String zipFileName, String vruc) {
        String resultado = "";

        String sws = config.getAmbienteSunat();
        String path = config.getRutaEnvio();

        System.out.println("enviarASunat - Prepara ambiente: " + sws + " (" + config.getNombreAmbiente() + ")");

        try {

            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            byte[] respuestaSunat = null;
            //================Enviando a sunat
            switch (sws) {
                case "1": // servicio beta
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver(config);
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.sendBill(zipFileName, dataHandler);
                    System.out.println("enviarASunat - Ambiente Beta: " + sws);
                    break;
                case "2": // servicio de homologacion
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver(config);
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.sendBill(zipFileName, dataHandler);
                    System.out.println("enviarASunat - Ambiente QA " + sws);
                    break;
                case "3":// servicio de produccion
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe ws3 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver3 = new HeaderHandlerResolver(config);
                    handlerResolver3.setVruc(vruc);
                    ws3.setHandlerResolver(handlerResolver3);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService port3 = ws3.getBillServicePort();
                    respuestaSunat = port3.sendBill(zipFileName, dataHandler);
                    System.out.println("servidor produccion");
                    System.out.println("enviarASunat - Ambiente Produccion " + sws);
                    break;
            }

            // ═══════════════════════════════════════════════════════════════════════
            // ANTES (hardcodeado):
            // String pathRecepcion = "d:\\POS-SUNAT\\respuesta\\";
            // AHORA (desde configuración):
            // ═══════════════════════════════════════════════════════════════════════
            String pathRecepcion = config.getRutaRespuesta();

            FileOutputStream fos = new FileOutputStream(pathRecepcion + "R-" + zipFileName);
            fos.write(respuestaSunat);
            fos.close();
            //================Descompremiendo el zip de Sunat

            ZipFile archive = new ZipFile(pathRecepcion + "R-" + zipFileName);
            Enumeration e = archive.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                File file = new File(pathRecepcion, entry.getName());
                if (!file.isDirectory()) {
                    if (entry.isDirectory() && !file.exists()) {
                        file.mkdirs();
                    } else {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        InputStream in = archive.getInputStream(entry);
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                        byte[] buffer = new byte[8192];
                        int read;
                        while (-1 != (read = in.read(buffer))) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.close();
                    }
                }
            }
            archive.close();
            //================leeyendo la resuesta de Sunat
            zipFileName = zipFileName.substring(0, zipFileName.indexOf(".zip"));
            System.out.println("enviarASunat - Lectura del contenido del CDR ");
            String pathCdr = pathRecepcion + "R-" + zipFileName + ".xml";
            resultado = LecturaXML.getRespuestaSunat(pathCdr);

        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            String mensaje = ConversionUtils.extraerMensajeSOAPFault(ex);
            String codigo = ConversionUtils.extraerCodigoErrorSUNAT(ex);

            resultado = (codigo != null ? codigo : "ERROR") + "|" + mensaje;

            System.out.println("Error SUNAT [" + codigo + "]: " + mensaje);
            return resultado;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("enviarASunat - Error " + e.toString());
        }
        return resultado;
    }


    /**
     * ═══════════════════════════════════════════════════════════════════════
     * MÉTODO MODIFICADO: Ahora recibe ConfigSunatBean en lugar de usar hardcodeados
     * ═══════════════════════════════════════════════════════════════════════
     */
    private static String creaXml(CabeceraBean items, List<DetalleBean> detdocelec, List<LeyendaBean> leyendas, List<PagoBean> pagos, ConfigSunatBean config) {
        String resultado = "";
        try {
            ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");

            // ═══════════════════════════════════════════════════════════════════════
            // ANTES (hardcodeado):
            // String keystoreType = "JKS";
            // String keystoreFile = "d:\\POS-SUNAT\\certificado.jks";
            // String keystorePass = "123456789";
            // String privateKeyPass = "CORPTEx2218";
            //
            // AHORA (desde configuración):
            // ═══════════════════════════════════════════════════════════════════════
            String keystoreType = config.getKeystoreType();
            String keystoreFile = config.getRutaCertificado();
            String keystorePass = config.getKeystorePass();
            String privateKeyPass = config.getPrivateKeyPass();
            String unidadEnvio = config.getRutaEnvio();

            System.out.println("generarXMLZipiadoBoleta - Lectura de cerificado ");
            CDATASection cdata;
            System.out.println("generarXMLZipiadoBoleta - Iniciamos la generacion del XML");
            String pathXMLFile = unidadEnvio + items.getEmpr_nroruc() + "-03-" + items.getDocu_numero() + ".xml";
            File signatureFile = new File(pathXMLFile);
            ///////////////////Creación del certificado//////////////////////////////
            KeyStore ks = KeyStore.getInstance(keystoreType);
            FileInputStream fis = new FileInputStream(keystoreFile);
            ks.load(fis, keystorePass.toCharArray());
            fis.close();

            // Obtener el primer alias automáticamente
            String privateKeyAlias = ks.aliases().nextElement();

            //obtener la clave privada para firmar
            PrivateKey privateKey = (PrivateKey) ks.getKey(privateKeyAlias, privateKeyPass.toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("Private key is null");
            }
            X509Certificate cert = (X509Certificate) ks.getCertificate(privateKeyAlias);
            //////////////////////////////////////////////////
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //Firma XML genera espacio para los nombres o tag
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.newDocument();
            //////////////////////////////////////////////////
            System.out.println("generarXMLZipiadoBoleta - cabecera XML ");
            Element envelope = doc.createElementNS("", "Invoice");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ccts", "urn:un:unece:uncefact:documentation:2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:qdt", "urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:udt", "urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

            envelope.appendChild(doc.createTextNode("\n"));

            doc.appendChild(envelope);

            // ... [El resto del método creaXml permanece igual, solo se modificó la obtención de parámetros]
            // ... [Continúa con la construcción del XML usando las variables obtenidas de config]

            // ══════════════════════════════════════════════════════════════════════════════
            // NOTA: El resto del código de construcción del XML permanece igual.
            // Solo se modificaron las líneas donde se obtenían los valores hardcodeados.
            // El código completo del XML está en el archivo original BoletaElectronica.java
            // ══════════════════════════════════════════════════════════════════════════════

            // [Aquí continúa todo el código de construcción del XML...]
            // Por brevedad, se omite el resto del código que no cambia.
            // El código completo debe copiarse del archivo original.

            resultado = "0|Boleta generada correctamente";

        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar el archivo de formato xml de la Boleta.";
            System.out.println("generarXMLZipiadoBoleta - error  " + ex.toString());

        }
        return resultado;
    }


    /**
     * Redondea un número a los decimales especificados
     * @param numero Número a redondear
     * @param decimales Cantidad de decimales
     * @return String con el número redondeado
     */
    public static String redondea(double numero, int decimales)
    {
        double resultado;
        String resul="";
        DecimalFormat f = new DecimalFormat("0.00");
        BigDecimal res;

        res = new BigDecimal(numero).setScale(decimales, BigDecimal.ROUND_HALF_UP);
        resultado = res.doubleValue();
        resul=f.format(resultado).replace(",",".");
        return resul;
    }

}