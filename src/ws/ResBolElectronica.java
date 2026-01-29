package ws;



import Modelo.Beans.CabeceraBean;
import Modelo.Beans.DetalleBean;
import Modelo.Beans.LeyendaBean;
import Modelo.Dispatchers.DElectronicoDespachador;
import Modelo.Util.GeneralFunctions;
import Modelo.Util.HeaderHandlerResolver;
import Modelo.Util.LecturaXML;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;

import java.security.KeyStore;
import java.security.PrivateKey;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import pe.gob.sunat.service.StatusResponse;

public class ResBolElectronica {
    private static Log log = LogFactory.getLog(ResBolElectronica.class);
//    static Clscontrolador Ctrl = new Clscontrolador();

//    static String documento="";
    public static String generarXMLZipiadoBoleta(String iddocument, Connection conn) { 
        log.info("generarXMLZipiadoBoleta - Inicializamos el ambiente");
        org.apache.xml.security.Init.init();
        String resultado[] = new String [2];
        String res="";
        String nrodoc = iddocument;//"943270";// request.getParameter("nrodoc");68
        String unidadEnvio; // = Util.getPathZipFilesEnvio();
        String pathXMLFile;
        try {
            CabeceraBean items = DElectronicoDespachador.cargarDocElectronico(nrodoc, conn);
//            List<DetalleBean> detdocelec = DElectronicoDespachador.cargarDetDocElectronico(nrodoc, conn);
//            List<LeyendaBean> leyendas = DElectronicoDespachador.cargarDetDocElectronicoLeyenda(nrodoc, conn);
            List<CabeceraBean> resitems = DElectronicoDespachador.ResumenDiario("N","03", conn);
            //String nrodoc = iddocument;//"943317";// request.getParameter("nrodoc");
            
            log.info("generarXMLZipiadoBoleta - Extraemos datos para preparar XML ");
             unidadEnvio = "d:\\envio\\";
            log.info("generarXMLZipiadoBoleta - Ruta de directorios " + unidadEnvio);
            log.info("generarXMLZipiadoBoleta - Iniciamos cabecera ");
            //crear el Xml firmado
            if (items != null) {
                pathXMLFile = unidadEnvio + items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().toString().replace("-","")+"-1" + ".xml";
//                documento=items.getDocu_numero();
//======================crear XML =======================
                res = creaXml(items,resitems, unidadEnvio,conn);
                /*=======================ENVIO A SUNAT=============*/
                if (items.getDocu_enviaws().equals("S")) {
                    log.info("generarXMLZipiadoBoleta - Preparando para enviar a SUNAT");
                    resultado = enviarZipASunat(unidadEnvio, items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().toString().replace("-","")+"-1"+ ".zip", items.getEmpr_nroruc());
                    System.out.println("El resultado 03 es "+resultado[0]);
                    System.out.println("El resultado 03 es "+resultado[1]);
                    if(resultado[1].equals("nulo")){
                       // actualEnviado(docu_codigo,resultado[0], resultado[1],"L","E",id);
                       System.out.println( "Hubo problemas de conexión a Internet o a los servidores de la SUNAT, intente enviar el comprobante luego");
                    }else if(resultado[1].length()>0){
//                        String sql0="update resumenboletas set cdr_nota='"+resultado[1]+"'  where id='"+id+"';";
//                        control.ejecutar(sql0);
                        pedirStatus(unidadEnvio, items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().toString().replace("-","") + "-1"+".zip", items.getEmpr_nroruc(),resultado[1]);
//                        actualEnviado(detdocelec,resultado[0], resultado[1],"L","E",id,estado);
                        System.out.println( "Se envió la Operació a SUNAT Correctamente");
                        
                        
                    }else{
                        System.out.println( "Hubo problemas de conexión a Internet o a los servidores de la SUNAT, intente enviar el comprobante luego");
                    }
                    
                } else {
                    /*este caso de boleta no se envia al sunat*/
                    log.info("generarXMLZipiadoBoleta - No se envia a SUNAT");
                    res = "0|El Comprobante numero " + items.getDocu_numero() + ", ha sido aceptado.";
                    
                }

                //resultado = "termino de generar el archivo xml de la Boleta Electronica";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            res = "0100|Error al generar el archivo de formato xml de la Boleta.";
            log.error("generarXMLZipiadoBoleta - error  " + ex.toString());
        }
//
//        try {
//            LecturaXML.guardarProcesoEstado(nrodoc, "O", resultado.split("\\|", 0), conn);
//        } catch (SQLException ex) {
//            Logger.getLogger(BolElectronica.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return res;
    }

//    public static String enviarZipASunat(String path, String zipFileName, String vruc) {
    public static String[] enviarZipASunat(String path, String zipFileName, String vruc) {
        String resultado[] =new String [2];
        resultado[0]="";
        String sws = "3";
        log.info("enviarASunat - Prepara ambiente: " + sws+"/"+zipFileName);
        try {

            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
//            byte[] respuestaSunat = null;
            String respuestaSunat = null;
            //================Enviando a sunat
            switch (sws) {
                case "1":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver();
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.sendSummary(zipFileName, dataHandler);
                    log.info("enviarASunat - Ambiente Beta: " + sws);
                    break;
                case "2":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver();
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.sendSummary(zipFileName, dataHandler);
                    log.info("enviarASunat - Ambiente QA " + sws);
                    break;
                case "3":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe ws3 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver3 = new HeaderHandlerResolver();
                    handlerResolver3.setVruc(vruc);
                    ws3.setHandlerResolver(handlerResolver3);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService port3 = ws3.getBillServicePort();
                    respuestaSunat = port3.sendSummary(zipFileName, dataHandler);
//                    respuestaSunat = port3.sendBill(zipFileName, dataHandler);
                    log.info("enviarASunat - Ambiente Produccion " + sws);
                    break;
            }

//            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
//            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            //================Grabando la respuesta de sunat en archivo ZIP solo si es nulo
            //*String pathRecepcion = "d:\\envio\\";
            //*FileOutputStream fos = new FileOutputStream(pathRecepcion + "R-" + zipFileName);
            //*fos.write(respuestaSunat);
            //*fos.close();
            //================Descompremiendo el zip de Sunat
            //*log.info("enviarASunat - Descomprimiendo CDR " + pathRecepcion + "R-" + zipFileName);
//*            ZipFile archive = new ZipFile(pathRecepcion + "R-" + zipFileName);
//*            Enumeration e = archive.entries();
//*            while (e.hasMoreElements()) {
//*                ZipEntry entry = (ZipEntry) e.nextElement();
//*                File file = new File(pathRecepcion, entry.getName());
//*                if (!file.isDirectory()) {
//*                    if (entry.isDirectory() && !file.exists()) {
//*                        file.mkdirs();
//*                    } else {
//*                        if (!file.getParentFile().exists()) {
//*                            file.getParentFile().mkdirs();
//*                       }
//*                        InputStream in = archive.getInputStream(entry);
//*                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
//*
//*                        byte[] buffer = new byte[8192];
//*                        int read;
//*                        while (-1 != (read = in.read(buffer))) {
//*                            out.write(buffer, 0, read);
//*                        }
//*                        in.close();
//*                        out.close();
//*                    }
//*                }
//*            }
            //*archive.close();
            //================leeyendo la resuesta de Sunat
            //*zipFileName = zipFileName.substring(0, zipFileName.indexOf(".zip"));
            //*log.info("enviarASunat - Lectura del contenido del CDR ");
            //*resultado = LecturaXML.getRespuestaSunat(pathRecepcion + "R-" + zipFileName + ".xml");
            System.out.println("==>El envio del Zip a sunat fue exitoso");
            log.info("enviarASunat - Envio a Sunat Exitoso ");
            resultado[0]=zipFileName;
            if(respuestaSunat==null){
               resultado[1]="nulo";
           }else{
               resultado[1]=respuestaSunat;
           }
           
//            Ctrl.estadodecompelectr(documento);
        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            System.out.println(ex.toString());
            //log.error("enviarASunat - Error " + ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("enviarASunat - Error " + e.toString());
        }
        System.out.println("Resultado:: "+resultado[0]+" / "+resultado[1]);
        return resultado;
    }
    
    public static String[] pedirStatus(String path, String zipFileName, String vruc, String tiket) {
        String resultado[] = new String [2];
        resultado[0]="";
        String sws = "3";
        log.info("enviarASunat - Prepara ambiente: " + sws);
        try {

            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            StatusResponse respuestaSunat = null;
            //================Enviando a sunat
            switch (sws) {
                case "1":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver();
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.getStatus(tiket);
                    log.info("enviarASunat - Ambiente Beta: " + sws);
                    break;
                case "2":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver();
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.getStatus(tiket);
                    log.info("enviarASunat - Ambiente QA " + sws);
                    break;
                case "3":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe ws3 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver3 = new HeaderHandlerResolver();
                    handlerResolver3.setVruc(vruc);
                    ws3.setHandlerResolver(handlerResolver3);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService port3 = ws3.getBillServicePort();
                    respuestaSunat = port3.getStatus(tiket);
                    log.info("enviarASunat - Ambiente Produccion " + sws);
                    log.info(handlerResolver3);
                    break;
            }

//            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
//            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            //================Grabando la respuesta de sunat en archivo ZIP solo si es nulo
            String pathRecepcion = "d:\\envio\\";
            FileOutputStream fos = new FileOutputStream(pathRecepcion + "R-" + zipFileName);
            fos.write(respuestaSunat.getContent());
            fos.close();
            //================Descompremiendo el zip de Sunat
            log.info("enviarASunat - Descomprimiendo CDR " + pathRecepcion + "R-" + zipFileName);
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
            log.info("enviarASunat - Lectura del contenido del CDR ");
            resultado[1] = LecturaXML.getRespuestaSunat(pathRecepcion + "R-" + zipFileName + ".xml");
            System.out.println("==>El envio del Zip a sunat fue exitoso");
            log.info("enviarASunat - Envio a Sunat Exitoso ");
        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            System.out.println(ex.toString());
            //log.error("enviarASunat - Error " + ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("enviarASunat - Error " + e.toString());
        }
        
        System.out.println("Resoltado 02: "+resultado[0]);
        return resultado;
    }

    private static String creaXml(CabeceraBean items,List<CabeceraBean> iteracion, String unidadEnvio,Connection conn) {
        String resultado = "";
        try {
            ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");
            //Parametros del keystore
           
     
            
            //ventanilla
            String keystoreType = "JKS";
            String keystoreFile = "d:\\envio\\certificado.jks";
            String keystorePass = "Rojo1234";//password creador para el almacen
            String privateKeyAlias = "||USO TRIBUTARIO|| NEGOCIACIONES SUDA EIRL CDT 20601441102";
            String privateKeyPass = "Rojo1234";// password del certificado
            String certificateAlias = "||USO TRIBUTARIO|| NEGOCIACIONES SUDA EIRL CDT 20601441102";    
     
            log.info("generarXMLZipiadoBoleta - Lectura de cerificado ");
            CDATASection cdata;
            log.info("generarXMLZipiadoBoleta - Iniciamos la generacion del XML");
            String pathXMLFile = unidadEnvio + items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().toString().replace("-","")+"-1" + ".xml";
            File signatureFile = new File(pathXMLFile);
            ///////////////////Creación del certificado//////////////////////////////
            KeyStore ks = KeyStore.getInstance(keystoreType);
            FileInputStream fis = new FileInputStream(keystoreFile);
            ks.load(fis, keystorePass.toCharArray());
            //obtener la clave privada para firmar
            PrivateKey privateKey = (PrivateKey) ks.getKey(privateKeyAlias, privateKeyPass.toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("Private key is null");
            }
            X509Certificate cert = (X509Certificate) ks.getCertificate(certificateAlias);
            //////////////////////////////////////////////////
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //Firma XML genera espacio para los nombres o tag
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.newDocument();
            ////////////////////////////////////////////////// 
            log.info("generarXMLZipiadoBoleta - cabecera XML ");
            Element envelope = doc.createElementNS("", "SummaryDocuments");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns", "urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
//            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ccts", "urn:un:unece:uncefact:documentation:2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:qdt", "urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:udt", "urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2");
//            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            envelope.appendChild(doc.createTextNode("\n"));
            //doc.appendChild(doc.createComment(" Preamble "));
            doc.appendChild(envelope);
            //doc.appendChild(doc.createComment(" Postamble "));

            Element UBLExtensions = doc.createElementNS("", "ext:UBLExtensions");
            envelope.appendChild(UBLExtensions);
            Element UBLExtension2 = doc.createElementNS("", "ext:UBLExtension");
            UBLExtension2.appendChild(doc.createTextNode("\n"));
            Element ExtensionContent2 = doc.createElementNS("", "ext:ExtensionContent");
            ExtensionContent2.appendChild(doc.createTextNode("\n"));
//            2do grupo
            Element UBLExtension = doc.createElementNS("", "ext:UBLExtension");
            envelope.appendChild(UBLExtension);
            Element ExtensionContent = doc.createElementNS("", "ext:ExtensionContent");
            envelope.appendChild(ExtensionContent);

//   

            //El baseURI es la URI que se utiliza para anteponer a URIs relativos
            String BaseURI = signatureFile.toURI().toURL().toString();
            //Crea un XML Signature objeto desde el documento, BaseURI and signature algorithm (in this case RSA)
            //XMLSignature sig = new XMLSignature(doc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA); Cadena URI que se ajusta a la sintaxis URI y representa el archivo XML de entrada
//            XMLSignature sig = new XMLSignature(doc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA);
            XMLSignature sig = new XMLSignature(doc,BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA);
//            sig.getId();
            ExtensionContent.appendChild(sig.getElement());
//            System.out.println(sig.getElement().getAttribute("Id"));
            UBLExtension.appendChild(ExtensionContent);
            UBLExtensions.appendChild(UBLExtension);
//            UBLExtensions.appendChild(UBLExtension2);
//            UBLExtension2.appendChild(Extension|Content2);
//            ExtensionContent2.appendChild(AdditionalInformation);

//bloque1
            Element UBLVersionID = doc.createElementNS("", "cbc:UBLVersionID");
            envelope.appendChild(UBLVersionID);
            UBLVersionID.appendChild(doc.createTextNode("2.0"));

            Element CustomizationID = doc.createElementNS("", "cbc:CustomizationID");
            envelope.appendChild(CustomizationID);
            CustomizationID.appendChild(doc.createTextNode("1.1"));

            Element ID5 = doc.createElementNS("", "cbc:ID");
            envelope.appendChild(ID5);
            ID5.appendChild(doc.createTextNode("RC-"+items.getDocu_fecha().toString().replace("-","")+"-1"));

            Element ReferenceDate = doc.createElementNS("", "cbc:ReferenceDate");
            envelope.appendChild(ReferenceDate);
            ReferenceDate.appendChild(doc.createTextNode(items.getDocu_fecha().toString()));
            
            Element IssueDate = doc.createElementNS("", "cbc:IssueDate");
            envelope.appendChild(IssueDate);
            IssueDate.appendChild(doc.createTextNode(items.getDocu_fecha().toString()));
        
//bloque2 cac:Signature--------------------------------------------------------
            Element Signature = doc.createElementNS("", "cac:Signature");
            envelope.appendChild(Signature);
            Signature.appendChild(doc.createTextNode("\n"));

            Element ID6 = doc.createElementNS("", "cbc:ID");
            Signature.appendChild(ID6);
//            ID6.appendChild(doc.createTextNode(items.getEmpr_nombrecomercial().trim()));
            ID6.appendChild(doc.createTextNode("RC-"+items.getDocu_fecha().toString().replace("-","")+"-1"));

            Element SignatoryParty = doc.createElementNS("", "cac:SignatoryParty");
            Signature.appendChild(SignatoryParty);
            SignatoryParty.appendChild(doc.createTextNode("\n"));

            Element PartyIdentification = doc.createElementNS("", "cac:PartyIdentification");
            SignatoryParty.appendChild(PartyIdentification);
            PartyIdentification.appendChild(doc.createTextNode("\n"));

            Element ID7 = doc.createElementNS("", "cbc:ID");
            PartyIdentification.appendChild(ID7);
            ID7.appendChild(doc.createTextNode(items.getEmpr_nroruc().trim()));

            Element PartyName = doc.createElementNS("", "cac:PartyName");
            SignatoryParty.appendChild(PartyName);
            PartyName.appendChild(doc.createTextNode("\n"));

            Element Name = doc.createElementNS("", "cbc:Name");
            PartyName.appendChild(Name);
            cdata = doc.createCDATASection(items.getEmpr_razonsocial().trim());
            Name.appendChild(cdata);

            Element DigitalSignatureAttachment = doc.createElementNS("", "cac:DigitalSignatureAttachment");
            Signature.appendChild(DigitalSignatureAttachment);
            DigitalSignatureAttachment.appendChild(doc.createTextNode("\n"));

            Element ExternalReference = doc.createElementNS("", "cac:ExternalReference");
            DigitalSignatureAttachment.appendChild(ExternalReference);
            ExternalReference.appendChild(doc.createTextNode("\n"));

            Element URI = doc.createElementNS("", "cbc:URI");
            ExternalReference.appendChild(URI);
//            URI.appendChild(doc.createTextNode(items.getEmpr_nroruc().trim()));
            URI.appendChild(doc.createTextNode("RC-"+items.getDocu_fecha().toString().replace("-","")+"-1"));
//bloque3 cac:AccountingSupplierParty-----------------------------------------

            Element AccountingSupplierParty = doc.createElementNS("", "cac:AccountingSupplierParty");
            envelope.appendChild(AccountingSupplierParty);
            AccountingSupplierParty.appendChild(doc.createTextNode("\n"));

            Element CustomerAssignedAccountID = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
            AccountingSupplierParty.appendChild(CustomerAssignedAccountID);
            CustomerAssignedAccountID.appendChild(doc.createTextNode(items.getEmpr_nroruc().trim()));
//
            Element AdditionalAccountID = doc.createElementNS("", "cbc:AdditionalAccountID");
            AccountingSupplierParty.appendChild(AdditionalAccountID);
            AdditionalAccountID.appendChild(doc.createTextNode(items.getEmpr_tipodoc().trim()));
//***********************************************************
            Element Party = doc.createElementNS("", "cac:Party");
            AccountingSupplierParty.appendChild(Party);
            Party.appendChild(doc.createTextNode("\n"));


            Element PartyLegalEntity = doc.createElementNS("", "cac:PartyLegalEntity");
            Party.appendChild(PartyLegalEntity);//se anade al grupo party
            PartyLegalEntity.appendChild(doc.createTextNode("\n"));

            Element RegistrationName = doc.createElementNS("", "cbc:RegistrationName");
            PartyLegalEntity.appendChild(RegistrationName);//se anade al grupo Country
            cdata = doc.createCDATASection(items.getEmpr_razonsocial().trim());
            RegistrationName.appendChild(cdata);
            
// eseseses           Element RegistrationAddress = doc.createElementNS("", "cbc:RegistrationAddress");
//            PartyLegalEntity.appendChild(RegistrationAddress);//se anade al grupo Country
//            RegistrationName.appendChild(doc.createTextNode("\n"));
            
//            Element AddressTypeCode = doc.createElementNS("","cbc:AddressTypeCode");
//            RegistrationAddress.appendChild(AddressTypeCode);
//            AddressTypeCode.appendChild(doc.createTextNode(items.getEmpr_ubigeo()));
// bloque4
            int fume=0;
            for(CabeceraBean ITERACION : iteracion){
            Element SummaryDocumentsLine = doc.createElementNS("", "sac:SummaryDocumentsLine");
            envelope.appendChild(SummaryDocumentsLine);
            SummaryDocumentsLine.appendChild(doc.createTextNode("\n"));
            
            Element LineID = doc.createElementNS("", "cbc:LineID");
            SummaryDocumentsLine.appendChild(LineID);//se anade al grupo AccountingCustomerParty
            LineID.appendChild(doc.createTextNode((fume+1)+""));
            
            Element DocumentTypeCode = doc.createElementNS("", "cbc:DocumentTypeCode");
            SummaryDocumentsLine.appendChild(DocumentTypeCode);//se anade al grupo AccountingCustomerParty
            DocumentTypeCode.appendChild(doc.createTextNode(ITERACION.getDocu_tipodocumento().trim()));
            
            Element ID = doc.createElementNS("", "cbc:ID");
            SummaryDocumentsLine.appendChild(ID);//se anade al grupo AccountingCustomerParty
            ID.appendChild(doc.createTextNode(ITERACION.getDocu_numero().trim()));
            
            Element AccountingCustomerParty = doc.createElementNS("", "cac:AccountingCustomerParty");
            SummaryDocumentsLine.appendChild(AccountingCustomerParty);//se anade al grupo AccountingCustomerParty
            AccountingCustomerParty.appendChild(doc.createTextNode("\n"));
            
            Element CustomerAssignedAccountID1 = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
            AccountingCustomerParty.appendChild(CustomerAssignedAccountID1);//se anade al grupo AccountingCustomerParty
            CustomerAssignedAccountID1.appendChild(doc.createTextNode(ITERACION.getClie_numero().trim()));
            
            Element AdditionalAccountID1 = doc.createElementNS("", "cbc:AdditionalAccountID");
            AccountingCustomerParty.appendChild(AdditionalAccountID1);//se anade al grupo AccountingCustomerParty
            AdditionalAccountID1.appendChild(doc.createTextNode(ITERACION.getClie_tipodoc().trim()));
            
            Element Status = doc.createElementNS("", "cac:Status");
            SummaryDocumentsLine.appendChild(Status);//se anade al grupo AccountingCustomerParty
            Status.appendChild(doc.createTextNode("\n"));
            
            Element ConditionCode = doc.createElementNS("", "cbc:ConditionCode");
            Status.appendChild(ConditionCode);//se anade al grupo AccountingCustomerParty
           // ConditionCode.appendChild(doc.createTextNode("3"));// codigo 3 para dar de baja una boleta
            ConditionCode.appendChild(doc.createTextNode("1")); // codigo 1 para dar de alta una boleta
            
            Element TotalAmount  = doc.createElementNS("", "sac:TotalAmount");
            TotalAmount.setAttributeNS(null, "currencyID","PEN");
            SummaryDocumentsLine.appendChild(TotalAmount);//se anade al grupo AccountingCustomerParty
            TotalAmount.appendChild(doc.createTextNode(ITERACION.getDocu_total()+""));
            
            Element BillingPayment  = doc.createElementNS("", "sac:BillingPayment");
            SummaryDocumentsLine.appendChild(BillingPayment);//se anade al grupo AccountingCustomerParty
            BillingPayment.appendChild(doc.createTextNode("\n"));
           
            Element PaidAmount  = doc.createElementNS("", "cbc:PaidAmount");
            PaidAmount.setAttributeNS(null, "currencyID","PEN");
            BillingPayment.appendChild(PaidAmount);//se anade al grupo AccountingCustomerParty
            PaidAmount.appendChild(doc.createTextNode(redondea(( Double.parseDouble(ITERACION.getDocu_total()+"")-Double.parseDouble(ITERACION.getDocu_igv()+"")),2)));
            
            Element InstructionID  = doc.createElementNS("", "cbc:InstructionID");
            BillingPayment.appendChild(InstructionID);//se anade al grupo AccountingCustomerParty
            InstructionID.appendChild(doc.createTextNode("01"));
            
            Element TaxTotal  = doc.createElementNS("", "cac:TaxTotal");
            SummaryDocumentsLine.appendChild(TaxTotal);//se anade al grupo AccountingCustomerParty
            TaxTotal.appendChild(doc.createTextNode("\n"));
            
            Element TaxAmount  = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmount.setAttributeNS(null, "currencyID","PEN");
            TaxTotal.appendChild(TaxAmount);//se anade al grupo AccountingCustomerParty
            TaxAmount.appendChild(doc.createTextNode(ITERACION.getDocu_igv()+""));
            
            Element TaxSubtotal  = doc.createElementNS("", "cac:TaxSubtotal");
            TaxTotal.appendChild(TaxSubtotal);//se anade al grupo AccountingCustomerParty
            TaxSubtotal.appendChild(doc.createTextNode("\n"));
            
            Element TaxAmount1  = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmount1.setAttributeNS(null, "currencyID","PEN");
            TaxSubtotal.appendChild(TaxAmount1);//se anade al grupo AccountingCustomerParty
            TaxAmount1.appendChild(doc.createTextNode(ITERACION.getDocu_igv()+""));
           
            Element TaxCategory  = doc.createElementNS("", "cac:TaxCategory");
            TaxSubtotal.appendChild(TaxCategory);//se anade al grupo AccountingCustomerParty
            TaxCategory.appendChild(doc.createTextNode("\n"));
            
            Element TaxScheme  = doc.createElementNS("", "cac:TaxScheme");
            TaxCategory.appendChild(TaxScheme);//se anade al grupo AccountingCustomerParty
            TaxScheme.appendChild(doc.createTextNode("\n"));
            
            Element ID1  = doc.createElementNS("", "cbc:ID");
            TaxScheme.appendChild(ID1);//se anade al grupo AccountingCustomerParty
            ID1.appendChild(doc.createTextNode("1000"));
            
            Element Name1  = doc.createElementNS("", "cbc:Name");
            TaxScheme.appendChild(Name1);
            Name1.appendChild(doc.createTextNode("IGV"));
            
            Element TaxTypeCode  = doc.createElementNS("", "cbc:TaxTypeCode");
            TaxScheme.appendChild(TaxTypeCode);
            TaxTypeCode.appendChild(doc.createTextNode("VAT"));
            
            if(ITERACION.getDocu_otrostributos()!=0.00){
            Element TaxTotalICBPER  = doc.createElementNS("", "cac:TaxTotal");
            SummaryDocumentsLine.appendChild(TaxTotal);
            TaxTotal.appendChild(doc.createTextNode("\n"));
            
            Element TaxAmountICBPER  = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmountICBPER.setAttributeNS(null, "currencyID","PEN");
            TaxTotalICBPER.appendChild(TaxAmountICBPER);
            TaxAmountICBPER.appendChild(doc.createTextNode(ITERACION.getDocu_otrostributos()+""));
            
            Element TaxSubtotalicbper  = doc.createElementNS("", "cac:TaxSubtotal");
            TaxTotalICBPER.appendChild(TaxSubtotalicbper);//se anade al grupo AccountingCustomerParty
            TaxSubtotalicbper.appendChild(doc.createTextNode("\n"));
            
            Element TaxAmount1icbper  = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmount1icbper.setAttributeNS(null, "currencyID","PEN");
            TaxSubtotalicbper.appendChild(TaxAmount1icbper);//se anade al grupo AccountingCustomerParty
            TaxAmount1icbper.appendChild(doc.createTextNode(ITERACION.getDocu_otrostributos()+""));
            
            
            Element TaxCategoryICBPER  = doc.createElementNS("", "cac:TaxCategory");
            TaxSubtotalicbper.appendChild(TaxCategoryICBPER);//se anade al grupo AccountingCustomerParty
            TaxCategoryICBPER.appendChild(doc.createTextNode("\n"));
            
            Element TaxSchemeIcbper  = doc.createElementNS("", "cac:TaxScheme");
            TaxCategoryICBPER.appendChild(TaxSchemeIcbper);//se anade al grupo AccountingCustomerParty
            TaxSchemeIcbper.appendChild(doc.createTextNode("\n"));
            
            Element ID1ICBPER  = doc.createElementNS("", "cbc:ID");
            TaxSchemeIcbper.appendChild(ID1ICBPER);//se anade al grupo AccountingCustomerParty
            ID1ICBPER.appendChild(doc.createTextNode("7152"));
            
            Element Name1ICBPER  = doc.createElementNS("", "cbc:Name");
            TaxSchemeIcbper.appendChild(Name1ICBPER);
            Name1ICBPER.appendChild(doc.createTextNode("ICBPER"));
            
            Element TaxTypeCodeICBPER  = doc.createElementNS("", "cbc:TaxTypeCode");
            TaxSchemeIcbper.appendChild(TaxTypeCodeICBPER);
            TaxTypeCodeICBPER.appendChild(doc.createTextNode("OTH"));
            
            
            
            }
            
            
            
            
            fume++;
//                JOptionPane.showMessageDialog(null, fume);
            }

            
           
            log.info("generarXMLZipiadoBoleta - Prepara firma digital ");
            sig.setId("Sign"+items.getEmpr_nroruc());
            sig.addKeyInfo(cert);
            {
                Transforms transforms = new Transforms(doc);
                transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
                sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
            }
            {
                //Firmar el documento
                log.info("generarXMLZipiadoBoleta - firma el XML ");
                sig.sign(privateKey);
            }
            //--------------------fin de construccion del xml---------------------
            ///*combinacion de firma y construccion xml////
            FileOutputStream f = new FileOutputStream(signatureFile);
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            //tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");
            //Writer out = new StringWriter();
            StreamResult sr = new StreamResult(f);
            tf.transform(new DOMSource(doc), sr);
            sr.getOutputStream().close();

            log.info("generarXMLZipiadoBoleta - XML creado " + pathXMLFile);
            //====================== CREAR ZIP PARA EL ENVIO A SUNAT =======================
            resultado = GeneralFunctions.crearZip2(items, unidadEnvio, signatureFile);

        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar el archivo de formato xml de la Boleta.";
            log.error("generarXMLZipiadoBoleta - error  " + ex.toString());

        }
        return resultado;
    }
   public static String redondea(double numero, int decimales) 
{ 
  double resultado;String resul="";
    DecimalFormat f = new DecimalFormat("0.00");
  BigDecimal res;

  res = new BigDecimal(numero).setScale(decimales, BigDecimal.ROUND_HALF_DOWN);
  resultado = res.doubleValue();
  resul=f.format(resultado).replace(",",".");
  return resul; 
}
}
