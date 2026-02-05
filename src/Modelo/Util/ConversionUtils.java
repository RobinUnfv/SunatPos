package Modelo.Util;

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
}
