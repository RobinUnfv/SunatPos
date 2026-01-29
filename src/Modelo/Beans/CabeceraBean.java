/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo.Beans;

/**
 *
 * @author LUISINHO
 */
public class CabeceraBean {
 private int docu_codigo;
 private String idExterno;
 private String empr_razonsocial;
 private String empr_ubigeo;
 private String empr_nombrecomercial;
 private String empr_direccion;
 private String empr_provincia;
 private String empr_departamento;
 private String empr_distrito;
 private String empr_pais;
 private String empr_nroruc;
 private String empr_tipodoc;
 private String clie_numero;
 private String clie_tipodoc;
 private String clie_nombre;
 private String docu_fecha; 
 private String docu_hora;
 private String docu_tipodocumento;
 private String docu_numero; // serie + "-" + correlativo   FF01-00001234
 private String docu_moneda;
 private double docu_gravada;
 private double docu_inafecta;
 private double docu_exonerada;
 private double docu_gratuita;
 private double docu_descuento;
 private double docu_subtotal;
 private double docu_total;
 private double docu_igv;
 private String tasa_igv;
 private double docu_isc;
 private String tasa_isc;
 private double docu_otrostributos;
 private String tasa_otrostributos;
 private double docu_otroscargos;
 private double docu_percepcion;
 private String hashcode;
 private String cdr;
 private String cdr_nota;
 private String cdr_observacion;
 private String docu_enviaws;   // N - no envia a sunat  // S - si envía a sunat
 private String docu_proce_status;
 private String docu_proce_fecha;
 private String docu_link_pdf;
 private String docu_link_cdr;
 private String docu_link_xml;
 private String clie_correo_cpe1;
 private String clie_correo_cpe2;
 private String docu_tipodcocumento_anular;
 private String docu_tipodcocumento_numero;
 private String docu_motivoanular;

    public String getDocu_tipodcocumento_anular() {
        return docu_tipodcocumento_anular;
    }

    public void setDocu_tipodcocumento_anular(String docu_tipodcocumento_anular) {
        this.docu_tipodcocumento_anular = docu_tipodcocumento_anular;
    }

    public String getDocu_tipodcocumento_numero() {
        return docu_tipodcocumento_numero;
    }

    public void setDocu_tipodcocumento_numero(String docu_tipodcocumento_numero) {
        this.docu_tipodcocumento_numero = docu_tipodcocumento_numero;
    }

    public String getDocu_motivoanular() {
        return docu_motivoanular;
    }

    public void setDocu_motivoanular(String docu_motivoanular) {
        this.docu_motivoanular = docu_motivoanular;
    }

    public int getDocu_codigo() {
        return docu_codigo;
    }

    public void setDocu_codigo(int docu_codigo) {
        this.docu_codigo = docu_codigo;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public String getEmpr_razonsocial() {
        return empr_razonsocial;
    }

    public void setEmpr_razonsocial(String empr_razonsocial) {
        this.empr_razonsocial = empr_razonsocial;
    }

    public String getEmpr_ubigeo() {
        return empr_ubigeo;
    }

    public void setEmpr_ubigeo(String empr_ubigeo) {
        this.empr_ubigeo = empr_ubigeo;
    }

    public String getEmpr_nombrecomercial() {
        return empr_nombrecomercial;
    }

    public void setEmpr_nombrecomercial(String empr_nombrecomercial) {
        this.empr_nombrecomercial = empr_nombrecomercial;
    }

    public String getEmpr_direccion() {
        return empr_direccion;
    }

    public void setEmpr_direccion(String empr_direccion) {
        this.empr_direccion = empr_direccion;
    }

    public String getEmpr_provincia() {
        return empr_provincia;
    }

    public void setEmpr_provincia(String empr_provincia) {
        this.empr_provincia = empr_provincia;
    }

    public String getEmpr_departamento() {
        return empr_departamento;
    }

    public void setEmpr_departamento(String empr_departamento) {
        this.empr_departamento = empr_departamento;
    }

    public String getEmpr_distrito() {
        return empr_distrito;
    }

    public void setEmpr_distrito(String empr_distrito) {
        this.empr_distrito = empr_distrito;
    }

    public String getEmpr_pais() {
        return empr_pais;
    }

    public void setEmpr_pais(String empr_pais) {
        this.empr_pais = empr_pais;
    }

    public String getEmpr_nroruc() {
        return empr_nroruc;
    }

    public void setEmpr_nroruc(String empr_nroruc) {
        this.empr_nroruc = empr_nroruc;
    }

    public String getEmpr_tipodoc() {
        return empr_tipodoc;
    }

    public void setEmpr_tipodoc(String empr_tipodoc) {
        this.empr_tipodoc = empr_tipodoc;
    }

    public String getClie_numero() {
        return clie_numero;
    }

    public void setClie_numero(String clie_numero) {
        this.clie_numero = clie_numero;
    }

    public String getClie_tipodoc() {
        return clie_tipodoc;
    }

    public void setClie_tipodoc(String clie_tipodoc) {
        this.clie_tipodoc = clie_tipodoc;
    }

    public String getClie_nombre() {
        return clie_nombre;
    }

    public void setClie_nombre(String clie_nombre) {
        this.clie_nombre = clie_nombre;
    }

    public String getDocu_fecha() {
        return docu_fecha;
    }

    public void setDocu_fecha(String docu_fecha) {
        this.docu_fecha = docu_fecha;
    }

    public String getDocu_hora() {
        return docu_hora;
    }

    public void setDocu_hora(String docu_hora) {
        this.docu_hora = docu_hora;
    }

    public String getDocu_tipodocumento() {
        return docu_tipodocumento;
    }

    public void setDocu_tipodocumento(String docu_tipodocumento) {
        this.docu_tipodocumento = docu_tipodocumento;
    }

    public String getDocu_numero() {
        return docu_numero;
    }

    public void setDocu_numero(String docu_numero) {
        this.docu_numero = docu_numero;
    }

    public String getDocu_moneda() {
        return docu_moneda;
    }

    public void setDocu_moneda(String docu_moneda) {
        this.docu_moneda = docu_moneda;
    }

    public double getDocu_gravada() {
        return docu_gravada;
    }

    public void setDocu_gravada(double docu_gravada) {
        this.docu_gravada = docu_gravada;
    }

    public double getDocu_inafecta() {
        return docu_inafecta;
    }

    public void setDocu_inafecta(double docu_inafecta) {
        this.docu_inafecta = docu_inafecta;
    }

    public double getDocu_exonerada() {
        return docu_exonerada;
    }

    public void setDocu_exonerada(double docu_exonerada) {
        this.docu_exonerada = docu_exonerada;
    }

    public double getDocu_gratuita() {
        return docu_gratuita;
    }

    public void setDocu_gratuita(double docu_gratuita) {
        this.docu_gratuita = docu_gratuita;
    }

    public double getDocu_descuento() {
        return docu_descuento;
    }

    public void setDocu_descuento(double docu_descuento) {
        this.docu_descuento = docu_descuento;
    }

    public double getDocu_subtotal() {
        return docu_subtotal;
    }

    public void setDocu_subtotal(double docu_subtotal) {
        this.docu_subtotal = docu_subtotal;
    }

    public double getDocu_total() {
        return docu_total;
    }

    public void setDocu_total(double docu_total) {
        this.docu_total = docu_total;
    }

    public double getDocu_igv() {
        return docu_igv;
    }

    public void setDocu_igv(double docu_igv) {
        this.docu_igv = docu_igv;
    }

    public String getTasa_igv() {
        return tasa_igv;
    }

    public void setTasa_igv(String tasa_igv) {
        this.tasa_igv = tasa_igv;
    }

    public double getDocu_isc() {
        return docu_isc;
    }

    public void setDocu_isc(double docu_isc) {
        this.docu_isc = docu_isc;
    }

    public String getTasa_isc() {
        return tasa_isc;
    }

    public void setTasa_isc(String tasa_isc) {
        this.tasa_isc = tasa_isc;
    }

    public double getDocu_otrostributos() {
        return docu_otrostributos;
    }

    public void setDocu_otrostributos(double docu_otrostributos) {
        this.docu_otrostributos = docu_otrostributos;
    }

    public String getTasa_otrostributos() {
        return tasa_otrostributos;
    }

    public void setTasa_otrostributos(String tasa_otrostributos) {
        this.tasa_otrostributos = tasa_otrostributos;
    }

    public double getDocu_otroscargos() {
        return docu_otroscargos;
    }

    public void setDocu_otroscargos(double docu_otroscargos) {
        this.docu_otroscargos = docu_otroscargos;
    }

    public double getDocu_percepcion() {
        return docu_percepcion;
    }

    public void setDocu_percepcion(double docu_percepcion) {
        this.docu_percepcion = docu_percepcion;
    }

    public String getHashcode() {
        return hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

    public String getCdr() {
        return cdr;
    }

    public void setCdr(String cdr) {
        this.cdr = cdr;
    }

    public String getCdr_nota() {
        return cdr_nota;
    }

    public void setCdr_nota(String cdr_nota) {
        this.cdr_nota = cdr_nota;
    }

    public String getCdr_observacion() {
        return cdr_observacion;
    }

    public void setCdr_observacion(String cdr_observacion) {
        this.cdr_observacion = cdr_observacion;
    }

    public String getDocu_enviaws() {
        return docu_enviaws;
    }

    public void setDocu_enviaws(String docu_enviaws) {
        this.docu_enviaws = docu_enviaws;
    }

    public String getDocu_proce_status() {
        return docu_proce_status;
    }

    public void setDocu_proce_status(String docu_proce_status) {
        this.docu_proce_status = docu_proce_status;
    }

    public String getDocu_proce_fecha() {
        return docu_proce_fecha;
    }

    public void setDocu_proce_fecha(String docu_proce_fecha) {
        this.docu_proce_fecha = docu_proce_fecha;
    }

    public String getDocu_link_pdf() {
        return docu_link_pdf;
    }

    public void setDocu_link_pdf(String docu_link_pdf) {
        this.docu_link_pdf = docu_link_pdf;
    }

    public String getDocu_link_cdr() {
        return docu_link_cdr;
    }

    public void setDocu_link_cdr(String docu_link_cdr) {
        this.docu_link_cdr = docu_link_cdr;
    }

    public String getDocu_link_xml() {
        return docu_link_xml;
    }

    public void setDocu_link_xml(String docu_link_xml) {
        this.docu_link_xml = docu_link_xml;
    }

    public String getClie_correo_cpe1() {
        return clie_correo_cpe1;
    }

    public void setClie_correo_cpe1(String clie_correo_cpe1) {
        this.clie_correo_cpe1 = clie_correo_cpe1;
    }

    public String getClie_correo_cpe2() {
        return clie_correo_cpe2;
    }

    public void setClie_correo_cpe2(String clie_correo_cpe2) {
        this.clie_correo_cpe2 = clie_correo_cpe2;
    }

 
 
}
