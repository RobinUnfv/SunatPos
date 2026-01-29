package ws;



import Modelo.Beans.CabeceraBean;
import Modelo.Beans.DetalleBean;
import Modelo.Beans.LeyendaBean;
import Modelo.Dispatchers.DElectronicoDespachador;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

public class NotaCred {

    private static Log log = LogFactory.getLog(NotaCred.class);

//    static Parametro Ctrl = new Parametro("",0,0.00, "");
    public static String generarXMLZipiadoNotaCred(String iddocument, Connection conn) { 
        log.info("generarXMLZipiadoNotaCred - Inicializamos el ambiente");
        org.apache.xml.security.Init.init();
        String resultado = "";
        String nrodoc = iddocument;//"943270";// request.getParameter("nrodoc");68
        String unidadEnvio; // = Util.getPathZipFilesEnvio();
        String pathXMLFile;
        try {
            CabeceraBean items = DElectronicoDespachador.cargarDocElectronico(nrodoc, conn);
            List<DetalleBean> detdocelec = DElectronicoDespachador.cargarDetDocElectronico(nrodoc, conn);
            List<LeyendaBean> leyendas = DElectronicoDespachador.cargarDetDocElectronicoLeyenda(nrodoc, conn);

            //String nrodoc = iddocument;//"943317";// request.getParameter("nrodoc");
            log.info("generarXMLZipiadoNotaCred - Extraemos datos para preparar XML ");
             unidadEnvio = "d:\\envio\\";
//             unidadEnvio = hp.getRutaxml();
            log.info("generarXMLZipiadoNotaCred - Ruta de directorios " + unidadEnvio);
            log.info("generarXMLZipiadoNotaCred - Iniciamos cabecera ");
            //crear el Xml firmado
            if (items != null) {
                pathXMLFile = unidadEnvio + items.getEmpr_nroruc() + "-07-" + items.getDocu_numero() + ".xml";
                //======================crear XML =======================
                resultado = creaXml(items, detdocelec, leyendas, unidadEnvio);
                /*=======================ENVIO A SUNAT=============*/
                if (items.getDocu_enviaws().equals("S")) {
                    log.info("generarXMLZipiadoNotaCred - Preparando para enviar a SUNAT");
                    resultado = enviarZipASunat(unidadEnvio, items.getEmpr_nroruc() + "-07-" + items.getDocu_numero() + ".zip", items.getEmpr_nroruc());
                } else {
                    /*este caso de boleta no se envia al sunat*/
                    log.info("generarXMLZipiadoNotaCred - No se envia a SUNAT");
                    resultado = "0|El Comprobante numero " + items.getDocu_numero() + ", ha sido aceptado.";
//                    Ctrl.estadodecompelectr(conn,nrodoc);
                    
                }

                //resultado = "termino de generar el archivo xml de la Boleta Electronica";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar el archivo de formato xml de la NOTA DE CREDITO.";
            log.error("generarXMLZipiadoFactura - error  " + ex.toString());
        }
//
//        try {
//            LecturaXML.guardarProcesoEstado(nrodoc, "O", resultado.split("\\|", 0), conn);
//        } catch (SQLException ex) {
//            Logger.getLogger(BolElectronica.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return resultado;
    }

    public static String enviarZipASunat(String path, String zipFileName, String vruc) {
        String resultado = "";
        String sws = "3";
        log.info("enviarASunat - Prepara ambiente: " + sws);
        try {

            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            byte[] respuestaSunat = null;
            //================Enviando a sunat
            switch (sws) {
                case "1":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver();
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.sendBill(zipFileName, dataHandler);
                    log.info("enviarASunat - Ambiente Beta: " + sws);
                    break;
                case "2":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver();
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.sendBill(zipFileName, dataHandler);
                    log.info("enviarASunat - Ambiente QA " + sws);
                    break;
                case "3":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe ws3 = new pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver3 = new HeaderHandlerResolver();
                    handlerResolver3.setVruc(vruc);
                    ws3.setHandlerResolver(handlerResolver3);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService port3 = ws3.getBillServicePort();
                    respuestaSunat = port3.sendBill(zipFileName, dataHandler);
                    System.out.println("servidor produccion");
                    log.info("enviarASunat - Ambiente Produccion " + sws);
                    break;
            }

//            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
//            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            //================Grabando la respuesta de sunat en archivo ZIP solo si es nulo
            String pathRecepcion = "d:\\envio\\";
//            String pathRecepcion = hp.getRutaxml();
            FileOutputStream fos = new FileOutputStream(pathRecepcion + "R-" + zipFileName);
            fos.write(respuestaSunat);
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
            resultado = LecturaXML.getRespuestaSunat(pathRecepcion + "R-" + zipFileName + ".xml");
            System.out.println("==>El envio del Zip a sunat fue exitoso");
            log.info("enviarASunat - Envio a Sunat Exitoso ");
        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            System.out.println(ex.toString());
            //log.error("enviarASunat - Error " + ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("enviarASunat - Error " + e.toString());
        }
        return resultado;
    }

  private static String creaXml(CabeceraBean items, List<DetalleBean> detdocelec, List<LeyendaBean> leyendas, String unidadEnvio) {
        String resultado = "";
        try {
            ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");
           
            String keystoreType = "JKS";
            String keystoreFile = "d:\\envio\\certificado.jks";
            String keystorePass = "Rojo1234";
            String privateKeyAlias = "||USO TRIBUTARIO|| NEGOCIACIONES SUDA EIRL CDT 20601441102";
            String privateKeyPass = "Rojo1234";
            String certificateAlias = "||USO TRIBUTARIO|| NEGOCIACIONES SUDA EIRL CDT 20601441102";  
        
            log.info("generarXMLZipiadoNotaCred - Lectura de cerificado ");
            CDATASection cdata;
            log.info("generarXMLZipiadoNotaCred - Iniciamos la generacion del XML");
            String pathXMLFile = unidadEnvio + items.getEmpr_nroruc() + "-07-" + items.getDocu_numero() + ".xml";
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
            Element envelope = doc.createElementNS("", "CreditNote");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
//            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ccts", "urn:un:unece:uncefact:documentation:2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
//            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:qdt", "urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2");
//            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
//            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:udt", "urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2");
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
            UBLVersionID.appendChild(doc.createTextNode("2.1"));

            Element CustomizationID = doc.createElementNS("", "cbc:CustomizationID");
            envelope.appendChild(CustomizationID);
            CustomizationID.appendChild(doc.createTextNode("2.0"));

            Element ID5 = doc.createElementNS("", "cbc:ID");
            envelope.appendChild(ID5);
            ID5.appendChild(doc.createTextNode(items.getDocu_numero().trim()));

            Element IssueDate = doc.createElementNS("", "cbc:IssueDate");
            envelope.appendChild(IssueDate);
            IssueDate.appendChild(doc.createTextNode(items.getDocu_fecha()+""));
            
            Element IssueTime = doc.createElementNS("", "cbc:IssueTime");
            envelope.appendChild(IssueTime);
            IssueTime.appendChild(doc.createTextNode(items.getDocu_hora().trim()));

//            Element InvoiceTypeCode = doc.createElementNS("", "cbc:InvoiceTypeCode");
//            Attr LISUT =doc.createAttribute("listURI");
//            Attr listName =doc.createAttribute("listName");
//            Attr listAgencyName =doc.createAttribute("listAgencyName");
//            Attr listID =doc.createAttribute("listID");
//            LISUT.setValue("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
//            listName.setValue("SUNAT:Identificador de Tipo de Documento");
//            listAgencyName.setValue("PE:SUNAT");
//            listID.setValue("0101");
//            envelope.appendChild(InvoiceTypeCode);
//            InvoiceTypeCode.setAttributeNode(LISUT);
//            InvoiceTypeCode.setAttributeNode(listName);
//            InvoiceTypeCode.setAttributeNode(listAgencyName);
//            InvoiceTypeCode.setAttributeNode(listID);
//            InvoiceTypeCode.appendChild(doc.createTextNode(items.getDocu_tipodocumento().trim()));
            
            for (LeyendaBean leyenda : leyendas) {

                Element Note = doc.createElementNS("", "cbc:Note");
                Attr languageLocaleID =doc.createAttribute("languageLocaleID");
                languageLocaleID.setValue(leyenda.getLeyenda_codigo());
                envelope.appendChild(Note);
                Note.setAttributeNode(languageLocaleID);
                Note.appendChild(doc.createTextNode(leyenda.getLeyenda_texto()));
            }
            
            Element DocumentCurrencyCode = doc.createElementNS("", "cbc:DocumentCurrencyCode");
            Attr listName1 =doc.createAttribute("listName");
            Attr listAgencyName1 =doc.createAttribute("listAgencyName");
            Attr listID1 =doc.createAttribute("listID");
            listName1.setValue("Currency");
            listAgencyName1.setValue("United Nations Economic Commission for Europe");
            listID1.setValue("ISO 4217 Alpha");
            envelope.appendChild(DocumentCurrencyCode);
            DocumentCurrencyCode.setAttributeNode(listName1);
            DocumentCurrencyCode.setAttributeNode(listAgencyName1);
            DocumentCurrencyCode.setAttributeNode(listID1);
            DocumentCurrencyCode.appendChild(doc.createTextNode(items.getDocu_moneda().trim()));

//            Element LineCountNumeric = doc.createElementNS("", "cbc:LineCountNumeric");
//            envelope.appendChild(LineCountNumeric);
//            LineCountNumeric.appendChild(doc.createTextNode(detdocelec.size()+""));
//datos de la nota de credito
            Element DiscrepancyResponse = doc.createElementNS("","cac:DiscrepancyResponse");
            envelope.appendChild(DiscrepancyResponse);
            DiscrepancyResponse.appendChild(doc.createTextNode("\n"));
            
            Element ReferenceID = doc.createElementNS("", "cbc:ReferenceID");
            DiscrepancyResponse.appendChild(ReferenceID);
            ReferenceID.appendChild(doc.createTextNode(items.getDocu_tipodcocumento_numero()));//llama a la bd
            
            Element ResponseCode = doc.createElementNS("", "cbc:ResponseCode");
            DiscrepancyResponse.appendChild(ResponseCode);
            ResponseCode.appendChild(doc.createTextNode(items.getDocu_tipodcocumento_anular()));
            
            Element Description_c = doc.createElementNS("", "cbc:Description");
            DiscrepancyResponse.appendChild(Description_c);
            Description_c.appendChild(doc.createTextNode(items.getDocu_motivoanular()));//llsms s ls bd
            
            
            Element BillingReference = doc.createElementNS("","cac:BillingReference");
            envelope.appendChild(BillingReference);
            BillingReference.appendChild(doc.createTextNode("\n"));
            
            Element InvoiceDocumentReference = doc.createElementNS("","cac:InvoiceDocumentReference");
            BillingReference.appendChild(InvoiceDocumentReference);
            InvoiceDocumentReference.appendChild(doc.createTextNode("\n"));
            
            Element ID_c = doc.createElementNS("", "cbc:ID");
            InvoiceDocumentReference.appendChild(ID_c);
            ID_c.appendChild(doc.createTextNode(items.getDocu_tipodcocumento_numero()));//llama a la bd
            
            Element DocumentTypeCode = doc.createElementNS("", "cbc:DocumentTypeCode");
            InvoiceDocumentReference.appendChild(DocumentTypeCode);
//            DocumentTypeCode.appendChild(doc.createTextNode(items.getTipodocanula()));//llama a la bd
            DocumentTypeCode.appendChild(doc.createTextNode("01"));//llama a la bd
            
            
            
            
//bloque2 cac:Signature--------------------------------------------------------
            Element Signature = doc.createElementNS("", "cac:Signature");
            envelope.appendChild(Signature);
            Signature.appendChild(doc.createTextNode("\n"));

            Element ID6 = doc.createElementNS("", "cbc:ID");
            Signature.appendChild(ID6);
            ID6.appendChild(doc.createTextNode(items.getEmpr_nroruc()));
            
            Element ID61 = doc.createElementNS("", "cbc:Note");
            Signature.appendChild(ID61);
            ID61.appendChild(doc.createTextNode(items.getEmpr_nombrecomercial().trim()));
//            ID6.appendChild(doc.createTextNode("sign"));

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
            URI.appendChild(doc.createTextNode("sing"));
//bloque3 cac:AccountingSupplierParty-----------------------------------------

            Element AccountingSupplierParty = doc.createElementNS("", "cac:AccountingSupplierParty");
            envelope.appendChild(AccountingSupplierParty);
            AccountingSupplierParty.appendChild(doc.createTextNode("\n"));

//            Element CustomerAssignedAccountID = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
//            AccountingSupplierParty.appendChild(CustomerAssignedAccountID);
//            CustomerAssignedAccountID.appendChild(doc.createTextNode(items.getEmpr_nroruc().trim()));
//
//            Element AdditionalAccountID = doc.createElementNS("", "cbc:AdditionalAccountID");
//            AccountingSupplierParty.appendChild(AdditionalAccountID);
//            AdditionalAccountID.appendChild(doc.createTextNode(items.getEmpr_tipodoc().trim()));
//***********************************************************
            Element Party = doc.createElementNS("", "cac:Party");
            AccountingSupplierParty.appendChild(Party);
            Party.appendChild(doc.createTextNode("\n"));

            Element PartyIdentification1 = doc.createElementNS("", "cac:PartyIdentification");
            Party.appendChild(PartyIdentification1);//se anade al grupo party
            PartyIdentification1.appendChild(doc.createTextNode("\n"));
            
            Element ID = doc.createElementNS("", "cbc:ID");
            Attr schemeURI =doc.createAttribute("schemeURI");
            Attr schemeAgencyName =doc.createAttribute("schemeAgencyName");
            Attr schemeName =doc.createAttribute("schemeName");
            Attr schemeID =doc.createAttribute("schemeID");
            schemeURI.setValue("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
            schemeAgencyName.setValue("PE:SUNAT");
            schemeName.setValue("SUNAT:Identificador de Documento de Identidad");
            schemeID.setValue(items.getEmpr_tipodoc().trim());
            PartyIdentification1.appendChild(ID);
            ID.setAttributeNode(schemeURI);
            ID.setAttributeNode(schemeAgencyName);
            ID.setAttributeNode(schemeName);
            ID.setAttributeNode(schemeID);
            ID.appendChild(doc.createTextNode(items.getEmpr_nroruc().trim()));
            
            Element PartyName1 = doc.createElementNS("", "cac:PartyName");
            Party.appendChild(PartyName1);//se anade al grupo party
            PartyName1.appendChild(doc.createTextNode("\n"));

            Element Name2 = doc.createElementNS("", "cbc:Name");
            PartyName1.appendChild(Name2);//se anade al grupo partyname1
            cdata = doc.createCDATASection(items.getEmpr_razonsocial().trim());
            Name2.appendChild(cdata);

//          
            Element PartyLegalEntity = doc.createElementNS("", "cac:PartyLegalEntity");
            Party.appendChild(PartyLegalEntity);//se anade al grupo party
            PartyLegalEntity.appendChild(doc.createTextNode("\n"));

            Element RegistrationName = doc.createElementNS("", "cbc:RegistrationName");
            PartyLegalEntity.appendChild(RegistrationName);//se anade al grupo Country
            cdata = doc.createCDATASection(items.getEmpr_razonsocial().trim());
            RegistrationName.appendChild(cdata);
            
            
            Element RegistrationAddress = doc.createElementNS("", "cac:RegistrationAddress");
            PartyLegalEntity.appendChild(RegistrationAddress);//se anade al grupo Country
            RegistrationName.appendChild(doc.createTextNode("\n"));
            
            Element AddressID = doc.createElementNS("","cbc:ID");
            RegistrationAddress.appendChild(AddressID);
            AddressID.appendChild(doc.createTextNode(items.getEmpr_ubigeo()));
            
            Element AddressTypeCode = doc.createElementNS("","cbc:AddressTypeCode");
            RegistrationAddress.appendChild(AddressTypeCode);
            AddressTypeCode.appendChild(doc.createTextNode("0000"));
            
            Element CitySubdivisionName = doc.createElementNS("","cbc:CitySubdivisionName");
            RegistrationAddress.appendChild(CitySubdivisionName);
            CitySubdivisionName.appendChild(doc.createTextNode("NONE"));
            
            Element CityName = doc.createElementNS("","cbc:CityName");
            RegistrationAddress.appendChild(CityName);
            CityName.appendChild(doc.createTextNode(items.getEmpr_provincia()));
            
            Element CountrySubentity = doc.createElementNS("","cbc:CountrySubentity");
            RegistrationAddress.appendChild(CountrySubentity);
            CountrySubentity.appendChild(doc.createTextNode(items.getEmpr_departamento()));
            
            Element District = doc.createElementNS("","cbc:District");
            RegistrationAddress.appendChild(District);
            District.appendChild(doc.createTextNode(items.getEmpr_distrito()));
            
            Element AddressLine = doc.createElementNS("","cac:AddressLine");
            RegistrationAddress.appendChild(AddressLine);
            AddressLine.appendChild(doc.createTextNode("\n"));
            
            Element Line = doc.createElementNS("","cbc:Line");
            AddressLine.appendChild(Line);
            cdata = doc.createCDATASection(items.getEmpr_direccion().trim());
            Line.appendChild(cdata);
            
            Element Country = doc.createElementNS("","cac:Country");
            RegistrationAddress.appendChild(Country);
            Country.appendChild(doc.createTextNode("\n"));
            
            Element IdentificationCode = doc.createElementNS("","cbc:IdentificationCode");
            Country.appendChild(IdentificationCode);
            IdentificationCode.appendChild(doc.createTextNode(items.getEmpr_pais()));
            
            
// bloque4
            Element AccountingCustomerParty = doc.createElementNS("", "cac:AccountingCustomerParty");
            envelope.appendChild(AccountingCustomerParty);
            AccountingCustomerParty.appendChild(doc.createTextNode("\n"));

//            Element CustomerAssignedAccountID1 = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
//            AccountingCustomerParty.appendChild(CustomerAssignedAccountID1);//se anade al grupo AccountingCustomerParty
//            CustomerAssignedAccountID1.appendChild(doc.createTextNode(items.getClie_numero().trim()));
//
//            Element AdditionalAccountID1 = doc.createElementNS("", "cbc:AdditionalAccountID");
//            AccountingCustomerParty.appendChild(AdditionalAccountID1);//se anade al grupo AccountingCustomerParty
//            AdditionalAccountID1.appendChild(doc.createTextNode(items.getClie_tipodoc().trim()));

            Element Party1 = doc.createElementNS("", "cac:Party");
            AccountingCustomerParty.appendChild(Party1);//se anade al grupo AccountingCustomerParty
            Party1.appendChild(doc.createTextNode("\n"));
            
            Element PartyIdentification2 = doc.createElementNS("", "cac:PartyIdentification");
            Party1.appendChild(PartyIdentification2);//se anade al grupo party
            PartyIdentification2.appendChild(doc.createTextNode("\n"));
            
            Element ID1 = doc.createElementNS("", "cbc:ID");
            Attr schemeURI1 =doc.createAttribute("schemeURI");
            Attr schemeAgencyName1 =doc.createAttribute("schemeAgencyName");
            Attr schemeName1 =doc.createAttribute("schemeName");
            Attr schemeID1 =doc.createAttribute("schemeID");
            schemeURI1.setValue("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
            schemeAgencyName1.setValue("PE:SUNAT");
            schemeName1.setValue("SUNAT:Identificador de Documento de Identidad");
            schemeID1.setValue(items.getClie_tipodoc().trim());
            PartyIdentification2.appendChild(ID1);
            ID1.setAttributeNode(schemeURI1);
            ID1.setAttributeNode(schemeAgencyName1);
            ID1.setAttributeNode(schemeName1);
            ID1.setAttributeNode(schemeID1);
            ID1.appendChild(doc.createTextNode(items.getClie_numero().trim()));
            

            Element PartyLegalEntity1 = doc.createElementNS("", "cac:PartyLegalEntity");
            Party1.appendChild(PartyLegalEntity1);//se anade al grupo Party1
            PartyLegalEntity1.appendChild(doc.createTextNode("\n"));
            Element RegistrationName1 = doc.createElementNS("", "cbc:RegistrationName");
            PartyLegalEntity1.appendChild(RegistrationName1);//se anade al grupo PartyLegalEntity1
            cdata = doc.createCDATASection(items.getClie_nombre().trim());
            RegistrationName1.appendChild(cdata);
            

//bloque 5
            Element TaxTotal = doc.createElementNS("", "cac:TaxTotal");
            envelope.appendChild(TaxTotal);
            TaxTotal.appendChild(doc.createTextNode("\n"));

            Element TaxAmount = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
            TaxAmount.setIdAttributeNS(null, "currencyID", true);
            TaxTotal.appendChild(TaxAmount);//se anade al grupo TaxTotal
            TaxAmount.appendChild(doc.createTextNode(redondea(Double.parseDouble(items.getDocu_igv()+"") + Double.parseDouble(items.getDocu_otrostributos()+""),2)));
            
            Element TaxSubtotal = doc.createElementNS("", "cac:TaxSubtotal");
            TaxTotal.appendChild(TaxSubtotal);//se anade al grupo TaxTotal
            TaxSubtotal.appendChild(doc.createTextNode("\n"));

            Element TaxableAmount1 = doc.createElementNS("", "cbc:TaxableAmount");
            TaxableAmount1.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
            TaxableAmount1.setIdAttributeNS(null, "currencyID", true);
            TaxSubtotal.appendChild(TaxableAmount1);//se anade al grupo TaxSubtotal
            TaxableAmount1.appendChild(doc.createTextNode(items.getDocu_gravada()+""));
            
             Element TaxAmount1 = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmount1.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
            TaxAmount1.setIdAttributeNS(null, "currencyID", true);
            TaxSubtotal.appendChild(TaxAmount1);//se anade al grupo TaxTotal
            TaxAmount1.appendChild(doc.createTextNode(items.getDocu_igv()+""));

            Element TaxCategory = doc.createElementNS("", "cac:TaxCategory");
            TaxSubtotal.appendChild(TaxCategory);//se anade al grupo TaxSubtotal
            TaxCategory.appendChild(doc.createTextNode("\n"));
            
            Element ID8_5 = doc.createElementNS("", "cbc:ID");
            TaxCategory.appendChild(ID8_5);
            ID8_5.appendChild(doc.createTextNode("S"));
           
            Element TaxScheme = doc.createElementNS("", "cac:TaxScheme");
            TaxCategory.appendChild(TaxScheme);//se anade al grupo TaxCategory
            TaxScheme.appendChild(doc.createTextNode("\n"));

            Element ID9 = doc.createElementNS("", "cbc:ID");
            ID9.setAttributeNS(null, "schemeID", "UN/ECE 5153");
            ID9.setAttributeNS(null, "schemeAgencyID", "6");
            TaxScheme.appendChild(ID9);//se anade al grupo TaxScheme
            ID9.appendChild(doc.createTextNode("1000")); 

            Element Name3 = doc.createElementNS("", "cbc:Name");
            TaxScheme.appendChild(Name3);//se anade al grupo TaxScheme
            Name3.appendChild(doc.createTextNode("IGV"));

            Element TaxTypeCode = doc.createElementNS("", "cbc:TaxTypeCode");
            TaxScheme.appendChild(TaxTypeCode);//se anade al grupo TaxScheme
            TaxTypeCode.appendChild(doc.createTextNode("VAT"));
            
            if(items.getDocu_otrostributos()>0.0){
            Element Taxsubtotal0 = doc.createElementNS("","cac:TaxSubtotal");
            TaxTotal.appendChild(Taxsubtotal0);
            Taxsubtotal0.appendChild(doc.createTextNode("\n"));
            

            
            Element TaxAmount0 = doc.createElementNS("", "cbc:TaxAmount");
            TaxAmount0.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
            TaxAmount0.setIdAttributeNS(null, "currencyID", true);
            Taxsubtotal0.appendChild(TaxAmount0);//se anade al grupo TaxSubtotal
            TaxAmount0.appendChild(doc.createTextNode(items.getDocu_otrostributos()+""));
            
            Element TaxCategory0 = doc.createElementNS("", "cac:TaxCategory");
            Taxsubtotal0.appendChild(TaxCategory0);//se anade al grupo TaxSubtotal
            TaxCategory0.appendChild(doc.createTextNode("\n"));
            
            Element ID9_5 = doc.createElementNS("", "cbc:ID");
            TaxCategory0.appendChild(ID9_5);
            ID9_5.appendChild(doc.createTextNode("S"));
            
            Element TaxScheme0 = doc.createElementNS("", "cac:TaxScheme");
            TaxCategory0.appendChild(TaxScheme0);//se anade al grupo TaxCategory
            TaxScheme0.appendChild(doc.createTextNode("\n"));
            
            Element ID0 = doc.createElementNS("", "cbc:ID");
            ID0.setAttributeNS(null, "schemeID", "UN/ECE 5153");
            ID0.setAttributeNS(null, "schemeAgencyID", "6");
            TaxScheme0.appendChild(ID0);//se anade al grupo TaxScheme
            ID0.appendChild(doc.createTextNode("7152"));
            
            Element Name0 = doc.createElementNS("", "cbc:Name");
            TaxScheme0.appendChild(Name0);//se anade al grupo TaxScheme
            Name0.appendChild(doc.createTextNode("ICBPER"));

            Element TaxTypeCode0 = doc.createElementNS("", "cbc:TaxTypeCode");
            TaxScheme0.appendChild(TaxTypeCode0);//se anade al grupo TaxScheme
            TaxTypeCode0.appendChild(doc.createTextNode("OTH"));
            
            }
//bloque 6     
            Element LegalMonetaryTotal = doc.createElementNS("", "cac:LegalMonetaryTotal");
            envelope.appendChild(LegalMonetaryTotal);
            LegalMonetaryTotal.appendChild(doc.createTextNode("\n"));

            if (items.getDocu_descuento()>0) {
                Element AllowanceTotalAmount = doc.createElementNS("", "cbc:AllowanceTotalAmount");
                AllowanceTotalAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                AllowanceTotalAmount.setIdAttributeNS(null, "currencyID", true);
                LegalMonetaryTotal.appendChild(AllowanceTotalAmount);//se anade al grupo LegalMonetaryTotal
                AllowanceTotalAmount.appendChild(doc.createTextNode(items.getDocu_descuento()+""));
            }

//            Element LineExtensionAmount = doc.createElementNS("", "cbc:LineExtensionAmount");
//            LineExtensionAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
//            LineExtensionAmount.setIdAttributeNS(null, "currencyID", true);
//            LegalMonetaryTotal.appendChild(LineExtensionAmount);//se anade al grupo LegalMonetaryTotal
//            LineExtensionAmount.appendChild(doc.createTextNode(items.getDocu_gravada().trim()));

            Element TaxInclusiveAmount = doc.createElementNS("", "cbc:TaxInclusiveAmount");
            TaxInclusiveAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
            TaxInclusiveAmount.setIdAttributeNS(null, "currencyID", true);
            LegalMonetaryTotal.appendChild(TaxInclusiveAmount);//se anade al grupo LegalMonetaryTotal
            TaxInclusiveAmount.appendChild(doc.createTextNode(items.getDocu_total()+""));

            Element PayableAmount = doc.createElementNS("", "cbc:PayableAmount");
            PayableAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
            PayableAmount.setIdAttributeNS(null, "currencyID", true);
            LegalMonetaryTotal.appendChild(PayableAmount);//se anade al grupo LegalMonetaryTotal
            PayableAmount.appendChild(doc.createTextNode(items.getDocu_total()+""));
//detalle Nota de credito
            log.info("generarXMLZipiadoBoleta - Iniciamos detalle XML ");
            for (DetalleBean listaDet : detdocelec) {
                Element InvoiceLine = doc.createElementNS("", "cac:CreditNoteLine");
                envelope.appendChild(InvoiceLine);
                InvoiceLine.appendChild(doc.createTextNode("\n"));

                Element ID11 = doc.createElementNS("", "cbc:ID");
                InvoiceLine.appendChild(ID11);//se anade al grupo InvoiceLine
                ID11.appendChild(doc.createTextNode(listaDet.getItem_orden()+""));

                Element InvoicedQuantity = doc.createElementNS("", "cbc:CreditedQuantity");
                InvoicedQuantity.setAttributeNS(null, "unitCode", listaDet.getItem_unidad().trim());
                InvoicedQuantity.setIdAttributeNS(null, "unitCode", true);

                InvoiceLine.appendChild(InvoicedQuantity);//se anade al grupo InvoiceLine
                InvoicedQuantity.appendChild(doc.createTextNode(listaDet.getItem_cantidad()+""));

                Element LineExtensionAmount1 = doc.createElementNS("", "cbc:LineExtensionAmount");
                LineExtensionAmount1.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                LineExtensionAmount1.setIdAttributeNS(null, "currencyID", true);

                InvoiceLine.appendChild(LineExtensionAmount1);//se anade al grupo InvoiceLine
                LineExtensionAmount1.appendChild(doc.createTextNode(listaDet.getItem_to_subtotal()+""));

                Element PricingReference = doc.createElementNS("", "cac:PricingReference");
                InvoiceLine.appendChild(PricingReference);//se anade al grupo InvoiceLine
                PricingReference.appendChild(doc.createTextNode("\n"));

                Element AlternativeConditionPrice = doc.createElementNS("", "cac:AlternativeConditionPrice");
                PricingReference.appendChild(AlternativeConditionPrice);//se anade al grupo PricingReference
                AlternativeConditionPrice.appendChild(doc.createTextNode("\n"));

                Element PriceAmount = doc.createElementNS("", "cbc:PriceAmount");
                PriceAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                PriceAmount.setIdAttributeNS(null, "currencyID", true);
                AlternativeConditionPrice.appendChild(PriceAmount);//se anade al grupo AlternativeConditionPrice
                PriceAmount.appendChild(doc.createTextNode(listaDet.getItem_pventa()+""));

                Element PriceTypeCode = doc.createElementNS("", "cbc:PriceTypeCode");
                AlternativeConditionPrice.appendChild(PriceTypeCode);//se anade al grupo AlternativeConditionPrice
                PriceTypeCode.appendChild(doc.createTextNode("01")); //=================================>Faltaba especificar ite

//                  
//                if (!listaDet.getItem_pventa_no_onerosa().equals("0.00")) {
//                    Element AlternativeConditionPrice02 = doc.createElementNS("", "cac:AlternativeConditionPrice");
//                    PricingReference.appendChild(AlternativeConditionPrice02);//se anade al grupo PricingReference
//                    AlternativeConditionPrice02.appendChild(doc.createTextNode("\n"));
//
//                    Element PriceAmount02 = doc.createElementNS("", "cbc:PriceAmount");
//                    PriceAmount02.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
//                    PriceAmount02.setIdAttributeNS(null, "currencyID", true);
//                    AlternativeConditionPrice02.appendChild(PriceAmount02);//se anade al grupo AlternativeConditionPrice
//                    PriceAmount02.appendChild(doc.createTextNode(listaDet.getItem_pventa_no_onerosa().trim()));
//
//                    Element PriceTypeCode02 = doc.createElementNS("", "cbc:PriceTypeCode");
//                    AlternativeConditionPrice02.appendChild(PriceTypeCode02);//se anade al grupo AlternativeConditionPrice
//                    PriceTypeCode02.appendChild(doc.createTextNode("02")); //==>Para los casos de gatuito venta no Onerosa
//                }

//
                Element TaxTotal1 = doc.createElementNS("", "cac:TaxTotal");
                InvoiceLine.appendChild(TaxTotal1);//se anade al grupo InvoiceLine
                TaxTotal1.appendChild(doc.createTextNode("\n"));

                Element TaxAmount2 = doc.createElementNS("", "cbc:TaxAmount");
                TaxAmount2.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                TaxAmount2.setIdAttributeNS(null, "currencyID", true);
                TaxTotal1.appendChild(TaxAmount2);//se anade al grupo TaxTotal1
                TaxAmount2.appendChild(doc.createTextNode(redondea(Double.parseDouble(listaDet.getItem_to_igv()+"") + Double.parseDouble(listaDet.getItem_pventa_nohonerosa()+""),2)));
                
                if (listaDet.getItem_pventa_nohonerosa()>0) {
                    Element TaxSubtotal00 = doc.createElementNS("", "cac:TaxSubtotal");
                    TaxTotal1.appendChild(TaxSubtotal00);//se anade al grupo TaxTotal1
                    TaxSubtotal00.appendChild(doc.createTextNode("\n"));
                    
                    Element TaxAmount00 = doc.createElementNS("", "cbc:TaxAmount");
                    TaxAmount00.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                    TaxAmount00.setIdAttributeNS(null, "currencyID", true);
                    TaxSubtotal00.appendChild(TaxAmount00);
                    TaxAmount00.appendChild(doc.createTextNode(listaDet.getItem_pventa_nohonerosa()+""));
                    
                    Element BaseUnitMeasure = doc.createElementNS("", "cbc:BaseUnitMeasure");
                    BaseUnitMeasure.setAttributeNS(null, "unitCode", items.getDocu_moneda().trim());
                    BaseUnitMeasure.setIdAttributeNS(null, "unitCode", true);
                    TaxSubtotal00.appendChild(BaseUnitMeasure);
                    BaseUnitMeasure.appendChild(doc.createTextNode(listaDet.getItem_cantidad()+"".replace(".00", "")));
                    
                    Element TaxCategory000 = doc.createElementNS("", "cac:TaxCategory");
                    TaxSubtotal00.appendChild(TaxCategory000);//se anade al grupo TaxSubtotal1
                    TaxCategory000.appendChild(doc.createTextNode("\n"));
                    
                    Element PerUnitAmount00 = doc.createElementNS("", "cbc:PerUnitAmount");
                    PerUnitAmount00.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                    PerUnitAmount00.setIdAttributeNS(null, "currencyID", true);
                    TaxCategory000.appendChild(PerUnitAmount00);//se anade al grupo TaxSubtotal1
                    PerUnitAmount00.appendChild(doc.createTextNode(redondea(Double.parseDouble(listaDet.getItem_pventa_nohonerosa()+"")/Double.parseDouble(listaDet.getItem_cantidad()+""),2)));
                    
                    Element TaxScheme000 = doc.createElementNS("", "cac:TaxScheme");
                    TaxCategory000.appendChild(TaxScheme000);//se anade al grupo TaxSubtotal1
                    TaxScheme000.appendChild(doc.createTextNode("\n"));
                    
                    Element ID000 = doc.createElementNS("", "cbc:ID");
                    ID000.setAttribute("schemeURI", "urn:pe:gob:sunat:cpe:see:gem:catalogos: catálogo05");
                    ID000.setAttribute("schemeAgencyName", "PE:SUNAT");
                    ID000.setAttribute("schemeName", "Codigo de tributos");
                    TaxScheme000.appendChild(ID000);
                    ID000.appendChild(doc.createTextNode("7152"));
                    
                    Element Name000 = doc.createElementNS("", "cbc:Name");
                    TaxScheme000.appendChild(Name000);
                    Name000.appendChild(doc.createTextNode("ICBPER"));
                    
                    Element TaxTypeCode000 = doc.createElementNS("", "cbc:TaxTypeCode");
                    TaxScheme000.appendChild(TaxTypeCode000);
                    TaxTypeCode000.appendChild(doc.createTextNode("OTH"));
                      
                }

                Element TaxSubtotal1 = doc.createElementNS("", "cac:TaxSubtotal");
                TaxTotal1.appendChild(TaxSubtotal1);//se anade al grupo TaxTotal1
                TaxSubtotal1.appendChild(doc.createTextNode("\n"));

                Element TaxableAmount = doc.createElementNS("", "cbc:TaxableAmount");
                TaxableAmount.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                TaxableAmount.setIdAttributeNS(null, "currencyID", true);

                TaxSubtotal1.appendChild(TaxableAmount);//se anade al grupo TaxSubtotal1
                TaxableAmount.appendChild(doc.createTextNode(listaDet.getItem_to_subtotal()+""));

                Element TaxAmount3 = doc.createElementNS("", "cbc:TaxAmount");
                TaxAmount3.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim()); //================>errror estaba con item..getItem_moneda()
                TaxAmount3.setIdAttributeNS(null, "currencyID", true);
                TaxSubtotal1.appendChild(TaxAmount3);//se anade al grupo TaxSubtotal1
                TaxAmount3.appendChild(doc.createTextNode(listaDet.getItem_to_igv()+""));

                

                Element TaxCategory1 = doc.createElementNS("", "cac:TaxCategory");
                TaxSubtotal1.appendChild(TaxCategory1);//se anade al grupo TaxSubtotal1
                TaxCategory1.appendChild(doc.createTextNode("\n"));

//                dosElement ID12 = doc.createElementNS("", "cbc:ID");
//                ID12.setAttribute("schemeAgencyName", "United Nations Economic Commission for Europe");
//                ID12.setAttribute("schemeName", "Tax Category Identifier");
//                ID12.setAttribute("schemeID", "UN/ECE 5305");
//                TaxCategory1.appendChild(ID12);//se anade al grupo TaxCategory1
//                ID12.appendChild(doc.createTextNode("S"));
                
                Element Percent = doc.createElementNS("", "cbc:Percent");
                TaxCategory1.appendChild(Percent);//se anade al grupo TaxSubtotal1
                Percent.appendChild(doc.createTextNode(items.getTasa_igv().trim().replace("%","")));

                Element TaxExemptionReasonCode = doc.createElementNS("", "cbc:TaxExemptionReasonCode");
                TaxExemptionReasonCode.setAttribute("listURI", "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07");
                TaxExemptionReasonCode.setAttribute("listName", "SUNAT:Codigo de Tipo de Afectación del IGV");
                TaxExemptionReasonCode.setAttribute("listAgencyName", "PE:SUNAT");
                TaxCategory1.appendChild(TaxExemptionReasonCode);//se anade al grupo TaxCategory1
                TaxExemptionReasonCode.appendChild(doc.createTextNode(listaDet.getItem_afectacion().trim()));

//                Element TierRange = doc.createElementNS("", "cbc:TierRange");
//                TaxCategory1.appendChild(TierRange);//se anade al grupo TaxCategory1
//                TierRange.appendChild(doc.createTextNode("00"));

                Element TaxScheme1 = doc.createElementNS("", "cac:TaxScheme");
                TaxCategory1.appendChild(TaxScheme1);//se anade al grupo TaxCategory1
                TaxScheme1.appendChild(doc.createTextNode("\n"));

                Element ID15 = doc.createElementNS("", "cbc:ID");
                ID15.setAttribute("schemeAgencyName", "United Nations Economic Commission for Europe");
                ID15.setAttribute("schemeName", "Tax Scheme Identifier");
                ID15.setAttribute("schemeID", "UN/ECE 5153");
                TaxScheme1.appendChild(ID15);//se anade al grupo TaxCategory1
                ID15.appendChild(doc.createTextNode("1000"));

                Element Name9 = doc.createElementNS("", "cbc:Name");
                TaxScheme1.appendChild(Name9);//se anade al grupo TaxCategory1
                Name9.appendChild(doc.createTextNode("IGV"));

                Element TaxTypeCode1 = doc.createElementNS("", "cbc:TaxTypeCode");
                TaxScheme1.appendChild(TaxTypeCode1);//se anade al grupo TaxCategory1
                TaxTypeCode1.appendChild(doc.createTextNode("VAT"));

                Element Item = doc.createElementNS("", "cac:Item");
                InvoiceLine.appendChild(Item);//se anade al grupo InvoiceLine
                Item.appendChild(doc.createTextNode("\n"));

                Element Description = doc.createElementNS("", "cbc:Description");
                Item.appendChild(Description);//se anade al grupo Item
                cdata = doc.createCDATASection(listaDet.getItem_descripcion().trim());
                Description.appendChild(cdata);

                Element SellersItemIdentification = doc.createElementNS("", "cac:SellersItemIdentification");
                Item.appendChild(SellersItemIdentification);//se anade al grupo Item
                SellersItemIdentification.appendChild(doc.createTextNode("\n"));

                Element ID18 = doc.createElementNS("", "cbc:ID");
                SellersItemIdentification.appendChild(ID18);//se anade al grupo Item
                ID18.appendChild(doc.createTextNode(listaDet.getItem_codproducto().trim()));

                Element Price = doc.createElementNS("", "cac:Price");
                InvoiceLine.appendChild(Price);//se anade al grupo InvoiceLine
                Price.appendChild(doc.createTextNode("\n"));

                Element PriceAmount2 = doc.createElementNS("", "cbc:PriceAmount");
                PriceAmount2.setAttributeNS(null, "currencyID", items.getDocu_moneda().trim());
                PriceAmount2.setIdAttributeNS(null, "currencyID", true);
                Price.appendChild(PriceAmount2);//se anade al grupo Price
                PriceAmount2.appendChild(doc.createTextNode(listaDet.getItem_pvtaigv()+""));
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
                log.info("generarXMLZipiadoNotaCredito - firma el XML ");
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
            resultado = GeneralFunctions.crearZip(items, unidadEnvio, signatureFile);

        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar el archivo de formato xml de la NotaCred.";
            log.error("generarXMLZipiadoFactura - error  " + ex.toString());

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
