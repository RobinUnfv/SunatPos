package Modelo.Beans;

/**
 * ══════════════════════════════════════════════════════════════════════════════════════
 * BEAN DE CONFIGURACIÓN SUNAT - ConfigSunatBean.java
 * ══════════════════════════════════════════════════════════════════════════════════════
 *
 * Contiene todos los valores de configuración cargados desde FACTU.CONFIG_SUNAT
 * y datos de empresa desde FACTU.ARFAMC.
 *
 * Este bean se carga una sola vez y se usa en todas las clases de facturación electrónica.
 *
 * ══════════════════════════════════════════════════════════════════════════════════════
 */
public class ConfigSunatBean {

    // ══════════════════════════════════════════════════════════════════════════
    // RUTAS
    // ══════════════════════════════════════════════════════════════════════════
    private String rutaEnvio;
    private String rutaRespuesta;
    private String rutaCertificado;

    // ══════════════════════════════════════════════════════════════════════════
    // CERTIFICADO
    // ══════════════════════════════════════════════════════════════════════════
    private String keystoreType;
    private String keystorePass;
    private String privateKeyPass;

    // ══════════════════════════════════════════════════════════════════════════
    // AMBIENTE SUNAT
    // ══════════════════════════════════════════════════════════════════════════
    private String ambienteSunat;      // 1=Beta, 2=QA, 3=Producción
    private String enviarSunat;        // S=Sí, N=No

    // ══════════════════════════════════════════════════════════════════════════
    // CREDENCIALES BETA
    // ══════════════════════════════════════════════════════════════════════════
    private String sunatBetaUsuario;
    private String sunatBetaClave;
    private String sunatBetaUrl;

    // ══════════════════════════════════════════════════════════════════════════
    // CREDENCIALES PRODUCCIÓN
    // ══════════════════════════════════════════════════════════════════════════
    private String sunatProdUsuario;
    private String sunatProdClave;
    private String sunatProdUrl;

    // ══════════════════════════════════════════════════════════════════════════
    // EMPRESA (de FACTU.ARFAMC)
    // ══════════════════════════════════════════════════════════════════════════
    private String empresaRuc;
    private String empresaRazonSocial;
    private double porcentajeIgv;

    // ══════════════════════════════════════════════════════════════════════════
    // LÍMITES
    // ══════════════════════════════════════════════════════════════════════════
    private int maxBoletasResumen;
    private int maxReintentos;
    private int timeoutConexion;

    // ══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════════

    public ConfigSunatBean() {
        // Valores por defecto (se usarán si falla la carga desde BD)
        this.rutaEnvio = "d:\\POS-SUNAT\\envio\\";
        this.rutaRespuesta = "d:\\POS-SUNAT\\respuesta\\";
        this.rutaCertificado = "d:\\POS-SUNAT\\certificado.jks";
        this.keystoreType = "JKS";
        this.keystorePass = "123456789";
        this.privateKeyPass = "CORPTEx2218";
        this.ambienteSunat = "1";
        this.enviarSunat = "S";
        this.sunatBetaUsuario = "20609272016MODDATOS";
        this.sunatBetaClave = "MODDATOS";
        this.sunatBetaUrl = "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService";
        this.sunatProdUsuario = "20609272016CORPOTCE";
        this.sunatProdClave = "DRAVErFACEL2";
        this.sunatProdUrl = "https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService";
        this.maxBoletasResumen = 100;
        this.maxReintentos = 3;
        this.timeoutConexion = 60000;
        this.porcentajeIgv = 18.0;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE UTILIDAD
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Verifica si está en ambiente Beta
     */
    public boolean isAmbienteBeta() {
        return "1".equals(ambienteSunat);
    }

    /**
     * Verifica si está en ambiente Producción
     */
    public boolean isAmbienteProduccion() {
        return "3".equals(ambienteSunat);
    }

    /**
     * Verifica si se debe enviar a SUNAT
     */
    public boolean isEnviarSunat() {
        return "S".equalsIgnoreCase(enviarSunat);
    }

    /**
     * Obtiene el usuario SUNAT según el ambiente activo
     */
    public String getUsuarioSunatActivo() {
        return isAmbienteProduccion() ? sunatProdUsuario : sunatBetaUsuario;
    }

    /**
     * Obtiene la clave SUNAT según el ambiente activo
     */
    public String getClaveSunatActiva() {
        return isAmbienteProduccion() ? sunatProdClave : sunatBetaClave;
    }

    /**
     * Obtiene la URL SUNAT según el ambiente activo
     */
    public String getUrlSunatActiva() {
        return isAmbienteProduccion() ? sunatProdUrl : sunatBetaUrl;
    }

    /**
     * Obtiene el nombre del ambiente actual
     */
    public String getNombreAmbiente() {
        switch (ambienteSunat) {
            case "1": return "BETA";
            case "2": return "HOMOLOGACION";
            case "3": return "PRODUCCION";
            default: return "DESCONOCIDO";
        }
    }

    /**
     * Imprime la configuración (para debug)
     */
    public void imprimirConfiguracion() {
        System.out.println("══════════════════════════════════════════════════════════════════");
        System.out.println("CONFIGURACIÓN SUNAT CARGADA");
        System.out.println("══════════════════════════════════════════════════════════════════");
        System.out.println("Ambiente       : " + getNombreAmbiente() + " (" + ambienteSunat + ")");
        System.out.println("Usuario SUNAT  : " + getUsuarioSunatActivo());
        System.out.println("RUC Empresa    : " + empresaRuc);
        System.out.println("Razón Social   : " + empresaRazonSocial);
        System.out.println("IGV            : " + porcentajeIgv + "%");
        System.out.println("──────────────────────────────────────────────────────────────────");
        System.out.println("Ruta Envío     : " + rutaEnvio);
        System.out.println("Ruta Respuesta : " + rutaRespuesta);
        System.out.println("Ruta Certif.   : " + rutaCertificado);
        System.out.println("══════════════════════════════════════════════════════════════════");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ══════════════════════════════════════════════════════════════════════════

    public String getRutaEnvio() {
        return rutaEnvio;
    }

    public void setRutaEnvio(String rutaEnvio) {
        this.rutaEnvio = rutaEnvio;
    }

    public String getRutaRespuesta() {
        return rutaRespuesta;
    }

    public void setRutaRespuesta(String rutaRespuesta) {
        this.rutaRespuesta = rutaRespuesta;
    }

    public String getRutaCertificado() {
        return rutaCertificado;
    }

    public void setRutaCertificado(String rutaCertificado) {
        this.rutaCertificado = rutaCertificado;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getPrivateKeyPass() {
        return privateKeyPass;
    }

    public void setPrivateKeyPass(String privateKeyPass) {
        this.privateKeyPass = privateKeyPass;
    }

    public String getAmbienteSunat() {
        return ambienteSunat;
    }

    public void setAmbienteSunat(String ambienteSunat) {
        this.ambienteSunat = ambienteSunat;
    }

    public String getEnviarSunat() {
        return enviarSunat;
    }

    public void setEnviarSunat(String enviarSunat) {
        this.enviarSunat = enviarSunat;
    }

    public String getSunatBetaUsuario() {
        return sunatBetaUsuario;
    }

    public void setSunatBetaUsuario(String sunatBetaUsuario) {
        this.sunatBetaUsuario = sunatBetaUsuario;
    }

    public String getSunatBetaClave() {
        return sunatBetaClave;
    }

    public void setSunatBetaClave(String sunatBetaClave) {
        this.sunatBetaClave = sunatBetaClave;
    }

    public String getSunatBetaUrl() {
        return sunatBetaUrl;
    }

    public void setSunatBetaUrl(String sunatBetaUrl) {
        this.sunatBetaUrl = sunatBetaUrl;
    }

    public String getSunatProdUsuario() {
        return sunatProdUsuario;
    }

    public void setSunatProdUsuario(String sunatProdUsuario) {
        this.sunatProdUsuario = sunatProdUsuario;
    }

    public String getSunatProdClave() {
        return sunatProdClave;
    }

    public void setSunatProdClave(String sunatProdClave) {
        this.sunatProdClave = sunatProdClave;
    }

    public String getSunatProdUrl() {
        return sunatProdUrl;
    }

    public void setSunatProdUrl(String sunatProdUrl) {
        this.sunatProdUrl = sunatProdUrl;
    }

    public String getEmpresaRuc() {
        return empresaRuc;
    }

    public void setEmpresaRuc(String empresaRuc) {
        this.empresaRuc = empresaRuc;
    }

    public String getEmpresaRazonSocial() {
        return empresaRazonSocial;
    }

    public void setEmpresaRazonSocial(String empresaRazonSocial) {
        this.empresaRazonSocial = empresaRazonSocial;
    }

    public double getPorcentajeIgv() {
        return porcentajeIgv;
    }

    public void setPorcentajeIgv(double porcentajeIgv) {
        this.porcentajeIgv = porcentajeIgv;
    }

    public int getMaxBoletasResumen() {
        return maxBoletasResumen;
    }

    public void setMaxBoletasResumen(int maxBoletasResumen) {
        this.maxBoletasResumen = maxBoletasResumen;
    }

    public int getMaxReintentos() {
        return maxReintentos;
    }

    public void setMaxReintentos(int maxReintentos) {
        this.maxReintentos = maxReintentos;
    }

    public int getTimeoutConexion() {
        return timeoutConexion;
    }

    public void setTimeoutConexion(int timeoutConexion) {
        this.timeoutConexion = timeoutConexion;
    }
}