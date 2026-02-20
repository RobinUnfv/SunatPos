package ws;

import Modelo.Beans.ComunicacionBajaBean;
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
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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


public class DarBajaDocElectronica {

    private static Log log = LogFactory.getLog(DarBajaDocElectronica.class);

    // Rutas de archivos
    private static final String UNIDAD_ENVIO = "d:\\POS-SUNAT\\envio\\";
    private static final String UNIDAD_RESPUESTA = "d:\\POS-SUNAT\\respuesta\\";

    // Credenciales del certificado
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_FILE = "d:\\POS-SUNAT\\certificado.jks";
    private static final String KEYSTORE_PASS = "123456789";
    private static final String PRIVATE_KEY_PASS = "CORPTEx2218";

    // Ambiente SUNAT: 1=Beta, 2=QA, 3=Producción
    private static final String AMBIENTE_SUNAT = "1";

    /**
     * Método principal para generar y enviar comunicación de baja
     * @param conn Conexión a la base de datos
     * @return Resultado del proceso
     */
    public static String generarComunicacionBaja(String noFactu, Connection conn) {
        log.info("=== INICIANDO COMUNICACIÓN DE BAJA ===");
        org.apache.xml.security.Init.init();

        String resultado = "";

        try {
            // 1. Cargar documentos pendientes de baja desde FACTU.COMUNICACION_BAJA
            ComunicacionBajaBean documentosBaja = DElectronicoDespachador.cargarDocumentosBajaPendientes(noFactu, conn);

            if (documentosBaja == null) {
                log.info("No hay documentos pendientes de baja");
                return "0|No hay documentos pendientes de comunicación de baja";
            }

            // 2. Cargar datos de la empresa
            ComunicacionBajaBean datosEmpresa = DElectronicoDespachador.cargarDatosEmpresaBaja(conn);
            if (datosEmpresa.getEmprRuc() == null) {
                return "0100|Error: No se pudieron cargar los datos de la empresa";
            }

            // 3. Obtener fecha de baja y correlativo
            //String fechaBaja = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fechaBaja = new SimpleDateFormat("yyyy-MM-dd").format( documentosBaja.getFecBaja() );
            String fechaBajaCompacta = fechaBaja.replace("-", "");
            //int correlativo = DElectronicoDespachador.obtenerCorrelativoBaja(fechaBaja, conn);
            String correlativo = documentosBaja.getNroCorrelativo();

            log.info("Fecha de baja: " + fechaBaja + ", Correlativo: " + correlativo);

            // 4. Bloquear documentos
            // DElectronicoDespachador.bloquearDocumentosBaja(documentosBaja, conn);

            // 5. Generar nombre del archivo
            String nombreArchivo = datosEmpresa.getEmprRuc() + "-RA-" + fechaBajaCompacta + "-" + correlativo;
            String pathXML = UNIDAD_ENVIO + nombreArchivo + ".xml";
            String pathZIP = UNIDAD_ENVIO + nombreArchivo + ".zip";

            // 6. Crear XML
            log.info("Generando XML: " + pathXML);
            resultado = crearXmlComunicacionBaja(documentosBaja, datosEmpresa, fechaBaja, correlativo, UNIDAD_ENVIO);

            /*if (resultado.startsWith("0100")) {
                DElectronicoDespachador.revertirBajaPendiente(documentosBaja, conn);
                return resultado;
            }*/

            // 7. Enviar a SUNAT
            log.info("Enviando a SUNAT: " + nombreArchivo + ".zip");
            String[] resultadoEnvio = enviarZipASunat(UNIDAD_ENVIO, nombreArchivo + ".zip", datosEmpresa.getEmprRuc());

            // 8. Marcar en proceso
            DElectronicoDespachador.marcarBajaEnProceso(documentosBaja, String.valueOf(correlativo), conn);



            if (resultadoEnvio[1] == null || resultadoEnvio[1].equals("nulo")) {
                DElectronicoDespachador.marcarBajaError(documentosBaja, "Error de conexión con SUNAT", conn);
                return "0100|Error de conexión con SUNAT";
            }

            // 9. El envío retorna un ticket, consultar estado
            String ticket = resultadoEnvio[1];
            log.info("Ticket SUNAT: " + ticket);

            // 10. Consultar estado del ticket
            String[] resultadoStatus = pedirStatus(UNIDAD_ENVIO, nombreArchivo + ".zip", datosEmpresa.getEmprRuc(), ticket);

            if (resultadoStatus[1] != null && !resultadoStatus[1].isEmpty()) {
                // Éxito
                DElectronicoDespachador.marcarBajaEnviada(documentosBaja, ticket, resultadoStatus[1], conn);
                resultado = "0|Comunicación de baja enviada correctamente. Ticket: " + ticket;
                DElectronicoDespachador.marcarEnviado(noFactu, ticket, resultado, conn);

                log.info("Comunicación de baja exitosa: " + ticket);
            } else {
                // Guardar ticket para consulta posterior
                DElectronicoDespachador.marcarBajaEnviada(documentosBaja, ticket, "Pendiente CDR", conn);
                resultado = "0|Comunicación enviada. Ticket: " + ticket + ". Consultar CDR posteriormente.";
                DElectronicoDespachador.marcarEnviado(noFactu, ticket, resultado, conn);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar comunicación de baja: " + ex.getMessage();
            log.error("Error en generarComunicacionBaja: " + ex.toString());
        }

        return resultado;
    }

    /**
     * Envía el ZIP a SUNAT usando sendSummary
     */
    public static String[] enviarZipASunat(String path, String zipFileName, String vruc) {
        String[] resultado = new String[2];
        resultado[0] = "";
        resultado[1] = "";

        log.info("enviarZipASunat - Archivo: " + zipFileName);

        try {
            javax.activation.FileDataSource fileDataSource = new javax.activation.FileDataSource(path + zipFileName);
            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fileDataSource);
            String respuestaSunat = null;

            switch (AMBIENTE_SUNAT) {
                case "1": // Beta
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver();
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.sendSummary(zipFileName, dataHandler);
                    log.info("Ambiente Beta - Ticket: " + respuestaSunat);
                    break;

                case "2": // QA
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver();
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.sendSummary(zipFileName, dataHandler);
                    log.info("Ambiente QA - Ticket: " + respuestaSunat);
                    break;

                case "3": // Producción
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe ws3 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver3 = new HeaderHandlerResolver();
                    handlerResolver3.setVruc(vruc);
                    ws3.setHandlerResolver(handlerResolver3);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService port3 = ws3.getBillServicePort();
                    respuestaSunat = port3.sendSummary(zipFileName, dataHandler);
                    log.info("Ambiente Producción - Ticket: " + respuestaSunat);
                    break;
            }

            resultado[0] = zipFileName;
            resultado[1] = (respuestaSunat != null) ? respuestaSunat : "nulo";

            log.info("Envío exitoso. Ticket: " + resultado[1]);

        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            String mensaje = ConversionUtils.extraerMensajeSOAPFault(ex);
            String codigo = ConversionUtils.extraerCodigoErrorSUNAT(ex);
            resultado[0] = "ERROR";
            resultado[1] = (codigo != null ? codigo : "ERROR") + "|" + mensaje;
            log.error("Error SOAP [" + codigo + "]: " + mensaje);
        } catch (Exception e) {
            e.printStackTrace();
            resultado[1] = "nulo";
            log.error("Error enviarZipASunat: " + e.toString());
        }

        return resultado;
    }

    /**
     * Consulta el estado del ticket en SUNAT
     */
    public static String[] pedirStatus(String path, String zipFileName, String vruc, String ticket) {
        String[] resultado = new String[2];
        resultado[0] = "";
        resultado[1] = "";

        log.info("pedirStatus - Ticket: " + ticket);

        try {
            StatusResponse respuestaSunat = null;

            switch (AMBIENTE_SUNAT) {
                case "1": // Beta
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver();
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.getStatus(ticket);
                    break;

                case "2": // QA
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver();
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.getStatus(ticket);
                    break;

                case "3": // Producción
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe ws3 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver3 = new HeaderHandlerResolver();
                    handlerResolver3.setVruc(vruc);
                    ws3.setHandlerResolver(handlerResolver3);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service.BillService port3 = ws3.getBillServicePort();
                    respuestaSunat = port3.getStatus(ticket);
                    break;
            }

            if (respuestaSunat != null && respuestaSunat.getContent() != null) {
                // Guardar y descomprimir respuesta
                String pathRecepcion = UNIDAD_RESPUESTA;
                FileOutputStream fos = new FileOutputStream(pathRecepcion + "R-" + zipFileName);
                fos.write(respuestaSunat.getContent());
                fos.close();

                // Descomprimir
                log.info("Descomprimiendo CDR: " + pathRecepcion + "R-" + zipFileName);
                ZipFile archive = new ZipFile(pathRecepcion + "R-" + zipFileName);
                Enumeration e = archive.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    File file = new File(pathRecepcion, entry.getName());
                    if (!file.isDirectory()) {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        InputStream in = archive.getInputStream(entry);
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.close();
                    }
                }
                archive.close();

                // Leer respuesta
                String xmlFileName = zipFileName.replace(".zip", ".xml");
                resultado[1] = LecturaXML.getRespuestaSunat(pathRecepcion + "R-" + xmlFileName);
                resultado[0] = ticket;

                log.info("CDR obtenido: " + resultado[1]);
            }

        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            String mensaje = ConversionUtils.extraerMensajeSOAPFault(ex);
            log.error("Error SOAP getStatus: " + mensaje);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error pedirStatus: " + e.toString());
        }

        return resultado;
    }

    /**
     * Crea el XML de Comunicación de Baja
     */
    private static String crearXmlComunicacionBaja(ComunicacionBajaBean documentos,
                                                   ComunicacionBajaBean datosEmpresa,
                                                   String fechaBaja,
                                                   String correlativo,
                                                   String unidadEnvio) {
        String resultado = "";

        try {
            ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");

            // Cargar certificado
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            FileInputStream fis = new FileInputStream(KEYSTORE_FILE);
            ks.load(fis, KEYSTORE_PASS.toCharArray());
            fis.close();

            // Obtener alias automáticamente
            String privateKeyAlias = ks.aliases().nextElement();

            PrivateKey privateKey = (PrivateKey) ks.getKey(privateKeyAlias, PRIVATE_KEY_PASS.toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("Private key is null");
            }
            X509Certificate cert = (X509Certificate) ks.getCertificate(privateKeyAlias);

            // Preparar documento XML
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.newDocument();

            // Datos para el nombre del archivo
            String ruc = datosEmpresa.getEmprRuc();
            String fechaCompacta = fechaBaja.replace("-", "");
            String identificador = "RA-" + fechaCompacta + "-" + correlativo;

            String pathXMLFile = unidadEnvio + ruc + "-" + identificador + ".xml";
            File signatureFile = new File(pathXMLFile);

            log.info("Creando XML: " + pathXMLFile);

            // ═══════════════════════════════════════════════════════════════════
            // ELEMENTO RAÍZ: VoidedDocuments
            // ═══════════════════════════════════════════════════════════════════
            Element envelope = doc.createElementNS("", "VoidedDocuments");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns", "urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            envelope.appendChild(doc.createTextNode("\n"));
            doc.appendChild(envelope);

            // ═══════════════════════════════════════════════════════════════════
            // UBLExtensions (Firma Digital)
            // ═══════════════════════════════════════════════════════════════════
            Element UBLExtensions = doc.createElementNS("", "ext:UBLExtensions");
            envelope.appendChild(UBLExtensions);

            Element UBLExtension = doc.createElementNS("", "ext:UBLExtension");
            UBLExtensions.appendChild(UBLExtension);

            Element ExtensionContent = doc.createElementNS("", "ext:ExtensionContent");
            UBLExtension.appendChild(ExtensionContent);

            String BaseURI = signatureFile.toURI().toURL().toString();
            XMLSignature sig = new XMLSignature(doc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA);
            ExtensionContent.appendChild(sig.getElement());

            // ═══════════════════════════════════════════════════════════════════
            // CABECERA
            // ═══════════════════════════════════════════════════════════════════
            Element UBLVersionID = doc.createElementNS("", "cbc:UBLVersionID");
            envelope.appendChild(UBLVersionID);
            UBLVersionID.appendChild(doc.createTextNode("2.0"));

            Element CustomizationID = doc.createElementNS("", "cbc:CustomizationID");
            envelope.appendChild(CustomizationID);
            CustomizationID.appendChild(doc.createTextNode("1.0"));

            // ID: RA-YYYYMMDD-correlativo
            Element ID = doc.createElementNS("", "cbc:ID");
            envelope.appendChild(ID);
            ID.appendChild(doc.createTextNode(identificador));

            // ReferenceDate: Fecha de los documentos a dar de baja
            Element ReferenceDate = doc.createElementNS("", "cbc:ReferenceDate");
            envelope.appendChild(ReferenceDate);
            // Usar la fecha de emisión del primer documento
            if ( documentos.getFecEmision() != null) {
                ReferenceDate.appendChild(doc.createTextNode(
                        new SimpleDateFormat("yyyy-MM-dd").format(documentos.getFecEmision())));
            } else {
                ReferenceDate.appendChild(doc.createTextNode(fechaBaja));
            }

            // IssueDate: Fecha de generación de la comunicación
            Element IssueDate = doc.createElementNS("", "cbc:IssueDate");
            envelope.appendChild(IssueDate);
            IssueDate.appendChild(doc.createTextNode(fechaBaja));

            // ═══════════════════════════════════════════════════════════════════
            // cac:Signature
            // ═══════════════════════════════════════════════════════════════════
            Element Signature = doc.createElementNS("", "cac:Signature");
            envelope.appendChild(Signature);

            Element SignatureID = doc.createElementNS("", "cbc:ID");
            Signature.appendChild(SignatureID);
            SignatureID.appendChild(doc.createTextNode(identificador));

            Element SignatoryParty = doc.createElementNS("", "cac:SignatoryParty");
            Signature.appendChild(SignatoryParty);

            Element PartyIdentification = doc.createElementNS("", "cac:PartyIdentification");
            SignatoryParty.appendChild(PartyIdentification);

            Element PartyID = doc.createElementNS("", "cbc:ID");
            PartyIdentification.appendChild(PartyID);
            PartyID.appendChild(doc.createTextNode(ruc));

            Element PartyName = doc.createElementNS("", "cac:PartyName");
            SignatoryParty.appendChild(PartyName);

            Element Name = doc.createElementNS("", "cbc:Name");
            PartyName.appendChild(Name);
            CDATASection cdata = doc.createCDATASection(datosEmpresa.getEmprRazonSocial());
            Name.appendChild(cdata);

            Element DigitalSignatureAttachment = doc.createElementNS("", "cac:DigitalSignatureAttachment");
            Signature.appendChild(DigitalSignatureAttachment);

            Element ExternalReference = doc.createElementNS("", "cac:ExternalReference");
            DigitalSignatureAttachment.appendChild(ExternalReference);

            Element URI = doc.createElementNS("", "cbc:URI");
            ExternalReference.appendChild(URI);
            URI.appendChild(doc.createTextNode(identificador));

            // ═══════════════════════════════════════════════════════════════════
            // cac:AccountingSupplierParty (Emisor)
            // ═══════════════════════════════════════════════════════════════════
            Element AccountingSupplierParty = doc.createElementNS("", "cac:AccountingSupplierParty");
            envelope.appendChild(AccountingSupplierParty);

            Element CustomerAssignedAccountID = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
            AccountingSupplierParty.appendChild(CustomerAssignedAccountID);
            CustomerAssignedAccountID.appendChild(doc.createTextNode(ruc));

            Element AdditionalAccountID = doc.createElementNS("", "cbc:AdditionalAccountID");
            AccountingSupplierParty.appendChild(AdditionalAccountID);
            AdditionalAccountID.appendChild(doc.createTextNode("6")); // RUC

            Element Party = doc.createElementNS("", "cac:Party");
            AccountingSupplierParty.appendChild(Party);

            Element PartyLegalEntity = doc.createElementNS("", "cac:PartyLegalEntity");
            Party.appendChild(PartyLegalEntity);

            Element RegistrationName = doc.createElementNS("", "cbc:RegistrationName");
            PartyLegalEntity.appendChild(RegistrationName);
            cdata = doc.createCDATASection(datosEmpresa.getEmprRazonSocial());
            RegistrationName.appendChild(cdata);

            // ═══════════════════════════════════════════════════════════════════
            // sac:VoidedDocumentsLine (Detalle de documentos a anular)
            // ═══════════════════════════════════════════════════════════════════
            int lineaNum = 1;
            //for (ComunicacionBajaBean documento : documentos) {
                Element VoidedDocumentsLine = doc.createElementNS("", "sac:VoidedDocumentsLine");
                envelope.appendChild(VoidedDocumentsLine);

                // LineID
                Element LineID = doc.createElementNS("", "cbc:LineID");
                VoidedDocumentsLine.appendChild(LineID);
                LineID.appendChild(doc.createTextNode(String.valueOf(lineaNum)));

                // DocumentTypeCode (01=Factura, 03=Boleta, 07=NC, 08=ND)
                Element DocumentTypeCode = doc.createElementNS("", "cbc:DocumentTypeCode");
                VoidedDocumentsLine.appendChild(DocumentTypeCode);
                DocumentTypeCode.appendChild(doc.createTextNode(documentos.getTipoDocumento()));

                // DocumentSerialID (Serie)
                Element DocumentSerialID = doc.createElementNS("", "sac:DocumentSerialID");
                VoidedDocumentsLine.appendChild(DocumentSerialID);
                DocumentSerialID.appendChild(doc.createTextNode(documentos.getSerie()));

                // DocumentNumberID (Número)
                Element DocumentNumberID = doc.createElementNS("", "sac:DocumentNumberID");
                VoidedDocumentsLine.appendChild(DocumentNumberID);
                DocumentNumberID.appendChild(doc.createTextNode(documentos.getNumero()));

                // VoidReasonDescription (Motivo de baja)
                Element VoidReasonDescription = doc.createElementNS("", "sac:VoidReasonDescription");
                VoidedDocumentsLine.appendChild(VoidReasonDescription);
                String motivo = documentos.getDescMotivo();
                if (motivo == null || motivo.trim().isEmpty()) {
                    motivo = "ANULACION DE COMPROBANTE";
                }
                VoidReasonDescription.appendChild(doc.createTextNode(motivo));

                log.info("Línea " + lineaNum + ": " + documentos.getTipoDocumento() + " " +
                        documentos.getSerie() + "-" + documentos.getNumero() + " - " + motivo);

                //lineaNum++;
            //}

            // ═══════════════════════════════════════════════════════════════════
            // FIRMAR DOCUMENTO
            // ═══════════════════════════════════════════════════════════════════
            sig.setId("Sign" + ruc);
            sig.addKeyInfo(cert);

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);

            sig.sign(privateKey);

            // ═══════════════════════════════════════════════════════════════════
            // GUARDAR XML
            // ═══════════════════════════════════════════════════════════════════
            FileOutputStream f = new FileOutputStream(signatureFile);
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");
            StreamResult sr = new StreamResult(f);
            tf.transform(new DOMSource(doc), sr);
            sr.getOutputStream().close();

            log.info("XML creado exitosamente: " + pathXMLFile);

            // ═══════════════════════════════════════════════════════════════════
            // CREAR ZIP
            // ═══════════════════════════════════════════════════════════════════
            resultado = GeneralFunctions.crearZipComunicacionBaja(ruc, identificador, unidadEnvio, signatureFile);

        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al generar XML de comunicación de baja: " + ex.getMessage();
            log.error("Error crearXmlComunicacionBaja: " + ex.toString());
        }

        return resultado;
    }

    /**
     * Método de utilidad para redondeo
     */
    public static String redondea(double numero, int decimales) {
        DecimalFormat f = new DecimalFormat("0.00");
        BigDecimal res = new BigDecimal(numero).setScale(decimales, BigDecimal.ROUND_HALF_UP);
        return f.format(res.doubleValue()).replace(",", ".");
    }
}