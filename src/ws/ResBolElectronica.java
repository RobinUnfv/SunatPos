package ws;

import Modelo.Beans.CabeceraBean;
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
import java.sql.Date;
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

/**
 * ══════════════════════════════════════════════════════════════════════════════════════
 * RESUMEN DIARIO DE BOLETAS ELECTRÓNICAS - UBL 2.0 SUNAT
 * ══════════════════════════════════════════════════════════════════════════════════════
 *
 * Genera el XML del Resumen Diario de Boletas según especificación SUNAT.
 *
 * CONSIDERACIONES:
 * - Máximo 100 boletas por resumen diario
 * - Incluye boletas despachadas (D) y anuladas (A)
 * - ConditionCode: 1=Adicionar, 2=Modificar, 3=Anular
 * - Formato nombre archivo: {RUC}-RC-{YYYYMMDD}-{correlativo}.xml
 *
 * ESTADOS DE BOLETA (FACTU.ARFAFE.ESTADO):
 * - 'D' = Despachado → ConditionCode = 1 (Adicionar)
 * - 'A' = Anulado → ConditionCode = 3 (Anular)
 *
 * ══════════════════════════════════════════════════════════════════════════════════════
 */
public class ResBolElectronica {

    private static Log log = LogFactory.getLog(ResBolElectronica.class);

    // ══════════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN
    // ══════════════════════════════════════════════════════════════════════════
    private static final String UNIDAD_ENVIO = "d:\\POS-SUNAT\\envio\\";
    private static final String UNIDAD_RESPUESTA = "d:\\POS-SUNAT\\respuesta\\";

    // Credenciales del certificado digital
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_FILE = "d:\\POS-SUNAT\\certificado.jks";
    private static final String KEYSTORE_PASS = "123456789";
    private static final String PRIVATE_KEY_PASS = "CORPTEx2218";

    // Ambiente SUNAT: 1=Beta, 2=QA, 3=Producción
    private static final String AMBIENTE_SUNAT = "1";

    // Límite máximo de boletas por resumen (requisito SUNAT)
    private static final int MAX_BOLETAS_RESUMEN = 100;

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * MÉTODO PRINCIPAL - Generar y enviar resumen diario de boletas
     * ══════════════════════════════════════════════════════════════════════════
     */
    public static String generarResumenDiario(String iddocument, Connection conn) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("INICIANDO RESUMEN DIARIO DE BOLETAS");
        log.info("═══════════════════════════════════════════════════════════════");

        org.apache.xml.security.Init.init();
        String[] resultado = new String[2];
        String res = "";

        List<Date> fechasBajas = DElectronicoDespachador.listaFechaResumenDiario(conn);

        if (fechasBajas.isEmpty()) {
            log.info("No hay fechas de resumen diario pendientes");
            res = "0|No hay fechas de resumen diario pendientes";
            return res;
        }

        // ══════════════════════════════════════════════════════════════════
        // 1. Cargar datos de la empresa (de cualquier documento)
        // ══════════════════════════════════════════════════════════════════
        //CabeceraBean datosEmpresa = DElectronicoDespachador.cargarDocElectronico(iddocument, conn);
        CabeceraBean datosEmpresa = DElectronicoDespachador.cargarDatoEmpresa(conn);

        if (datosEmpresa == null) {
            res = "0100|Error: No se encontraron datos de la empresa";
            return res;
        }

        for (Date fechaBaja : fechasBajas) {
            try {

                // ══════════════════════════════════════════════════════════════════
                // 2. Cargar boletas pendientes de resumen (máximo 100)
                // ══════════════════════════════════════════════════════════════════

                List<CabeceraBean> boletas = DElectronicoDespachador.ResumenDiario(fechaBaja,"E", "03", conn);

                if (boletas.isEmpty()) {
                    log.info("No hay boletas pendientes para el resumen diario");
                    return "0|No hay boletas pendientes para el resumen diario";
                }

                log.info("Boletas encontradas: " + boletas.size() + " (máximo " + MAX_BOLETAS_RESUMEN + ")");

                // ══════════════════════════════════════════════════════════════════
                // 3. Bloquear boletas para proceso
                // ══════════════════════════════════════════════════════════════════
                DElectronicoDespachador.bloquearBoletasResumen(boletas, conn);

                // ══════════════════════════════════════════════════════════════════
                // 4. Generar nombre del archivo
                // ══════════════════════════════════════════════════════════════════
                //String fechaHoy = new SimpleDateFormat("yyyyMMdd").format(fechaBaja);
                String fechaEmision = new SimpleDateFormat("yyyyMMdd").format(fechaBaja);
                int correlativo = 1; // TODO: Obtener correlativo dinámico si hay múltiples resúmenes por día
                String identificador = "RC-" + fechaEmision + "-" + correlativo;
                String nombreArchivo = datosEmpresa.getEmpr_nroruc() + "-" + identificador;

                log.info("Generando archivo: " + nombreArchivo);

                // ══════════════════════════════════════════════════════════════════
                // 5. Crear XML del resumen
                // ══════════════════════════════════════════════════════════════════
                res = crearXmlResumenDiario(datosEmpresa, boletas, UNIDAD_ENVIO, conn);

                if (res.startsWith("0100")) {
                    // Error al crear XML, revertir bloqueo
                    return res;
                }

                // ══════════════════════════════════════════════════════════════════
                // 6. Enviar a SUNAT (si está configurado)
                // ══════════════════════════════════════════════════════════════════
                if (datosEmpresa.getDocu_enviaws() != null && datosEmpresa.getDocu_enviaws().equals("S")) {
                    log.info("Enviando resumen a SUNAT...");

                    resultado = enviarZipASunat(UNIDAD_ENVIO, nombreArchivo + ".zip", datosEmpresa.getEmpr_nroruc());

                    if (resultado[1] == null || resultado[1].equals("nulo")) {
                        log.error("Error de conexión con SUNAT");
                        return "0100|Error de conexión con SUNAT";
                    }

                    // Obtener ticket
                    String ticket = resultado[1];
                    log.info("Ticket SUNAT: " + ticket);

                    // Consultar estado del ticket
                    String[] resultadoStatus = pedirStatus(UNIDAD_ENVIO, nombreArchivo + ".zip",
                            datosEmpresa.getEmpr_nroruc(), ticket);

                    if (resultadoStatus[1] != null && !resultadoStatus[1].isEmpty()) {
                        // Marcar boletas como enviadas
                        DElectronicoDespachador.marcarResumenEnviado(boletas, ticket, resultadoStatus[1], conn);
                        res = "0|Resumen diario enviado correctamente. Ticket: " + ticket;
                        log.info("Resumen enviado exitosamente");
                    } else {
                        res = "0|Resumen enviado. Ticket: " + ticket + ". Consultar CDR posteriormente.";
                    }

                } else {
                    log.info("Resumen generado (sin envío a SUNAT)");
                    res = "0|Resumen diario generado correctamente";
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                res = "0100|Error al generar resumen diario: " + ex.getMessage();
                log.error("Error en generarResumenDiario: " + ex.toString());
            }
        }

        return res;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * CREAR XML DEL RESUMEN DIARIO
     * ══════════════════════════════════════════════════════════════════════════
     *
     * Estructura del XML según guía SUNAT:
     * - SummaryDocuments (elemento raíz)
     *   - UBLExtensions (firma digital)
     *   - UBLVersionID
     *   - CustomizationID
     *   - ID (identificador del resumen)
     *   - ReferenceDate (fecha de los documentos)
     *   - IssueDate (fecha de generación)
     *   - Signature (referencia a la firma)
     *   - AccountingSupplierParty (datos del emisor)
     *   - SummaryDocumentsLine (detalle de cada boleta)
     */
    private static String crearXmlResumenDiario(CabeceraBean datosEmpresa, List<CabeceraBean> boletas,
                                                String unidadEnvio, Connection conn) {
        String resultado = "";

        try {
            ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");

            // ══════════════════════════════════════════════════════════════════
            // CARGAR CERTIFICADO DIGITAL
            // ══════════════════════════════════════════════════════════════════
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

            // ══════════════════════════════════════════════════════════════════
            // PREPARAR DOCUMENTO XML
            // ══════════════════════════════════════════════════════════════════
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.newDocument();

            // Datos para el nombre del archivo
            String ruc = datosEmpresa.getEmpr_nroruc();
            String fechaHoy = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fechaReferencia = datosEmpresa.getDocu_fecha(); // Fecha de las boletas
            int correlativo = 1;
            String identificador = "RC-" + fechaHoy + "-" + correlativo;

            String pathXMLFile = unidadEnvio + ruc + "-" + identificador + ".xml";
            File signatureFile = new File(pathXMLFile);

            log.info("Creando XML: " + pathXMLFile);
            CDATASection cdata;

            // ══════════════════════════════════════════════════════════════════════════════════════
            // ELEMENTO RAÍZ: SummaryDocuments
            // Namespace: urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element envelope = doc.createElementNS("", "SummaryDocuments");

            // Declaración de namespaces
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns",
                    "urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cac",
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cbc",
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds",
                    "http://www.w3.org/2000/09/xmldsig#");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ext",
                    "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:sac",
                    "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");

            envelope.appendChild(doc.createTextNode("\n"));
            doc.appendChild(envelope);

            // ══════════════════════════════════════════════════════════════════════════════════════
            // ext:UBLExtensions - Contenedor de la firma digital
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element UBLExtensions = doc.createElementNS("", "ext:UBLExtensions");
            envelope.appendChild(UBLExtensions);

            Element UBLExtension = doc.createElementNS("", "ext:UBLExtension");
            UBLExtensions.appendChild(UBLExtension);

            // ext:ExtensionContent - Contenido de la extensión (firma digital)
            Element ExtensionContent = doc.createElementNS("", "ext:ExtensionContent");
            UBLExtension.appendChild(ExtensionContent);

            // Crear firma digital
            String BaseURI = signatureFile.toURI().toURL().toString();
            XMLSignature sig = new XMLSignature(doc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA);
            ExtensionContent.appendChild(sig.getElement());

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cbc:UBLVersionID - Versión del estándar UBL
            // Valor fijo: "2.0"
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element UBLVersionID = doc.createElementNS("", "cbc:UBLVersionID");
            envelope.appendChild(UBLVersionID);
            UBLVersionID.appendChild(doc.createTextNode("2.0")); // Versión UBL 2.0

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cbc:CustomizationID - Versión de la estructura del documento
            // Valor fijo: "1.1" para resumen diario
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element CustomizationID = doc.createElementNS("", "cbc:CustomizationID");
            envelope.appendChild(CustomizationID);
            CustomizationID.appendChild(doc.createTextNode("1.1")); // Versión estructura SUNAT

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cbc:ID - Identificador del resumen
            // Formato: RC-YYYYMMDD-correlativo
            // RC = Resumen de Comprobantes
            // YYYYMMDD = Fecha de generación
            // correlativo = Número secuencial del día (1, 2, 3...)
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element ID = doc.createElementNS("", "cbc:ID");
            envelope.appendChild(ID);
            ID.appendChild(doc.createTextNode(identificador)); // Ej: RC-20260220-1

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cbc:ReferenceDate - Fecha de emisión de los documentos incluidos
            // Formato: YYYY-MM-DD
            // Debe ser la fecha de las boletas que se están resumiendo
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element ReferenceDate = doc.createElementNS("", "cbc:ReferenceDate");
            envelope.appendChild(ReferenceDate);
            ReferenceDate.appendChild(doc.createTextNode(fechaReferencia)); // Fecha de las boletas

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cbc:IssueDate - Fecha de generación del resumen
            // Formato: YYYY-MM-DD
            // Es la fecha actual (hoy)
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element IssueDate = doc.createElementNS("", "cbc:IssueDate");
            envelope.appendChild(IssueDate);
            String fechaEmision = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            IssueDate.appendChild(doc.createTextNode(fechaEmision)); // Fecha de hoy

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cac:Signature - Información de la firma digital
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element Signature = doc.createElementNS("", "cac:Signature");
            envelope.appendChild(Signature);

            // cbc:ID - Identificador de la firma (mismo que el ID del documento)
            Element SignatureID = doc.createElementNS("", "cbc:ID");
            Signature.appendChild(SignatureID);
            SignatureID.appendChild(doc.createTextNode(identificador));

            // cac:SignatoryParty - Datos del firmante
            Element SignatoryParty = doc.createElementNS("", "cac:SignatoryParty");
            Signature.appendChild(SignatoryParty);

            // cac:PartyIdentification - Identificación del firmante
            Element PartyIdentification = doc.createElementNS("", "cac:PartyIdentification");
            SignatoryParty.appendChild(PartyIdentification);

            // cbc:ID - RUC del emisor/firmante
            Element PartyID = doc.createElementNS("", "cbc:ID");
            PartyIdentification.appendChild(PartyID);
            PartyID.appendChild(doc.createTextNode(ruc)); // RUC del emisor

            // cac:PartyName - Nombre del firmante
            Element PartyName = doc.createElementNS("", "cac:PartyName");
            SignatoryParty.appendChild(PartyName);

            // cbc:Name - Razón social del emisor
            Element Name = doc.createElementNS("", "cbc:Name");
            PartyName.appendChild(Name);
            cdata = doc.createCDATASection(datosEmpresa.getEmpr_razonsocial());
            Name.appendChild(cdata);

            // cac:DigitalSignatureAttachment - Referencia a la firma
            Element DigitalSignatureAttachment = doc.createElementNS("", "cac:DigitalSignatureAttachment");
            Signature.appendChild(DigitalSignatureAttachment);

            Element ExternalReference = doc.createElementNS("", "cac:ExternalReference");
            DigitalSignatureAttachment.appendChild(ExternalReference);

            // cbc:URI - URI de referencia a la firma
            Element URI = doc.createElementNS("", "cbc:URI");
            ExternalReference.appendChild(URI);
            URI.appendChild(doc.createTextNode(identificador));

            // ══════════════════════════════════════════════════════════════════════════════════════
            // cac:AccountingSupplierParty - Datos del emisor
            // ══════════════════════════════════════════════════════════════════════════════════════
            Element AccountingSupplierParty = doc.createElementNS("", "cac:AccountingSupplierParty");
            envelope.appendChild(AccountingSupplierParty);

            // cbc:CustomerAssignedAccountID - RUC del emisor
            Element CustomerAssignedAccountID = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
            AccountingSupplierParty.appendChild(CustomerAssignedAccountID);
            CustomerAssignedAccountID.appendChild(doc.createTextNode(ruc)); // RUC

            // cbc:AdditionalAccountID - Tipo de documento del emisor
            // Catálogo 06: 6 = RUC
            Element AdditionalAccountID = doc.createElementNS("", "cbc:AdditionalAccountID");
            AccountingSupplierParty.appendChild(AdditionalAccountID);
            AdditionalAccountID.appendChild(doc.createTextNode("6")); // 6 = RUC

            // cac:Party - Información adicional del emisor
            Element Party = doc.createElementNS("", "cac:Party");
            AccountingSupplierParty.appendChild(Party);

            // cac:PartyLegalEntity - Entidad legal
            Element PartyLegalEntity = doc.createElementNS("", "cac:PartyLegalEntity");
            Party.appendChild(PartyLegalEntity);

            // cbc:RegistrationName - Razón social del emisor
            Element RegistrationName = doc.createElementNS("", "cbc:RegistrationName");
            PartyLegalEntity.appendChild(RegistrationName);
            cdata = doc.createCDATASection(datosEmpresa.getEmpr_razonsocial());
            RegistrationName.appendChild(cdata);

            // ══════════════════════════════════════════════════════════════════════════════════════
            // sac:SummaryDocumentsLine - Detalle de cada boleta (se repite por cada documento)
            // ══════════════════════════════════════════════════════════════════════════════════════
            int lineaNum = 1;
            for (CabeceraBean boleta : boletas) {

                log.info("Procesando boleta " + lineaNum + ": " + boleta.getDocu_numero() +
                        " - Estado: " + boleta.getEstadoBoleta());

                // sac:SummaryDocumentsLine - Línea del resumen
                Element SummaryDocumentsLine = doc.createElementNS("", "sac:SummaryDocumentsLine");
                envelope.appendChild(SummaryDocumentsLine);

                // ══════════════════════════════════════════════════════════════════════════════════
                // cbc:LineID - Número de línea (correlativo 1, 2, 3...)
                // ══════════════════════════════════════════════════════════════════════════════════
                Element LineID = doc.createElementNS("", "cbc:LineID");
                SummaryDocumentsLine.appendChild(LineID);
                LineID.appendChild(doc.createTextNode(String.valueOf(lineaNum))); // 1, 2, 3...

                // ══════════════════════════════════════════════════════════════════════════════════
                // cbc:DocumentTypeCode - Tipo de documento
                // Catálogo 01: 03 = Boleta de Venta
                // ══════════════════════════════════════════════════════════════════════════════════
                Element DocumentTypeCode = doc.createElementNS("", "cbc:DocumentTypeCode");
                SummaryDocumentsLine.appendChild(DocumentTypeCode);
                DocumentTypeCode.appendChild(doc.createTextNode(boleta.getDocu_tipodocumento())); // 03 = Boleta

                // ══════════════════════════════════════════════════════════════════════════════════
                // cbc:ID - Número completo del documento
                // Formato: B001-00000001
                // ══════════════════════════════════════════════════════════════════════════════════
                Element DocumentID = doc.createElementNS("", "cbc:ID");
                SummaryDocumentsLine.appendChild(DocumentID);
                DocumentID.appendChild(doc.createTextNode(boleta.getDocu_numero())); // B001-00000001

                // ══════════════════════════════════════════════════════════════════════════════════
                // cac:AccountingCustomerParty - Datos del cliente
                // ══════════════════════════════════════════════════════════════════════════════════
                Element AccountingCustomerParty = doc.createElementNS("", "cac:AccountingCustomerParty");
                SummaryDocumentsLine.appendChild(AccountingCustomerParty);

                // cbc:CustomerAssignedAccountID - Número de documento del cliente
                // DNI, CE, Pasaporte, etc.
                Element CustomerAccountID = doc.createElementNS("", "cbc:CustomerAssignedAccountID");
                AccountingCustomerParty.appendChild(CustomerAccountID);
                CustomerAccountID.appendChild(doc.createTextNode(boleta.getClie_numero())); // Nro documento

                // cbc:AdditionalAccountID - Tipo de documento del cliente
                // Catálogo 06: 1=DNI, 4=CE, 6=RUC, 7=Pasaporte, 0=Sin documento
                Element CustomerAccountType = doc.createElementNS("", "cbc:AdditionalAccountID");
                AccountingCustomerParty.appendChild(CustomerAccountType);
                CustomerAccountType.appendChild(doc.createTextNode(boleta.getClie_tipodoc())); // Tipo doc

                // ══════════════════════════════════════════════════════════════════════════════════
                // cac:Status - Estado del documento en el resumen
                // ══════════════════════════════════════════════════════════════════════════════════
                Element Status = doc.createElementNS("", "cac:Status");
                SummaryDocumentsLine.appendChild(Status);

                // cbc:ConditionCode - Código de condición
                // 1 = Adicionar (boleta nueva/despachada)
                // 2 = Modificar (corrección de datos)
                // 3 = Anular (dar de baja la boleta)
                Element ConditionCode = doc.createElementNS("", "cbc:ConditionCode");
                Status.appendChild(ConditionCode);

                // Determinar ConditionCode según estado de la boleta
                String conditionCodeValue = "1"; // Por defecto: Adicionar
                String estadoBoleta = boleta.getEstadoBoleta();
                if (estadoBoleta != null && estadoBoleta.equals("A")) {
                    conditionCodeValue = "3"; // Anular
                }
                ConditionCode.appendChild(doc.createTextNode(conditionCodeValue));

                // ══════════════════════════════════════════════════════════════════════════════════
                // sac:TotalAmount - Importe total del documento (incluye IGV)
                // Moneda: PEN (Soles)
                // ══════════════════════════════════════════════════════════════════════════════════
                Element TotalAmount = doc.createElementNS("", "sac:TotalAmount");
                TotalAmount.setAttributeNS(null, "currencyID", "PEN");
                SummaryDocumentsLine.appendChild(TotalAmount);
                TotalAmount.appendChild(doc.createTextNode(redondea(boleta.getDocu_total(), 2))); // Total con IGV

                // ══════════════════════════════════════════════════════════════════════════════════
                // sac:BillingPayment - Totales de la operación (se repite por tipo de operación)
                // ══════════════════════════════════════════════════════════════════════════════════

                // --- Operaciones Gravadas (si existe) ---
                if (boleta.getDocu_gravada() > 0) {
                    Element BillingPaymentGravada = doc.createElementNS("", "sac:BillingPayment");
                    SummaryDocumentsLine.appendChild(BillingPaymentGravada);

                    // cbc:PaidAmount - Monto de operaciones gravadas (base imponible)
                    Element PaidAmountGravada = doc.createElementNS("", "cbc:PaidAmount");
                    PaidAmountGravada.setAttributeNS(null, "currencyID", "PEN");
                    BillingPaymentGravada.appendChild(PaidAmountGravada);
                    PaidAmountGravada.appendChild(doc.createTextNode(redondea(boleta.getDocu_gravada(), 2)));

                    // cbc:InstructionID - Código de tipo de operación
                    // Catálogo 11: 01=Gravado, 02=Exonerado, 03=Inafecto, 05=Exportación
                    Element InstructionIDGravada = doc.createElementNS("", "cbc:InstructionID");
                    BillingPaymentGravada.appendChild(InstructionIDGravada);
                    InstructionIDGravada.appendChild(doc.createTextNode("01")); // 01 = Gravado
                }

                // --- Operaciones Exoneradas (si existe) ---
                if (boleta.getDocu_exonerada() > 0) {
                    Element BillingPaymentExonerada = doc.createElementNS("", "sac:BillingPayment");
                    SummaryDocumentsLine.appendChild(BillingPaymentExonerada);

                    Element PaidAmountExonerada = doc.createElementNS("", "cbc:PaidAmount");
                    PaidAmountExonerada.setAttributeNS(null, "currencyID", "PEN");
                    BillingPaymentExonerada.appendChild(PaidAmountExonerada);
                    PaidAmountExonerada.appendChild(doc.createTextNode(redondea(boleta.getDocu_exonerada(), 2)));

                    Element InstructionIDExonerada = doc.createElementNS("", "cbc:InstructionID");
                    BillingPaymentExonerada.appendChild(InstructionIDExonerada);
                    InstructionIDExonerada.appendChild(doc.createTextNode("02")); // 02 = Exonerado
                }

                // --- Operaciones Inafectas (si existe) ---
                if (boleta.getDocu_inafecta() > 0) {
                    Element BillingPaymentInafecta = doc.createElementNS("", "sac:BillingPayment");
                    SummaryDocumentsLine.appendChild(BillingPaymentInafecta);

                    Element PaidAmountInafecta = doc.createElementNS("", "cbc:PaidAmount");
                    PaidAmountInafecta.setAttributeNS(null, "currencyID", "PEN");
                    BillingPaymentInafecta.appendChild(PaidAmountInafecta);
                    PaidAmountInafecta.appendChild(doc.createTextNode(redondea(boleta.getDocu_inafecta(), 2)));

                    Element InstructionIDInafecta = doc.createElementNS("", "cbc:InstructionID");
                    BillingPaymentInafecta.appendChild(InstructionIDInafecta);
                    InstructionIDInafecta.appendChild(doc.createTextNode("03")); // 03 = Inafecto
                }

                // --- Operaciones Gratuitas (si existe) ---
                if (boleta.getDocu_gratuita() > 0) {
                    Element BillingPaymentGratuita = doc.createElementNS("", "sac:BillingPayment");
                    SummaryDocumentsLine.appendChild(BillingPaymentGratuita);

                    Element PaidAmountGratuita = doc.createElementNS("", "cbc:PaidAmount");
                    PaidAmountGratuita.setAttributeNS(null, "currencyID", "PEN");
                    BillingPaymentGratuita.appendChild(PaidAmountGratuita);
                    PaidAmountGratuita.appendChild(doc.createTextNode(redondea(boleta.getDocu_gratuita(), 2)));

                    Element InstructionIDGratuita = doc.createElementNS("", "cbc:InstructionID");
                    BillingPaymentGratuita.appendChild(InstructionIDGratuita);
                    InstructionIDGratuita.appendChild(doc.createTextNode("05")); // 05 = Gratuito
                }

                // ══════════════════════════════════════════════════════════════════════════════════
                // cac:TaxTotal - Total de tributos (IGV)
                // ══════════════════════════════════════════════════════════════════════════════════
                Element TaxTotal = doc.createElementNS("", "cac:TaxTotal");
                SummaryDocumentsLine.appendChild(TaxTotal);

                // cbc:TaxAmount - Monto total del IGV
                Element TaxAmount = doc.createElementNS("", "cbc:TaxAmount");
                TaxAmount.setAttributeNS(null, "currencyID", "PEN");
                TaxTotal.appendChild(TaxAmount);
                TaxAmount.appendChild(doc.createTextNode(redondea(boleta.getDocu_igv(), 2))); // Monto IGV

                // cac:TaxSubtotal - Subtotal del tributo
                Element TaxSubtotal = doc.createElementNS("", "cac:TaxSubtotal");
                TaxTotal.appendChild(TaxSubtotal);

                // cbc:TaxAmount (repetido en subtotal)
                Element TaxAmountSub = doc.createElementNS("", "cbc:TaxAmount");
                TaxAmountSub.setAttributeNS(null, "currencyID", "PEN");
                TaxSubtotal.appendChild(TaxAmountSub);
                TaxAmountSub.appendChild(doc.createTextNode(redondea(boleta.getDocu_igv(), 2)));

                // cac:TaxCategory - Categoría del tributo
                Element TaxCategory = doc.createElementNS("", "cac:TaxCategory");
                TaxSubtotal.appendChild(TaxCategory);

                // cac:TaxScheme - Esquema del tributo
                Element TaxScheme = doc.createElementNS("", "cac:TaxScheme");
                TaxCategory.appendChild(TaxScheme);

                // cbc:ID - Código del tributo
                // Catálogo 05: 1000=IGV, 2000=ISC, 9999=Otros
                Element TaxSchemeID = doc.createElementNS("", "cbc:ID");
                TaxScheme.appendChild(TaxSchemeID);
                TaxSchemeID.appendChild(doc.createTextNode("1000")); // 1000 = IGV

                // cbc:Name - Nombre del tributo
                Element TaxSchemeName = doc.createElementNS("", "cbc:Name");
                TaxScheme.appendChild(TaxSchemeName);
                TaxSchemeName.appendChild(doc.createTextNode("IGV")); // Nombre del tributo

                // cbc:TaxTypeCode - Código internacional del tipo de tributo
                // VAT = Value Added Tax (IGV)
                Element TaxTypeCode = doc.createElementNS("", "cbc:TaxTypeCode");
                TaxScheme.appendChild(TaxTypeCode);
                TaxTypeCode.appendChild(doc.createTextNode("VAT")); // VAT = IGV

                // ══════════════════════════════════════════════════════════════════════════════════
                // cac:TaxTotal - ICBPER (si existe)
                // Catálogo 05: 7152=ICBPER
                // ══════════════════════════════════════════════════════════════════════════════════
                if (boleta.getDocu_otrostributos() > 0) {
                    Element TaxTotalICBPER = doc.createElementNS("", "cac:TaxTotal");
                    SummaryDocumentsLine.appendChild(TaxTotalICBPER);

                    Element TaxAmountICBPER = doc.createElementNS("", "cbc:TaxAmount");
                    TaxAmountICBPER.setAttributeNS(null, "currencyID", "PEN");
                    TaxTotalICBPER.appendChild(TaxAmountICBPER);
                    TaxAmountICBPER.appendChild(doc.createTextNode(redondea(boleta.getDocu_otrostributos(), 2)));

                    Element TaxSubtotalICBPER = doc.createElementNS("", "cac:TaxSubtotal");
                    TaxTotalICBPER.appendChild(TaxSubtotalICBPER);

                    Element TaxAmountSubICBPER = doc.createElementNS("", "cbc:TaxAmount");
                    TaxAmountSubICBPER.setAttributeNS(null, "currencyID", "PEN");
                    TaxSubtotalICBPER.appendChild(TaxAmountSubICBPER);
                    TaxAmountSubICBPER.appendChild(doc.createTextNode(redondea(boleta.getDocu_otrostributos(), 2)));

                    Element TaxCategoryICBPER = doc.createElementNS("", "cac:TaxCategory");
                    TaxSubtotalICBPER.appendChild(TaxCategoryICBPER);

                    Element TaxSchemeICBPER = doc.createElementNS("", "cac:TaxScheme");
                    TaxCategoryICBPER.appendChild(TaxSchemeICBPER);

                    Element TaxSchemeIDICBPER = doc.createElementNS("", "cbc:ID");
                    TaxSchemeICBPER.appendChild(TaxSchemeIDICBPER);
                    TaxSchemeIDICBPER.appendChild(doc.createTextNode("7152")); // 7152 = ICBPER

                    Element TaxSchemeNameICBPER = doc.createElementNS("", "cbc:Name");
                    TaxSchemeICBPER.appendChild(TaxSchemeNameICBPER);
                    TaxSchemeNameICBPER.appendChild(doc.createTextNode("ICBPER"));

                    Element TaxTypeCodeICBPER = doc.createElementNS("", "cbc:TaxTypeCode");
                    TaxSchemeICBPER.appendChild(TaxTypeCodeICBPER);
                    TaxTypeCodeICBPER.appendChild(doc.createTextNode("OTH")); // OTH = Otros tributos
                }

                lineaNum++;
            }

            // ══════════════════════════════════════════════════════════════════════════════════════
            // FIRMAR DOCUMENTO
            // ══════════════════════════════════════════════════════════════════════════════════════
            log.info("Firmando documento...");
            sig.setId("Sign" + ruc);
            sig.addKeyInfo(cert);

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);

            sig.sign(privateKey);

            // ══════════════════════════════════════════════════════════════════════════════════════
            // GUARDAR XML
            // ══════════════════════════════════════════════════════════════════════════════════════
            FileOutputStream f = new FileOutputStream(signatureFile);
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            tf.setOutputProperty(OutputKeys.STANDALONE, "no");
            StreamResult sr = new StreamResult(f);
            tf.transform(new DOMSource(doc), sr);
            sr.getOutputStream().close();

            log.info("XML creado exitosamente: " + pathXMLFile);

            // ══════════════════════════════════════════════════════════════════════════════════════
            // CREAR ZIP
            // ══════════════════════════════════════════════════════════════════════════════════════
            resultado = GeneralFunctions.crearZip2(datosEmpresa, unidadEnvio, signatureFile);

        } catch (Exception ex) {
            ex.printStackTrace();
            resultado = "0100|Error al crear XML del resumen: " + ex.getMessage();
            log.error("Error crearXmlResumenDiario: " + ex.toString());
        }

        return resultado;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * ENVIAR ZIP A SUNAT - Método sendSummary
     * ══════════════════════════════════════════════════════════════════════════
     */
    public static String[] enviarZipASunat(String path, String zipFileName, String vruc) {
        String[] resultado = new String[2];
        resultado[0] = "";
        resultado[1] = "";

        log.info("Enviando a SUNAT: " + zipFileName);

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

        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            String mensaje = ConversionUtils.extraerMensajeSOAPFault(ex);
            String codigo = ConversionUtils.extraerCodigoErrorSUNAT(ex);
            resultado[0] = "ERROR";
            resultado[1] = (codigo != null ? codigo : "ERROR") + "|" + mensaje;
            log.error("Error SOAP: " + mensaje);
        } catch (Exception e) {
            e.printStackTrace();
            resultado[1] = "nulo";
            log.error("Error enviarZipASunat: " + e.toString());
        }

        return resultado;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * CONSULTAR ESTADO DEL TICKET - Método getStatus
     * ══════════════════════════════════════════════════════════════════════════
     */
    public static String[] pedirStatus(String path, String zipFileName, String vruc, String ticket) {
        String[] resultado = new String[2];
        resultado[0] = "";
        resultado[1] = "";

        log.info("Consultando ticket: " + ticket);

        try {
            StatusResponse respuestaSunat = null;

            switch (AMBIENTE_SUNAT) {
                case "1":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe ws1 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService_Service_fe();
                    HeaderHandlerResolver handlerResolver1 = new HeaderHandlerResolver();
                    handlerResolver1.setVruc(vruc);
                    ws1.setHandlerResolver(handlerResolver1);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.service_bta.BillService port1 = ws1.getBillServicePort();
                    respuestaSunat = port1.getStatus(ticket);
                    break;
                case "2":
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa ws2 =
                            new pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService_Service_sqa();
                    HeaderHandlerResolver handlerResolver2 = new HeaderHandlerResolver();
                    handlerResolver2.setVruc(vruc);
                    ws2.setHandlerResolver(handlerResolver2);
                    pe.gob.sunat.servicio.registro.comppago.factura.gem.servicesqa.BillService port2 = ws2.getBillServicePort();
                    respuestaSunat = port2.getStatus(ticket);
                    break;
                case "3":
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
                // Guardar y descomprimir CDR
                FileOutputStream fos = new FileOutputStream(UNIDAD_RESPUESTA + "R-" + zipFileName);
                fos.write(respuestaSunat.getContent());
                fos.close();

                ZipFile archive = new ZipFile(UNIDAD_RESPUESTA + "R-" + zipFileName);
                Enumeration e = archive.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    File file = new File(UNIDAD_RESPUESTA, entry.getName());
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

                String xmlFileName = zipFileName.replace(".zip", ".xml");
                resultado[1] = LecturaXML.getRespuestaSunat(UNIDAD_RESPUESTA + "R-" + xmlFileName);
                resultado[0] = ticket;

                log.info("CDR obtenido: " + resultado[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error pedirStatus: " + e.toString());
        }

        return resultado;
    }

    /**
     * Redondea un número a los decimales especificados
     */
    public static String redondea(double numero, int decimales) {
        DecimalFormat f = new DecimalFormat("0.00");
        BigDecimal res = new BigDecimal(numero).setScale(decimales, BigDecimal.ROUND_HALF_UP);
        return f.format(res.doubleValue()).replace(",", ".");
    }
}