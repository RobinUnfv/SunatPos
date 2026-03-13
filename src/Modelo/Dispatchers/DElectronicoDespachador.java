
package Modelo.Dispatchers;

import Modelo.Beans.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import Modelo.Util.ConversionUtils;


public class DElectronicoDespachador {

    //private static Log log = LogFactory.getLog(DElectronicoDespachador.class);
    private static String NO_CIA_DEFAULT = "01";

    public static CabeceraBean pendienteDocElectronico(Connection conn) {
        System.out.println("ENTRO A pendienteDocElectronico");
        CabeceraBean cabeceraBean = new CabeceraBean();
        try {
            /*
            String sql = "SELECT A.NO_CIA, A.TIPO_DOC, A.NO_FACTU, A.FECHA, " +
                    "TO_CHAR(A.FEC_CREA, 'HH24:MI:SS') AS HORA, " +
                    "A.NO_CLIENTE, A.NBR_CLIENTE, CXC.PR_CLIENTE.GET_DIRECCION(A.NO_CIA, A.NO_CLIENTE) AS DIRECCION, " +
                    "A.TIPO_DOC_CLI, A.NUM_DOC_CLI, A.RUC, " +
                    "A.MONEDA, A.VALOR_VENTA, A.SUB_TOTAL, A.IMPUESTO, A.TOTAL, " +
                    "A.T_DESCUENTO, A.OPER_GRAVADAS, A.OPER_EXONERADAS, " +
                    "A.OPER_INAFECTAS, A.OPER_GRATUITAS, A.IMP_ISC, " +
                    "NVL(A.IGV, 18) AS TASA_IGV, A.TIPO_OPERACION, " +
                    "A.TIPO_FPAGO, A.COD_FPAGO, A.FECHA_VENCE, " +
                    "A.TIPO_REFE_FACTU, A.NO_REFE_FACTU, A.MOTIVO_NC, " +
                    "A.COD_TIENDA, A.CENTRO, A.BODEGA, " +
                    "A.COD_HASH, A.CDR, A.CDR_NOTA, A.CDR_OBSERVACION, " +
                    "A.ENVIAWS, A.NOMBRE_RQ, A.ESTADO, A.PROCE_STATUS " +
                    "FROM FACTU.ARFAFE A " +
                    "WHERE A.NO_CIA = ? " +
                    "AND A.ESTADO = 'D' " +
                    "AND A.PROCE_STATUS = 'N' " +
                    "AND A.ENVIAWS = 'S' " +
                    "AND ROWNUM = 1 "; */
            String sql = "SELECT A.NO_CIA, A.TIPO_DOC, A.NO_FACTU, A.ESTADO " +
                        "FROM FACTU.ARFAFE A " +
                        "WHERE A.NO_CIA = ? " +
                        //"AND A.ESTADO = 'D' " +
                        "AND A.PROCE_STATUS = 'N' " +
                        "AND A.ENVIAWS = 'S' " +
                        "AND ROWNUM = 1 ";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
               // String noCia = rs.getString("NO_CIA");
                String tipoDoc = rs.getString("TIPO_DOC");
                String noFactu = rs.getString("NO_FACTU");
                String estado  = rs.getString("ESTADO");

                cabeceraBean.setDocu_codigo(noFactu);
                cabeceraBean.setDocu_tipodocumento(ConversionUtils.convertirTipoDoc(tipoDoc));
                cabeceraBean.setDocu_numero(ConversionUtils.formatearNumeroDocumento(noFactu));
                cabeceraBean.setCdr(estado);

            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("Error pendienteDocElectronico: " + ex.getMessage());
            ex.printStackTrace();
        }
        return cabeceraBean;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BUSCAR DOCUMENTOS PARA REPROCESO (más de 10 min en proceso/error)
    // ══════════════════════════════════════════════════════════════════════════
    public static CabeceraBean noPendienteDocElectronico(Connection conn) {
        CabeceraBean b = null;
        try {
            String sql = "SELECT A.NO_CIA, A.TIPO_DOC, A.NO_FACTU, A.FECHA, " +
                    "TO_CHAR(A.FEC_CREA, 'HH24:MI:SS') AS HORA, " +
                    "A.NO_CLIENTE, A.NBR_CLIENTE, CXC.PR_CLIENTE.GET_DIRECCION(A.NO_CIA, A.NO_CLIENTE) AS DIRECCION, " +
                    "A.TIPO_DOC_CLI, A.NUM_DOC_CLI, A.RUC, " +
                    "A.MONEDA, A.VALOR_VENTA, A.SUB_TOTAL, A.IMPUESTO, A.TOTAL, " +
                    "A.T_DESCUENTO, A.OPER_GRAVADAS, A.OPER_EXONERADAS, " +
                    "A.OPER_INAFECTAS, A.OPER_GRATUITAS, A.IMP_ISC, " +
                    "NVL(A.IGV, 18) AS TASA_IGV, A.TIPO_OPERACION, " +
                    "A.TIPO_FPAGO, A.COD_FPAGO, A.FECHA_VENCE, " +
                    "A.TIPO_REFE_FACTU, A.NO_REFE_FACTU, A.MOTIVO_NC, " +
                    "A.COD_TIENDA, A.CENTRO, A.BODEGA, " +
                    "A.COD_HASH, A.CDR, A.CDR_NOTA, A.CDR_OBSERVACION, " +
                    "A.ENVIAWS, A.NOMBRE_RQ, A.ESTADO, A.PROCE_STATUS " +
                    "FROM FACTU.ARFAFE A " +
                    "WHERE A.NO_CIA = ? " +
                    "AND A.ESTADO = 'D' " +
                    "AND A.PROCE_STATUS IN ('B', 'P', 'E', 'X') " +
                    "AND A.PROCE_FECHA <= SYSDATE - INTERVAL '10' MINUTE " +
                    "AND A.TIPO_DOC IN ('F', 'B', 'C', 'D') " +
                    "AND ROWNUM = 1";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                b = mapearCabecera(rs, conn);
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("Error noPendienteDocElectronico: " + ex.getMessage());
        }
        return b;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR DOCUMENTO POR NÚMERO
    // ══════════════════════════════════════════════════════════════════════════
    public static CabeceraBean cargarDocElectronico(String pdocu_codigo, Connection conn) {
        CabeceraBean b = null;

        String noFactu = pdocu_codigo.replace("-", "").trim();

        try {
            String sql = "SELECT A.NO_CIA, A.TIPO_DOC, A.NO_FACTU, A.FECHA, " +
                    "TO_CHAR(A.FEC_CREA, 'HH24:MI:SS') AS HORA, " +
                    "A.NO_CLIENTE, A.NBR_CLIENTE, CXC.PR_CLIENTE.GET_DIRECCION(A.NO_CIA, A.NO_CLIENTE) AS DIR_CLIENTE, " +
                    "A.TIPO_DOC_CLI, A.NUM_DOC_CLI, A.RUC, " +
                    "A.MONEDA, ROUND(A.VALOR_VENTA, 2) AS VALOR_VENTA, ROUND(A.SUB_TOTAL, 2) AS SUB_TOTAL, ROUND(A.IMPUESTO, 2) AS IMPUESTO, ROUND(A.TOTAL, 2) AS TOTAL, " +
                    "ROUND(A.T_DESCUENTO, 2) AS T_DESCUENTO, ROUND(A.OPER_GRAVADAS, 2) AS OPER_GRAVADAS, ROUND(A.OPER_EXONERADAS, 2) AS OPER_EXONERADAS, " +
                    "ROUND(A.OPER_INAFECTAS, 2) AS OPER_INAFECTAS, ROUND(A.OPER_GRATUITAS, 2) AS OPER_GRATUITAS, A.IMP_ISC, " +
                    "NVL(A.IGV, 18) AS TASA_IGV, A.TIPO_OPERACION, " +
                    "A.TIPO_FPAGO, A.COD_FPAGO, A.FECHA_VENCE, " +
                    "A.TIPO_REFE_FACTU, A.NO_REFE_FACTU, A.MOTIVO_NC, " +
                    "A.COD_TIENDA, A.CENTRO, A.BODEGA, " +
                    "A.COD_HASH, A.CDR, A.CDR_NOTA, A.CDR_OBSERVACION, " +
                    "A.ENVIAWS, A.NOMBRE_RQ, A.ESTADO, A.PROCE_STATUS, " +
                    "A.TIP_DOC_ANULAR, A.NUM_ANULAR, A.MOT_ANULAR " +
                    "FROM FACTU.ARFAFE A " +
                    "WHERE A.NO_CIA = ? AND A.NO_FACTU = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noFactu);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                b = mapearCabecera(rs, conn);
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("Error cargarDocElectronico: " + ex.getMessage());
        }
        return b;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LISTA PARA RESUMEN DIARIO
    /* ══════════════════════════════════════════════════════════════════════════
    public static List<CabeceraBean> ResumenDiario(String pendiente, String tipodoc, Connection conn) {
        List<CabeceraBean> resumen = new ArrayList<CabeceraBean>();
        String tipoDocOracle = ConversionUtils.convertirTipoDocInverso(tipodoc);

        try {
            String sql = "SELECT A.NO_CIA, A.TIPO_DOC, A.NO_FACTU, A.FECHA, " +
                    "TO_CHAR(A.FEC_CREA, 'HH24:MI:SS') AS HORA, " +
                    "A.NO_CLIENTE, A.NBR_CLIENTE,CXC.PR_CLIENTE.GET_DIRECCION(A.NO_CIA, A.NO_CLIENTE) AS DIRECCION, " +
                    "A.TIPO_DOC_CLI, A.NUM_DOC_CLI, A.RUC, " +
                    "A.MONEDA, A.VALOR_VENTA, A.SUB_TOTAL, A.IMPUESTO, A.TOTAL, " +
                    "A.T_DESCUENTO, A.OPER_GRAVADAS, A.OPER_EXONERADAS, " +
                    "A.OPER_INAFECTAS, A.OPER_GRATUITAS, A.IMP_ISC, " +
                    "NVL(A.IGV, 18) AS TASA_IGV, A.TIPO_OPERACION, " +
                    "A.TIPO_FPAGO, A.COD_FPAGO, A.FECHA_VENCE, " +
                    "A.TIPO_REFE_FACTU, A.NO_REFE_FACTU, A.MOTIVO_NC, " +
                    "A.COD_TIENDA, A.CENTRO, A.BODEGA, " +
                    "A.COD_HASH, A.CDR, A.CDR_NOTA, A.CDR_OBSERVACION, " +
                    "A.ENVIAWS, A.NOMBRE_RQ, A.ESTADO, A.PROCE_STATUS " +
                    "FROM FACTU.ARFAFE A " +
                    "WHERE A.NO_CIA = ? AND A.PROCE_STATUS = ? AND A.TIPO_DOC = ? " +
                    "AND A.ESTADO = 'D' ORDER BY A.FECHA, A.NO_FACTU";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, pendiente);
            ps.setString(3, tipoDocOracle);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resumen.add(mapearCabecera(rs, conn));
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("Error ResumenDiario: " + ex.getMessage());
        }
        return resumen;
    }
    */
    // ══════════════════════════════════════════════════════════════════════════
    // MAPEAR RESULTSET A CABECERABEAN
    // ══════════════════════════════════════════════════════════════════════════
    private static CabeceraBean mapearCabecera(ResultSet rs, Connection conn) throws SQLException {
        CabeceraBean b = new CabeceraBean();

        String noCia = rs.getString("NO_CIA");
        String tipoDocOracle = rs.getString("TIPO_DOC");
        String noFactu = rs.getString("NO_FACTU");
        String codTienda = rs.getString("COD_TIENDA");
        String noCliente = rs.getString("NO_CLIENTE");
        codTienda = (codTienda != null && !codTienda.trim().isEmpty()) ? codTienda.trim() : "000";
        // Datos documento
        //b.setDocu_codigo(Math.abs(noFactu.hashCode()));
        b.setDocu_codigo(noFactu);
        b.setDocu_tipodocumento(ConversionUtils.convertirTipoDoc(tipoDocOracle));
        b.setDocu_numero(ConversionUtils.formatearNumeroDocumento(noFactu));

        Date fecha = rs.getDate("FECHA");
        if (fecha != null) {
            b.setDocu_fecha(new SimpleDateFormat("yyyy-MM-dd").format(fecha));
        }
        b.setDocu_hora(rs.getString("HORA") != null ? rs.getString("HORA") : "00:00:00");
        b.setDocu_moneda(ConversionUtils.convertirMoneda(rs.getString("MONEDA")));

        // Montos
        b.setDocu_gravada(rs.getDouble("OPER_GRAVADAS"));
        b.setDocu_exonerada(rs.getDouble("OPER_EXONERADAS"));
        b.setDocu_inafecta(rs.getDouble("OPER_INAFECTAS"));
        b.setDocu_gratuita(rs.getDouble("OPER_GRATUITAS"));
        b.setDocu_subtotal(rs.getDouble("VALOR_VENTA"));
        b.setDocu_igv(rs.getDouble("IMPUESTO"));
        b.setDocu_total(rs.getDouble("TOTAL"));
        b.setDocu_descuento(rs.getDouble("T_DESCUENTO"));
        b.setDocu_isc(rs.getDouble("IMP_ISC"));
        b.setTasa_igv(String.valueOf((int)rs.getDouble("TASA_IGV")));
        b.setTasa_isc("0");
        b.setDocu_otrostributos(0.0);
        b.setTasa_otrostributos("0");
        b.setDocu_otroscargos(0.0);
        b.setDocu_percepcion(0.0);

        // Cliente
        String tipoDocCli = rs.getString("TIPO_DOC_CLI");
        String numDocCli = rs.getString("NUM_DOC_CLI");
        String docCliente = (numDocCli != null && !numDocCli.trim().isEmpty()) ? numDocCli.trim() : noCliente;

        if (tipoDocCli == null || tipoDocCli.trim().isEmpty()) {
            tipoDocCli = ConversionUtils.determinarTipoDocPorLongitud(docCliente);
        } else {
            tipoDocCli = ConversionUtils.convertirTipoDocIdentidad(tipoDocCli);
        }

        b.setClie_tipodoc(tipoDocCli);
        b.setClie_numero(docCliente);
        b.setClie_nombre(limpiar(rs.getString("NBR_CLIENTE")));

        // Datos SUNAT
        b.setHashcode(rs.getString("COD_HASH"));
        b.setCdr(rs.getString("CDR"));
        b.setCdr_nota(rs.getString("CDR_NOTA"));
        b.setCdr_observacion(rs.getString("CDR_OBSERVACION"));
        b.setDocu_enviaws(rs.getString("ENVIAWS") != null ? rs.getString("ENVIAWS") : "S");

        // NC/ND
        String tipoRefe = rs.getString("TIPO_REFE_FACTU");
        String noRefe = rs.getString("NO_REFE_FACTU");
        if (tipoRefe != null && noRefe != null && !noRefe.trim().isEmpty()) {
            //b.setDocu_tipodocumento_anular(ConversionUtils.convertirTipoDoc(tipoRefe));
            b.setDocu_tipodcocumento_anular(ConversionUtils.convertirTipoDoc(tipoRefe));
            //b.setDocu_tipodocumento_numero(ConversionUtils.formatearNumeroDocumento(noRefe));
            b.setDocu_tipodcocumento_numero(ConversionUtils.formatearNumeroDocumento(noRefe));
        }
        b.setDocu_motivoanular(rs.getString("MOTIVO_NC"));

        // Cargar datos adicionales
        CabeceraBean cabeceraBean = cargarDatosEmpresa(b, NO_CIA_DEFAULT, codTienda, conn);
        CabeceraBean cabecera = cargarCorreoCliente(cabeceraBean, NO_CIA_DEFAULT, noCliente, conn);
        cabecera.setIdExterno(ConversionUtils.generarIdExterno(cabecera.getEmpr_nroruc(), tipoDocOracle, noFactu));

        return cabecera;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR DATOS EMPRESA
    // ══════════════════════════════════════════════════════════════════════════
    private static CabeceraBean cargarDatosEmpresa(CabeceraBean b, String noCia, String codTienda, Connection conn) {
        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT NOMBRE, NO_CLIENTE_ONLINE FROM FACTU.ARFAMC WHERE NO_CIA = ?");
            ps1.setString(1, noCia);
            ResultSet rs1 = ps1.executeQuery();
            String ruc = null;
            if (rs1.next()) {
                b.setEmpr_razonsocial(limpiar(rs1.getString("NOMBRE")));
                b.setEmpr_nombrecomercial(limpiar(rs1.getString("NOMBRE")));
                ruc = rs1.getString("NO_CLIENTE_ONLINE");
                b.setEmpr_nroruc(ruc);
            }
            rs1.close();
            ps1.close();

            if (ruc != null) {
                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT DIRECCION, CODI_DEPA||CODI_PROV||CODI_DIST AS UBIGEO, " +
                                "CODI_DEPA, CODI_PROV, CODI_DIST FROM SUCURSAL_PTOVTA " +
                                "WHERE NO_CIA = ?");
                ps2.setString(1, NO_CIA_DEFAULT);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    b.setEmpr_direccion(limpiar(rs2.getString("DIRECCION")));
                    b.setEmpr_ubigeo(rs2.getString("UBIGEO"));
                    String cDepa = rs2.getString("CODI_DEPA");
                    String cProv = rs2.getString("CODI_PROV");
                    String cDist = rs2.getString("CODI_DIST");
                    b.setEmpr_departamento(getUbigeo(conn, noCia, "D", cDepa, null, null));
                    b.setEmpr_provincia(getUbigeo(conn, noCia, "P", cDepa, cProv, null));
                    b.setEmpr_distrito(getUbigeo(conn, noCia, "I", cDepa, cProv, cDist));
                }
                rs2.close();
                ps2.close();
            }

            b.setEmpr_tipodoc("6");
            b.setEmpr_pais("PE");
            if (b.getEmpr_ubigeo() == null || b.getEmpr_ubigeo().isEmpty()) {
                b.setEmpr_ubigeo("150101");
            }


        } catch (Exception ex) {
            b.setEmpr_tipodoc("6");
            b.setEmpr_pais("PE");
            b.setEmpr_ubigeo("150101");
        }
        return b;
    }

    private static String getUbigeo(Connection conn, String noCia, String t, String d, String p, String i) {
        try {
            String sql = t.equals("D") ? "SELECT DESC_DEPA FROM CXC.ARCCDP WHERE NO_CIA=? AND CODI_DEPA=?" :
                    t.equals("P") ? "SELECT DESC_PROV FROM CXC.ARCCPR WHERE NO_CIA=? AND CODI_DEPA=? AND CODI_PROV=?" :
                            "SELECT DESC_DIST FROM CXC.ARCCDI WHERE NO_CIA=? AND CODI_DEPA=? AND CODI_PROV=? AND CODI_DIST=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, d);
            if (!t.equals("D")) ps.setString(3, p);
            if (t.equals("I")) ps.setString(4, i);
            ResultSet rs = ps.executeQuery();
            String r = rs.next() ? rs.getString(1) : "";
            rs.close();
            ps.close();
            return limpiar(r);
        } catch (Exception e) {
            return "";
        }
    }

    private static CabeceraBean cargarCorreoCliente(CabeceraBean b, String noCia, String noCliente, Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT EMAIL FROM CXC.ARCCMC WHERE NO_CIA=? AND NO_CLIENTE=?");
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noCliente);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("EMAIL") != null) {
                b.setClie_correo_cpe1(rs.getString("EMAIL").trim());
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println("Error cargarCorreoCliente: " + e.getMessage());
        }
        return b;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR DETALLE
    // ══════════════════════════════════════════════════════════════════════════
    public static List<DetalleBean> cargarDetDocElectronico(String pdocu_codigo, Connection conn) throws SQLException {
        List<DetalleBean> det = new ArrayList<DetalleBean>();
        String noFactu = pdocu_codigo.replace("-", "").trim();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT L.CONSECUTIVO, NVL(L.MEDIDA,'NIU') AS MEDIDA, L.CANTIDAD_FACT, L.NO_ARTI, " +
                            "INVE.PR_ARTICULO.GET_NOMBRE(L.NO_CIA, L.NO_ARTI, L.NO_FACTU) AS DESCRIPCION, ROUND(L.PRECIO_UNIT,7) AS PRECIO_UNIT, ROUND(L.TOTAL, 2) AS TOTAL, " +
                            "ROUND(NVL(L.IMP_IGV,0),2) AS IMP_IGV, " +
                            "ROUND(NVL(L.PREC_IGV,ROUND(L.PRECIO_UNIT*1.18,2)),2) AS PREC_IGV, " +
                            "NVL(L.TIPO_AFECTACION,'10') AS TIPO_AFECTACION " +
                            "FROM FACTU.ARFAFL L " +
                            "WHERE L.NO_CIA=? AND L.NO_FACTU=? ORDER BY L.CONSECUTIVO");

            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noFactu);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleBean d = new DetalleBean();
                d.setDocu_codigo(Math.abs(noFactu.hashCode()));
                d.setItem_orden(rs.getInt("CONSECUTIVO"));
                d.setItem_unidad(rs.getString("MEDIDA"));
                d.setItem_cantidad(rs.getInt("CANTIDAD_FACT"));
                d.setItem_codproducto(rs.getString("NO_ARTI"));
                d.setItem_descripcion(limpiar(rs.getString("DESCRIPCION")));
                d.setItem_pventa(rs.getDouble("PRECIO_UNIT"));
                d.setItem_pvtaigv(rs.getDouble("PREC_IGV"));
                d.setItem_to_subtotal(rs.getDouble("TOTAL"));
                d.setItem_to_igv(rs.getDouble("IMP_IGV"));

                String ta = rs.getString("TIPO_AFECTACION");
                d.setItem_afectacion(ConversionUtils.convertirTipoAfectacion(ta));

                if (ConversionUtils.esOperacionGratuita(ta)) {
                    d.setItem_pventa_nohonerosa(rs.getDouble("PRECIO_UNIT"));
                    d.setItem_pventa(0.0);
                } else {
                    d.setItem_pventa_nohonerosa(0.0);
                }

                det.add(d);
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
        return det;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR LEYENDAS
    // ══════════════════════════════════════════════════════════════════════════
    public static List<LeyendaBean> cargarDetDocElectronicoLeyenda(String pdocu_codigo, Connection conn) throws SQLException {
        List<LeyendaBean> ley = new ArrayList<LeyendaBean>();
        String noFactu = pdocu_codigo.replace("-", "").trim();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOTAL, MONEDA FROM FACTU.ARFAFE WHERE NO_CIA=? AND NO_FACTU=?");
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noFactu);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LeyendaBean l = new LeyendaBean();
                l.setLeyenda_codigo("1000");
                l.setLeyenda_texto(numLetras(rs.getDouble("TOTAL"), rs.getString("MONEDA")));
                ley.add(l);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            // Ignorar
        }
        return ley;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR PAGOS/CUOTAS
    // ══════════════════════════════════════════════════════════════════════════
    public static List<PagoBean> cargarDetDocElectronicoPagos(String pdocu_codigo, Connection conn) throws SQLException {
        List<PagoBean> pag = new ArrayList<PagoBean>();
        String noFactu = pdocu_codigo.replace("-", "").trim();

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT TIPO_FPAGO, TOTAL, NO_CLIENTE, MONEDA FROM FACTU.ARFAFE WHERE NO_CIA=? AND NO_FACTU=?");
            ps1.setString(1, NO_CIA_DEFAULT);
            ps1.setString(2, noFactu);
            ResultSet rs1 = ps1.executeQuery();

            String tf = "CON";
            double tot = 0;
            String nc = "";
            String mon = "PEN";

            if (rs1.next()) {
                tf = rs1.getString("TIPO_FPAGO");
                tot = rs1.getDouble("TOTAL");
                nc = rs1.getString("NO_CLIENTE");
                mon = ConversionUtils.convertirMoneda(rs1.getString("MONEDA"));
            }
            rs1.close();
            ps1.close();

            if ("CRE".equalsIgnoreCase(tf)) {
                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT NO_CREDITO, MONTO, FEC_PAGO FROM FACTU.ARFCRED " +
                                "WHERE NO_CIA=? AND NO_CLIENTE=? AND NO_ORDEN=? ORDER BY NO_CREDITO");
                ps2.setString(1, NO_CIA_DEFAULT);
                ps2.setString(2, nc);
                ps2.setString(3, noFactu);
                ResultSet rs2 = ps2.executeQuery();

                int id = 1;
                while (rs2.next()) {
                    PagoBean p = new PagoBean();
                    p.setIdpago(id++);
                    p.setDocu_codigo(Math.abs(noFactu.hashCode()));
                    p.setNrocuota(rs2.getInt("NO_CREDITO"));
                    p.setMonto(rs2.getDouble("MONTO"));
                    p.setFecha(rs2.getDate("FEC_PAGO"));
                    pag.add(p);
                }
                rs2.close();
                ps2.close();
            }
            else {
                PagoBean p = new PagoBean();
                p.setIdpago(1);
                p.setDocu_codigo(Math.abs(noFactu.hashCode()));
                p.setNrocuota(0);
                p.setMonto(tot);
                p.setFecha(null);
                pag.add(p);
            }
        } catch (Exception e) {
            // Ignorar
        }
        return pag;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE ACTUALIZACIÓN DE ESTADO
    // ══════════════════════════════════════════════════════════════════════════

    public static void bloquearDocumento(String noFactu, Connection conn) {
        ejecutarUpdate(
                "UPDATE FACTU.ARFAFE SET PROCE_STATUS='B', PROCE_FECHA=SYSDATE WHERE NO_CIA=? AND NO_FACTU=?",
                noFactu, conn);
    }

    public static void marcarEnProceso(String noFactu, Connection conn) {
        ejecutarUpdate(
                "UPDATE FACTU.ARFAFE SET PROCE_STATUS='P', PROCE_FECHA=SYSDATE WHERE NO_CIA=? AND NO_FACTU=?",
                noFactu, conn);
    }

    public static void marcarEnviado(String noFactu, String hash, String cdr, Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE FACTU.ARFAFE SET PROCE_STATUS = 'E', PROCE_FECHA = SYSDATE, " +
                            "COD_HASH = ?, CDR = ?, FEC_ENVIO = SYSDATE " +
                            "WHERE NO_CIA = ? AND NO_FACTU = ?");
            ps.setString(1, hash);
            ps.setString(2, cdr);
            ps.setString(3, NO_CIA_DEFAULT);
            ps.setString(4, noFactu.replace("-", ""));
            ps.executeUpdate();
            conn.commit();
            ps.close();
            System.out.println("Documento enviado: " + noFactu);
        } catch (Exception e) {
            System.out.println("Error marcarEnviado: " + e.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    public static void marcarError(String noFactu, String cdr, String nota, String obs, Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE FACTU.ARFAFE SET PROCE_STATUS='X', PROCE_FECHA=SYSDATE, " +
                            "CDR=?, CDR_NOTA=?, CDR_OBSERVACION=?, FEC_MODI=SYSDATE, USU_MODI=USER " +
                            "WHERE NO_CIA=? AND NO_FACTU=?");
            ps.setString(1, cdr);
            ps.setString(2, nota);
            ps.setString(3, obs);
            ps.setString(4, NO_CIA_DEFAULT);
            ps.setString(5, noFactu.replace("-", ""));
            ps.executeUpdate();
            conn.commit();
            ps.close();
            System.out.println("Documento con error: " + noFactu + " - " + nota);
        } catch (Exception e) {
            System.out.println("Error marcarError: " + e.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    public static void marcarPendiente(String noFactu, Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE FACTU.ARFAFE SET PROCE_STATUS='N', PROCE_FECHA=NULL, " +
                            "COD_HASH=NULL, CDR=NULL, CDR_NOTA=NULL, CDR_OBSERVACION=NULL " +
                            "WHERE NO_CIA=? AND NO_FACTU=?");
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noFactu.replace("-", ""));
            ps.executeUpdate();
            conn.commit();
            ps.close();
            System.out.println("Documento pendiente: " + noFactu);
        } catch (Exception e) {
            System.out.println("Error marcarPendiente: " + e.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    public static int contarPendientes(Connection conn) {
        int c = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM FACTU.ARFAFE WHERE NO_CIA=? AND ESTADO='D' " +
                            "AND (PROCE_STATUS='N' OR PROCE_STATUS IS NULL) AND TIPO_DOC IN('F','B','C','D')");
            ps.setString(1, NO_CIA_DEFAULT);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) c = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println("Error contarPendientes: " + e.getMessage());
        }
        return c;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UTILITARIOS
    // ══════════════════════════════════════════════════════════════════════════

    private static void ejecutarUpdate(String sql, String noFactu, Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noFactu.replace("-", ""));
            ps.executeUpdate();
            conn.commit();
            ps.close();
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    private static String limpiar(String t) {
        return t == null ? "" : t.replaceAll("[|%•$¿?^Çªº°~€¬ç¡\"]", "").replaceAll("\\s+", " ").trim();
    }

    private static String numLetras(double n, String m) {
        int e = (int) n;
        int d = (int) Math.round((n - e) * 100);
        String mt = "SOL".equalsIgnoreCase(m) || "PEN".equalsIgnoreCase(m) ? "SOLES" : "DOLARES AMERICANOS";
        return nTexto(e).toUpperCase() + " Y " + String.format("%02d", d) + "/100 " + mt;
    }

    private static String nTexto(int n) {
        if (n == 0) return "cero";
        if (n == 100) return "cien";
        String[] u = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"};
        String[] e = {"diez", "once", "doce", "trece", "catorce", "quince"};
        String[] d = {"", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
        String[] c = {"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
        StringBuilder sb = new StringBuilder();
        if (n >= 1000) {
            int m = n / 1000;
            sb.append(m == 1 ? "mil " : nTexto(m) + " mil ");
            n %= 1000;
        }
        if (n >= 100) {
            sb.append(c[n / 100]).append(" ");
            n %= 100;
        }
        if (n >= 10 && n <= 15) sb.append(e[n - 10]);
        else if (n >= 16 && n <= 19) sb.append("dieci").append(u[n - 10]);
        else if (n >= 20 && n <= 29 && n != 20) sb.append("veinti").append(u[n - 20]);
        else if (n >= 20) {
            sb.append(d[n / 10]);
            if (n % 10 > 0) sb.append(" y ").append(u[n % 10]);
        } else if (n > 0) sb.append(u[n]);
        return sb.toString().trim();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR DOCUMENTOS PENDIENTES DE BAJA
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los documentos pendientes de comunicación de baja (estado = 'N')
     * @param conn Conexión a la base de datos
     * @return Lista de documentos a dar de baja
     */
    public static ComunicacionBajaBean cargarDocumentosBajaPendientes(String noFactu, Connection conn) {
        ComunicacionBajaBean baja = new ComunicacionBajaBean();
        try {
            String sql = "SELECT CB.NO_CIA, CB.NO_FACTU, CB.FEC_EMISION, CB.FEC_BAJA, " +
                    "CB.COD_MOTIVO, CB.DESC_MOTIVO, CB.ESTADO, CB.NRO_CORRELATIVO " +
                    "FROM FACTU.COMUNICACION_BAJA CB " +
                    "WHERE CB.NO_CIA = ? " +
                    "AND CB.NO_FACTU = ? " +
                    "AND CB.ESTADO = 'N' ";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, noFactu);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                baja = mapearComunicacionBaja(rs);

            }
            rs.close();
            ps.close();

        } catch (Exception ex) {
            System.out.println("Error cargarDocumentosBajaPendientes: " + ex.getMessage());
            ex.printStackTrace();
        }
        return baja;
    }

    /**
     * Mapea un ResultSet a ComunicacionBajaBean
     */
    private static ComunicacionBajaBean mapearComunicacionBaja(ResultSet rs) throws SQLException {
        ComunicacionBajaBean bean = new ComunicacionBajaBean();

        bean.setNoCia(rs.getString("NO_CIA"));
        bean.setNoFactu(rs.getString("NO_FACTU"));
        bean.setFecEmision(rs.getDate("FEC_EMISION"));
        bean.setFecBaja(rs.getDate("FEC_BAJA"));
        bean.setCodMotivo(rs.getString("COD_MOTIVO"));
        bean.setDescMotivo(rs.getString("DESC_MOTIVO"));
        bean.setEstado(rs.getString("ESTADO"));
        bean.setNroCorrelativo(rs.getString("NRO_CORRELATIVO"));
        //bean.setTicketSunat(rs.getString("TICKET_SUNAT"));
        //bean.setCdrSunat(rs.getString("CDR_SUNAT"));

        // Convertir tipo de documento Oracle a código SUNAT
        String noFactu = bean.getNoFactu();
        String tipoDocOracle = noFactu.toUpperCase().substring(0, 1);
        tipoDocOracle = "N".equals(tipoDocOracle) ? "NC" : tipoDocOracle; // Ajuste para NC/ND
        bean.setTipoDocumento(ConversionUtils.convertirTipoDoc(tipoDocOracle));

        // Parsear serie y número del NO_FACTU

        if (noFactu.length() >= 4) {
            bean.setSerie(noFactu.substring(0, 4));
            bean.setNumero(noFactu.substring(4));
        }

        return bean;
    }

    /**
     * Obtiene los datos de la empresa para la comunicación de baja
     * @param conn Conexión a la base de datos
     * @return ComunicacionBajaBean con datos de la empresa
     */
    public static ComunicacionBajaBean cargarDatosEmpresaBaja(Connection conn) {
        ComunicacionBajaBean bean = new ComunicacionBajaBean();
        try {
            String sql = "SELECT NOMBRE, NO_CLIENTE_ONLINE FROM FACTU.ARFAMC WHERE NO_CIA = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                bean.setEmprRazonSocial(rs.getString("NOMBRE"));
                bean.setEmprRuc(rs.getString("NO_CLIENTE_ONLINE"));
                bean.setEmprTipoDoc("6"); // RUC
            }
            rs.close();
            ps.close();

        } catch (Exception ex) {
            System.out.println("Error cargarDatosEmpresaBaja: " + ex.getMessage());
        }
        return bean;
    }

    /**
     * Marca los documentos como en proceso (estado = 'P')
     */
    public static void marcarBajaEnProceso(ComunicacionBajaBean documentos, String correlativo, Connection conn) {
        try {
            String sql = "UPDATE FACTU.COMUNICACION_BAJA SET ESTADO = 'P' " +
                    "WHERE NO_CIA = ? AND NO_FACTU = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            //for (ComunicacionBajaBean doc : documentos) {
                ps.setString(1, documentos.getNoCia());
                ps.setString(2, documentos.getNoFactu());
                ps.addBatch();
           // }
            ps.executeBatch();
            conn.commit();
            ps.close();

        } catch (Exception ex) {
            System.out.println("Error marcarBajaEnProceso: " + ex.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    /**
     * Marca los documentos con error (estado = 'X')
     */
    public static void marcarBajaError(ComunicacionBajaBean documentos, String cdr, Connection conn) {
        try {
            String sql = "UPDATE FACTU.COMUNICACION_BAJA SET ESTADO = 'X', CDR_SUNAT = ? " +
                    "WHERE NO_CIA = ? AND NO_FACTU = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

           // for (ComunicacionBajaBean doc : documentos) {
                ps.setString(1, cdr);
                ps.setString(2, documentos.getNoCia());
                ps.setString(3, documentos.getNoFactu());
                ps.addBatch();
           // }
            ps.executeBatch();
            conn.commit();
            ps.close();

        } catch (Exception ex) {
            System.out.println("Error marcarBajaError: " + ex.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    /**
     * Marca los documentos como enviados exitosamente (estado = 'E')
     */
    public static void marcarBajaEnviada(ComunicacionBajaBean documentos, String ticket, String cdr, Connection conn) {
        try {
            String sql = "UPDATE FACTU.COMUNICACION_BAJA SET ESTADO = 'E', TICKET_SUNAT = ?, CDR_SUNAT = ? " +
                    "WHERE NO_CIA = ? AND NO_FACTU = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            //for (ComunicacionBajaBean doc : documentos) {
                ps.setString(1, ticket);
                ps.setString(2, cdr);
                ps.setString(3, documentos.getNoCia());
                ps.setString(4, documentos.getNoFactu());
                ps.addBatch();
            //}
            ps.executeBatch();
            conn.commit();
            ps.close();

        } catch (Exception ex) {
            System.out.println("Error marcarBajaEnviada: " + ex.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
// MÉTODO REFACTORIZADO ResumenDiario - AGREGAR/REEMPLAZAR EN DElectronicoDespachador.java
// ══════════════════════════════════════════════════════════════════════════════════════
//
// CAMBIOS REALIZADOS:
// 1. Incluye boletas con estado 'D' (Despachado) y 'A' (Anulado)
// 2. Límite de 100 boletas por resumen diario (requisito SUNAT)
// 3. Agrega campo ESTADO al bean para determinar el ConditionCode
// 4. Ordena por fecha y número de documento
// ══════════════════════════════════════════════════════════════════════════════════════

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * LISTA PARA RESUMEN DIARIO DE BOLETAS
     * ══════════════════════════════════════════════════════════════════════════
     *
     * Obtiene las boletas pendientes de envío en resumen diario.
     *
     * ESTADOS DE BOLETA (FACTU.ARFAFE.ESTADO):
     * - 'D' = Despachado (boleta emitida normalmente) → ConditionCode = 1 (Adicionar)
     * - 'A' = Anulado (boleta anulada) → ConditionCode = 3 (Anular)
     *
     * ESTADOS DE PROCESO (FACTU.ARFAFE.PROCE_STATUS):
     * - 'N' = Nuevo/Pendiente de envío
     * - 'B' = Bloqueado para proceso
     * - 'P' = En proceso
     * - 'E' = Enviado exitosamente
     * - 'X' = Error de envío
     *
     * @param pendiente Estado de proceso ('N' para pendientes)
     * @param tipodoc Tipo de documento SUNAT ('03' para boletas)
     * @param conn Conexión a la base de datos
     * @return Lista de boletas para el resumen (máximo 100)
     */
    public static List<CabeceraBean> ResumenDiario(Date fecha, String pendiente, String tipodoc, Connection conn) {
        List<CabeceraBean> resumen = new ArrayList<CabeceraBean>();

        // Convertir tipo documento SUNAT a código Oracle
        // '03' (Boleta SUNAT) → 'B' (Boleta Oracle)
        String tipoDocOracle = ConversionUtils.convertirTipoDocInverso(tipodoc);

        // Límite máximo de documentos por resumen diario según SUNAT
        final int LIMITE_BOLETAS = 100;

        try {
            // ══════════════════════════════════════════════════════════════════
            // SQL: Obtener boletas despachadas (D) y anuladas (A)
            // ══════════════════════════════════════════════════════════════════
            String sql = "SELECT A.NO_CIA, A.TIPO_DOC, A.NO_FACTU, A.FECHA, " +
                    "TO_CHAR(A.FEC_CREA, 'HH24:MI:SS') AS HORA, " +
                    "A.NO_CLIENTE, A.NBR_CLIENTE, " +
                    "CXC.PR_CLIENTE.GET_DIRECCION(A.NO_CIA, A.NO_CLIENTE) AS DIRECCION, " +
                    "A.TIPO_DOC_CLI, A.NUM_DOC_CLI, A.RUC, " +
                    "A.MONEDA, " +
                    "ROUND(A.VALOR_VENTA, 2) AS VALOR_VENTA, " +
                    "ROUND(A.SUB_TOTAL, 2) AS SUB_TOTAL, " +
                    "ROUND(A.IMPUESTO, 2) AS IMPUESTO, " +
                    "ROUND(A.TOTAL, 2) AS TOTAL, " +
                    "ROUND(A.T_DESCUENTO, 2) AS T_DESCUENTO, " +
                    "ROUND(A.OPER_GRAVADAS, 2) AS OPER_GRAVADAS, " +
                    "ROUND(A.OPER_EXONERADAS, 2) AS OPER_EXONERADAS, " +
                    "ROUND(A.OPER_INAFECTAS, 2) AS OPER_INAFECTAS, " +
                    "ROUND(A.OPER_GRATUITAS, 2) AS OPER_GRATUITAS, " +
                    //"A.IMP_ISC, " +
                    "NVL(A.IGV, 18) AS TASA_IGV, A.TIPO_OPERACION, " +
                    //"A.TIPO_FPAGO, A.COD_FPAGO, A.FECHA_VENCE, " +
                    //"A.TIPO_REFE_FACTU, A.NO_REFE_FACTU, A.MOTIVO_NC, " +
                    //"A.COD_TIENDA, A.CENTRO, A.BODEGA, " +
                    "A.COD_HASH, A.CDR, A.CDR_NOTA, A.CDR_OBSERVACION, " +
                    "A.ENVIAWS, A.NOMBRE_RQ, " +
                    "A.ESTADO, " +           // D=Despachado, A=Anulado
                    "A.PROCE_STATUS " +
                    "FROM FACTU.ARFAFE A " +
                    "WHERE A.NO_CIA = ? " +
                    "AND A.ENVIAWS = ? " +                   // S=ENVIAR A SUNAT
                   // "AND A.PROCE_STATUS = ? " +              // E=Enviado
                    "AND A.PROCE_STATUS IN ('E', 'N') " +       // E=Enviado ; N=Nuevo
                    "AND A.TIPO_DOC = ? " +                   // B=Boleta
                    "AND A.FECHA = ? " +
                    "AND A.ESTADO IN ('D', 'A') " +           // D=Despachado, A=Anulado
                    "AND ROWNUM <= ? " +                      // Límite de 100 boletas
                    "ORDER BY A.FECHA, A.NO_FACTU";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setString(2, "S");        // 'S' para enviar a SUNAT
            //ps.setString(3, pendiente);        // 'E' de Enviado
            ps.setString(3, tipoDocOracle);    // 'B' para boletas
            ps.setDate (4, fecha);
            ps.setInt(5, LIMITE_BOLETAS);      // Máximo 100 boletas

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CabeceraBean bean = mapearCabeceraResumen(rs, conn);
                resumen.add(bean);
            }

            rs.close();
            ps.close();

            System.out.println("ResumenDiario: " + resumen.size() + " boletas encontradas (máx " + LIMITE_BOLETAS + ")");

        } catch (Exception ex) {
            System.out.println("Error ResumenDiario: " + ex.getMessage());
            ex.printStackTrace();
        }

        return resumen;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * MAPEAR RESULTSET A CABECERABEAN PARA RESUMEN DIARIO
     * ══════════════════════════════════════════════════════════════════════════
     *
     * Mapea los datos del ResultSet incluyendo el campo ESTADO para determinar
     * si la boleta es para alta (D) o baja (A) en el resumen.
     */
    private static CabeceraBean mapearCabeceraResumen(ResultSet rs, Connection conn) throws SQLException {
        CabeceraBean b = new CabeceraBean();

        String noCia = rs.getString("NO_CIA");
        String tipoDocOracle = rs.getString("TIPO_DOC");
        String noFactu = rs.getString("NO_FACTU");
        //String codTienda = rs.getString("COD_TIENDA");
        String codTienda = "000";
        String noCliente = rs.getString("NO_CLIENTE");


        // ══════════════════════════════════════════════════════════════════
        // ESTADO DE LA BOLETA (importante para ConditionCode)
        // ══════════════════════════════════════════════════════════════════
        String estadoBoleta = rs.getString("ESTADO"); // D=Despachado, A=Anulado
        b.setEstadoBoleta(estadoBoleta);

        // Datos documento
        b.setDocu_codigo(noFactu);
        b.setDocu_tipodocumento(ConversionUtils.convertirTipoDoc(tipoDocOracle)); // "03" para boleta
        b.setDocu_numero(ConversionUtils.formatearNumeroDocumento(noFactu));

        Date fecha = rs.getDate("FECHA");
        if (fecha != null) {
            b.setDocu_fecha(new SimpleDateFormat("yyyy-MM-dd").format(fecha));
        }
        b.setDocu_hora(rs.getString("HORA") != null ? rs.getString("HORA") : "00:00:00");
        b.setDocu_moneda(ConversionUtils.convertirMoneda(rs.getString("MONEDA")));

        // Montos (ya redondeados en el SQL)
        b.setDocu_gravada(rs.getDouble("OPER_GRAVADAS"));
        b.setDocu_exonerada(rs.getDouble("OPER_EXONERADAS"));
        b.setDocu_inafecta(rs.getDouble("OPER_INAFECTAS"));
        b.setDocu_gratuita(rs.getDouble("OPER_GRATUITAS"));
        b.setDocu_subtotal(rs.getDouble("VALOR_VENTA"));
        b.setDocu_igv(rs.getDouble("IMPUESTO"));
        b.setDocu_total(rs.getDouble("TOTAL"));
        b.setDocu_descuento(rs.getDouble("T_DESCUENTO"));
        //b.setDocu_isc(rs.getDouble("IMP_ISC"));
        b.setDocu_isc(0.0);
        b.setTasa_igv(String.valueOf((int)rs.getDouble("TASA_IGV")));
        b.setTasa_isc("0");
        b.setDocu_otrostributos(0.0);
        b.setTasa_otrostributos("0");
        b.setDocu_otroscargos(0.0);
        b.setDocu_percepcion(0.0);

        // Cliente
        String tipoDocCli = rs.getString("TIPO_DOC_CLI");
        String numDocCli = rs.getString("NUM_DOC_CLI");
        String docCliente = (numDocCli != null && !numDocCli.trim().isEmpty()) ? numDocCli.trim() : noCliente;

        if (tipoDocCli == null || tipoDocCli.trim().isEmpty()) {
            tipoDocCli = ConversionUtils.determinarTipoDocPorLongitud(docCliente);
        } else {
            tipoDocCli = ConversionUtils.convertirTipoDocIdentidad(tipoDocCli);
        }

        b.setClie_tipodoc(tipoDocCli);
        b.setClie_numero(docCliente);
        b.setClie_nombre(limpiar(rs.getString("NBR_CLIENTE")));

        // Datos SUNAT
        b.setHashcode(rs.getString("COD_HASH"));
        b.setCdr(rs.getString("CDR"));
        b.setCdr_nota(rs.getString("CDR_NOTA"));
        b.setCdr_observacion(rs.getString("CDR_OBSERVACION"));
        b.setDocu_enviaws(rs.getString("ENVIAWS") != null ? rs.getString("ENVIAWS") : "S");

        // Cargar datos adicionales de empresa
        CabeceraBean cabeceraBean = cargarDatosEmpresa(b, NO_CIA_DEFAULT, codTienda, conn);

        return cabeceraBean;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * MARCAR BOLETAS DEL RESUMEN COMO ENVIADAS
     * ══════════════════════════════════════════════════════════════════════════
     */
    public static void marcarResumenEnviado(List<CabeceraBean> boletas, Date fechaEmision, String ticket, String cdr, Connection conn) {
        CallableStatement cstmt = null;
        try {
            String sql = "{ call FACTU.PR_FACTURA.REG_RESUM_DIARIO(?, ?, ?, ?, ?) }";
            cstmt = conn.prepareCall(sql);
            cstmt.setString(1, NO_CIA_DEFAULT);
            cstmt.setDate (2, fechaEmision);
            cstmt.setString(3, "E"); // Estado 'E' para Enviado
            //cstmt.setString(4, null);
            cstmt.setString(4, ticket);
            cstmt.setString(5, cdr);

            // Ejecutar el procedimiento
            cstmt.execute();

            //conn.commit();
            cstmt.close();

            String sql2 = "{ call FACTU.PR_FACTURA.ACTU_ESTADO_ENVIO(?, ?, ?, ?) }";

            for (CabeceraBean boleta : boletas) {
                  cstmt = conn.prepareCall(sql2);
                  cstmt.setString(1, NO_CIA_DEFAULT);
                  cstmt.setString(2, null);
                  cstmt.setString(3, boleta.getDocu_codigo().replace("-", ""));
                  cstmt.setString(4, "R");
                  // Ejecutar el procedimiento
                  cstmt.execute();
            }

            //conn.commit();
            cstmt.close();

        } catch (Exception e) {
            System.out.println("Error marcarResumenEnviado: " + e.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * BLOQUEAR BOLETAS PARA RESUMEN DIARIO
     * ══════════════════════════════════════════════════════════════════════════
     */
    public static void bloquearBoletasResumen(List<CabeceraBean> boletas, Connection conn) {
        try {
            String sql = "UPDATE FACTU.ARFAFE SET PROCE_STATUS = 'B', PROCE_FECHA = SYSDATE " +
                    "WHERE NO_CIA = ? AND NO_FACTU = ?";

            PreparedStatement ps = conn.prepareStatement(sql);

            for (CabeceraBean boleta : boletas) {
                ps.setString(1, NO_CIA_DEFAULT);
                ps.setString(2, boleta.getDocu_codigo().replace("-", ""));
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            ps.close();

        } catch (Exception e) {
            System.out.println("Error bloquearBoletasResumen: " + e.getMessage());
            try { conn.rollback(); } catch (Exception x) {}
        }
    }

    /*
    * FUNCION QUE CARGA EL CORRELATIVO DE BOLETAS PARA RESUMEN DIARIO
     */
    public static String cargarCorrelativoResumenDiario( Date fecEmision, Connection conn) {
        String correlativo = "0";
        try {
            String sql = "SELECT FACTU.PR_FACTURA.GET_CORRE_RESDIA(?, ?) AS CORRELATIVO FROM DUAL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, NO_CIA_DEFAULT);
            ps.setDate (2, fecEmision);        // fecha emision para obtener correlativo específico del día emisión
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                correlativo = rs.getString("CORRELATIVO");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println("Error cargarCorrelativoResumenDiario: " + e.getMessage());
        }
        return correlativo;
    }


    // ══════════════════════════════════════════════════════════════════════════
    // CARGAR DATOS EMPRESA
    // ══════════════════════════════════════════════════════════════════════════
    public static CabeceraBean cargarDatoEmpresa(Connection conn) {
        CabeceraBean datosEmpresa = new CabeceraBean();
        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT NOMBRE, NO_CLIENTE_ONLINE FROM FACTU.ARFAMC WHERE NO_CIA = ?");
            ps1.setString(1, NO_CIA_DEFAULT);
            ResultSet rs1 = ps1.executeQuery();
            String ruc = null;
            if (rs1.next()) {
                datosEmpresa.setEmpr_razonsocial(limpiar(rs1.getString("NOMBRE")));
                datosEmpresa.setEmpr_nombrecomercial(limpiar(rs1.getString("NOMBRE")));
                ruc = rs1.getString("NO_CLIENTE_ONLINE");
                datosEmpresa.setEmpr_nroruc(ruc);
            }
            rs1.close();
            ps1.close();

             // Cargar tipo de documento, país y ubigeo con valores por defecto si no se encuentran
             datosEmpresa.setEmpr_tipodoc("6"); // RUC
             datosEmpresa.setEmpr_pais("PE");
             datosEmpresa.setEmpr_ubigeo("150101");

        } catch (Exception ex) {
            datosEmpresa.setEmpr_tipodoc("6");
            datosEmpresa.setEmpr_pais("PE");
            datosEmpresa.setEmpr_ubigeo("150101");
        }
        return datosEmpresa;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LISTA DE FECHAS DE BOLETAS PARA RESUMEN DIARIO
    // ══════════════════════════════════════════════════════════════════════════
    public static List<Date> listaFechaResumenDiario(Connection conn) {

        List<Date> fechas = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT TRUNC(FECHA) AS FECHA " +
                    "FROM FACTU.ARFAFE "+
                    "WHERE NO_CIA = ? "+
                    "AND ENVIAWS = ? "+
                    //"AND PROCE_STATUS = ? "+
                    "AND PROCE_STATUS IN ('E', 'N') "+
                    "AND TIPO_DOC = ? "+
                    "AND ESTADO IN ('D', 'A') "+
                    "ORDER BY TRUNC(FECHA) ASC";
            PreparedStatement ps1 = conn.prepareStatement(sql);
            ps1.setString(1, NO_CIA_DEFAULT);
            ps1.setString(2, "S");        // 'S' para enviar a SUNAT
           // ps1.setString(3, "N");
            ps1.setString(3, "B");        // 'B' para boletas
            ResultSet rs1 = ps1.executeQuery();
            String ruc = null;
            if (rs1.next()) {
                Date fecha = rs1.getDate("FECHA");
                fechas.add(fecha);
            }
            rs1.close();
            ps1.close();

        } catch (Exception ex) {
            fechas.add(null);
        }
        return fechas;
    }

}