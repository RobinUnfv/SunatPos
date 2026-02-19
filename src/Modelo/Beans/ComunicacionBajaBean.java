package Modelo.Beans;

import java.util.Date;

/**
 * Bean para representar un documento a dar de baja
 * Mapea la tabla FACTU.COMUNICACION_BAJA
 */
public class ComunicacionBajaBean {

    private String noCia;           // NO_CIA
    private String noFactu;         // NO_FACTU (número completo: F001-00000123)
    private Date fecEmision;        // FEC_EMISION
    private Date fecBaja;           // FEC_BAJA
    private String codMotivo;       // COD_MOTIVO
    private String descMotivo;      // DESC_MOTIVO
    private String estado;          // ESTADO (N, B, P, E, X)
    private String nroCorrelativo;  // NRO_CORRELATIVO
    private String ticketSunat;     // TICKET_SUNAT
    private String cdrSunat;        // CDR_SUNAT

    // Campos adicionales para el XML
    private String tipoDocumento;   // 01=Factura, 03=Boleta, 07=NC, 08=ND
    private String serie;           // Serie (F001, B001, etc.)
    private String numero;          // Número correlativo (00000123)

    // Datos de la empresa (para el XML)
    private String emprRuc;
    private String emprRazonSocial;
    private String emprTipoDoc;

    // Constructor vacío
    public ComunicacionBajaBean() {
    }

    // Getters y Setters
    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getNoFactu() {
        return noFactu;
    }

    public void setNoFactu(String noFactu) {
        this.noFactu = noFactu;
    }

    public Date getFecEmision() {
        return fecEmision;
    }

    public void setFecEmision(Date fecEmision) {
        this.fecEmision = fecEmision;
    }

    public Date getFecBaja() {
        return fecBaja;
    }

    public void setFecBaja(Date fecBaja) {
        this.fecBaja = fecBaja;
    }

    public String getCodMotivo() {
        return codMotivo;
    }

    public void setCodMotivo(String codMotivo) {
        this.codMotivo = codMotivo;
    }

    public String getDescMotivo() {
        return descMotivo;
    }

    public void setDescMotivo(String descMotivo) {
        this.descMotivo = descMotivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNroCorrelativo() {
        return nroCorrelativo;
    }

    public void setNroCorrelativo(String nroCorrelativo) {
        this.nroCorrelativo = nroCorrelativo;
    }

    public String getTicketSunat() {
        return ticketSunat;
    }

    public void setTicketSunat(String ticketSunat) {
        this.ticketSunat = ticketSunat;
    }

    public String getCdrSunat() {
        return cdrSunat;
    }

    public void setCdrSunat(String cdrSunat) {
        this.cdrSunat = cdrSunat;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getEmprRuc() {
        return emprRuc;
    }

    public void setEmprRuc(String emprRuc) {
        this.emprRuc = emprRuc;
    }

    public String getEmprRazonSocial() {
        return emprRazonSocial;
    }

    public void setEmprRazonSocial(String emprRazonSocial) {
        this.emprRazonSocial = emprRazonSocial;
    }

    public String getEmprTipoDoc() {
        return emprTipoDoc;
    }

    public void setEmprTipoDoc(String emprTipoDoc) {
        this.emprTipoDoc = emprTipoDoc;
    }

    /**
     * Obtiene el número de documento formateado para SUNAT (con guión)
     * Ejemplo: F001-00000123
     */
    public String getNumeroFormateado() {
        if (serie != null && numero != null) {
            return serie + "-" + numero;
        }
        return noFactu;
    }

    @Override
    public String toString() {
        return "ComunicacionBajaBean{" +
                "noFactu='" + noFactu + '\'' +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", serie='" + serie + '\'' +
                ", numero='" + numero + '\'' +
                ", descMotivo='" + descMotivo + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
