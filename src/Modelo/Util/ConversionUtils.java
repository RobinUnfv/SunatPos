package Modelo.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ConversionUtils {

    /** Convierte tipo documento Oracle->SUNAT: F->01, B->03, C->07, D->08 */
    public static String convertirTipoDoc(String tipoDocOracle) {
        if (tipoDocOracle == null || tipoDocOracle.isEmpty()) return "01";
        switch (tipoDocOracle.toUpperCase().trim()) {
            case "F": return "01";
            case "B": return "03";
            case "NC": return "07";
            case "ND": return "08";
            default:  return "01";
        }
    }

    /** Convierte tipo documento SUNAT->Oracle: 01->F, 03->B, 07->C, 08->D */
    public static String convertirTipoDocInverso(String tipoDocSunat) {
        if (tipoDocSunat == null) return "F";
        switch (tipoDocSunat) {
            case "01": return "F";
            case "03": return "B";
            case "07": return "C";
            case "08": return "D";
            default:   return "F";
        }
    }

    /** Convierte tipo doc identidad: DNI->1, RUC->6, CE->4, PAS->7 */
    public static String convertirTipoDocIdentidad(String tipoDocIdentidad) {
        if (tipoDocIdentidad == null || tipoDocIdentidad.isEmpty()) return "0";
        switch (tipoDocIdentidad.toUpperCase().trim()) {
            case "DNI": return "1";
            case "RUC": return "6";
            case "CE":  return "4";
            case "PAS": return "7";
            case "1": case "6": case "4": case "7": return tipoDocIdentidad;
            default: return "0";
        }
    }

    /** Determina tipo doc por longitud: 11 dígitos=RUC, 8 dígitos=DNI */
    public static String determinarTipoDocPorLongitud(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isEmpty()) return "0";
        String num = numeroDocumento.replaceAll("[^0-9]", "");
        if (num.length() == 11) return "6";
        if (num.length() == 8) return "1";
        if (num.length() == 9 || num.length() == 12) return "4";
        return "0";
    }

    /** Convierte moneda: SOL->PEN, DOL->USD */
    public static String convertirMoneda(String monedaOracle) {
        if (monedaOracle == null || monedaOracle.isEmpty()) return "PEN";
        switch (monedaOracle.toUpperCase().trim()) {
            case "SOL": case "S/.": case "PEN": return "PEN";
            case "DOL": case "USD": case "$": return "USD";
            default: return "PEN";
        }
    }

    /** Convierte tipo afectación IGV */
    public static String convertirTipoAfectacion(String tipoAfectacion) {
        if (tipoAfectacion == null || tipoAfectacion.isEmpty()) return "10";
        String tipo = tipoAfectacion.trim();
        if (tipo.matches("^(10|11|12|13|14|15|16|17|20|21|30|31|32|33|34|35|36|40)$")) {
            return tipo;
        }
        if (tipo.length() == 1) {
            switch (tipo) {
                case "1": return "10";
                case "2": return "20";
                case "3": return "30";
                case "4": return "40";
            }
        }
        return "10";
    }

    /** Determina si operación es gratuita */
    public static boolean esOperacionGratuita(String tipoAfectacion) {
        if (tipoAfectacion == null) return false;
        String tipo = tipoAfectacion.trim();
        return tipo.equals("11") || tipo.equals("12") || tipo.equals("13") ||
                tipo.equals("14") || tipo.equals("15") || tipo.equals("16") ||
                tipo.equals("21") || tipo.equals("31") || tipo.equals("32") ||
                tipo.equals("33") || tipo.equals("34") || tipo.equals("35") || tipo.equals("36");
    }

    /** Obtiene código tributo: 10-17->1000(IGV), 20-21->9997(EXO), 30-36->9998(INA) */
    public static String obtenerCodigoTributo(String tipoAfectacion) {
        if (tipoAfectacion == null || tipoAfectacion.isEmpty()) return "1000";
        String tipo = tipoAfectacion.trim();
        int codigo = Integer.parseInt(tipo.length() == 1 ? tipo + "0" : tipo);
        if ((codigo >= 10 && codigo <= 17) || codigo == 40) return "1000";
        if (codigo >= 20 && codigo <= 21) return "9997";
        if (codigo >= 30 && codigo <= 36) return "9998";
        if (esOperacionGratuita(tipo)) return "9996";
        return "1000";
    }

    /** Formatea número documento: F0010000097 -> F001-0000097 */
    public static String formatearNumeroDocumento(String noFactu) {
        if (noFactu == null || noFactu.isEmpty()) return "";
        String numero = noFactu.trim();
        if (numero.contains("-")) return numero;
        if (numero.length() >= 5) {
            return numero.substring(0, 4) + "-" + numero.substring(4);
        }
        return numero;
    }

    /** Genera ID externo SUNAT: RUC-TIPO-SERIE-CORRELATIVO */
    public static String generarIdExterno(String ruc, String tipoDocOracle, String noFactu) {
        String tipoSunat = convertirTipoDoc(tipoDocOracle);
        String numeroFormateado = formatearNumeroDocumento(noFactu);
        return ruc + "-" + tipoSunat + "-" + numeroFormateado;
    }

    public static double redondear2Decimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static String formatoDosDecimales(double numero) {
        // Redondeo a 2 decimales
        BigDecimal bd = new BigDecimal(numero);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        // Formatear con punto como separador decimal
        DecimalFormat df = new DecimalFormat("0.00");
        df.setDecimalSeparatorAlwaysShown(true);

        return df.format(bd.doubleValue()).replace(",", ".");
    }

    public static String convertirTextoDosDecimales(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "0.00";
        }

        try {
            // Limpiar el texto
            String textoLimpio = texto.trim()
                    .replace(",", ".")       // Reemplazar coma por punto
                    .replace(" ", "")        // Eliminar espacios
                    .replace("$", "")        // Eliminar símbolo de moneda
                    .replace("€", "")
                    .replace("S/", "")
                    .replace("USD", "");

            // Validar que sea un número
            if (!textoLimpio.matches("-?\\d+(\\.\\d+)?")) {
                return "0.00";
            }

            // Convertir a double
            double numero = Double.parseDouble(textoLimpio);

            // Formatear a 2 decimales
            DecimalFormat df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.HALF_UP);

            return df.format(numero).replace(",", ".");

        } catch (Exception e) {
            return "0.00";
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÉTODOS PARA MANEJO DE ERRORES SOAP DE SUNAT
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Extrae el mensaje de detalle de un SOAPFaultException.
     *
     * @param ex SOAPFaultException capturado
     * @return String con el mensaje de error detallado
     */
    public static String extraerMensajeSOAPFault(javax.xml.ws.soap.SOAPFaultException ex) {
        if (ex == null) return "Error desconocido";

        try {
            // Intentar obtener el detalle del fault
            javax.xml.soap.SOAPFault fault = ex.getFault();
            if (fault != null) {
                // Obtener el mensaje principal
                String faultString = fault.getFaultString();

                // Intentar obtener detalles adicionales
                javax.xml.soap.Detail detail = fault.getDetail();
                if (detail != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(faultString != null ? faultString : "");

                    java.util.Iterator<?> it = detail.getDetailEntries();
                    while (it.hasNext()) {
                        javax.xml.soap.DetailEntry entry = (javax.xml.soap.DetailEntry) it.next();
                        sb.append(" | ").append(entry.getTextContent());
                    }
                    return sb.toString();
                }

                return faultString != null ? faultString : ex.getMessage();
            }
        } catch (Exception e) {
            // Si falla, usar el mensaje básico
        }

        return ex.getMessage() != null ? ex.getMessage() : "Error SOAP sin mensaje";
    }

    /**
     * Extrae el código de error de SUNAT del SOAPFaultException.
     * Busca patrones como "errorCode 2021" o "código: 2021"
     *
     * @param ex SOAPFaultException capturado
     * @return String con el código de error o null si no se encuentra
     */
    public static String extraerCodigoErrorSUNAT(javax.xml.ws.soap.SOAPFaultException ex) {
        String mensaje = extraerMensajeSOAPFault(ex);
        if (mensaje == null) return null;

        // Buscar patrones comunes de código de error
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?:errorCode|código|code|error)[:\\s]*(\\d{4})",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(mensaje);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Buscar solo números de 4 dígitos al inicio
        pattern = java.util.regex.Pattern.compile("^(\\d{4})");
        matcher = pattern.matcher(mensaje.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Formatea el error SOAP para mostrar/guardar en base de datos.
     * Formato: "CODIGO|MENSAJE"
     *
     * @param ex SOAPFaultException capturado
     * @return String formateado "CODIGO|MENSAJE"
     */
    public static String formatearErrorSOAP(javax.xml.ws.soap.SOAPFaultException ex) {
        String codigo = extraerCodigoErrorSUNAT(ex);
        String mensaje = extraerMensajeSOAPFault(ex);

        if (codigo != null) {
            return codigo + "|" + mensaje;
        }
        return "ERROR|" + mensaje;
    }

    /**
     * Extrae información completa del SOAPFaultException en un arreglo.
     *
     * @param ex SOAPFaultException capturado
     * @return String[] {codigo, mensaje, faultCode, faultActor}
     */
    public static String[] extraerInfoCompletaSOAPFault(javax.xml.ws.soap.SOAPFaultException ex) {
        String[] info = new String[4];
        info[0] = ""; // código
        info[1] = ""; // mensaje
        info[2] = ""; // faultCode
        info[3] = ""; // faultActor

        if (ex == null) return info;

        try {
            javax.xml.soap.SOAPFault fault = ex.getFault();
            if (fault != null) {
                info[1] = fault.getFaultString() != null ? fault.getFaultString() : "";
                info[2] = fault.getFaultCode() != null ? fault.getFaultCode() : "";
                info[3] = fault.getFaultActor() != null ? fault.getFaultActor() : "";
            }
        } catch (Exception e) {
            info[1] = ex.getMessage() != null ? ex.getMessage() : "";
        }

        info[0] = extraerCodigoErrorSUNAT(ex);
        if (info[0] == null) info[0] = "";

        return info;
    }

    /**
     * Verifica si el error SOAP es un error de validación de XML (códigos 2xxx).
     *
     * @param ex SOAPFaultException capturado
     * @return true si es error de validación XML
     */
    public static boolean esErrorValidacionXML(javax.xml.ws.soap.SOAPFaultException ex) {
        String codigo = extraerCodigoErrorSUNAT(ex);
        if (codigo != null && codigo.length() == 4) {
            return codigo.startsWith("2");
        }
        return false;
    }

    /**
     * Verifica si el error SOAP es un error de firma digital (códigos 3xxx).
     *
     * @param ex SOAPFaultException capturado
     * @return true si es error de firma
     */
    public static boolean esErrorFirmaDigital(javax.xml.ws.soap.SOAPFaultException ex) {
        String codigo = extraerCodigoErrorSUNAT(ex);
        if (codigo != null && codigo.length() == 4) {
            return codigo.startsWith("3");
        }
        return false;
    }

    public static String[] codigoRespuesta(String texto) {
        if (texto == null || !texto.contains("|")) {
            return new String[]{texto, ""};
        }
        return texto.split("\\|", 2);
    }

}
