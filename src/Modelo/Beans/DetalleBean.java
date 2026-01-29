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
public class DetalleBean {
    private int iddetalle;
    private int docu_codigo;
    private int item_orden;
    private String item_unidad;
    private int item_cantidad;
    private String item_codproducto;
    private String item_descripcion;
    private String item_afectacion;
    private String item_tipo_precio_venta;
    private double item_pventa;
    private double item_pventa_nohonerosa;
    private double item_to_subtotal;
    private double item_to_igv;
    private double item_pvtaigv;

    public double getItem_pvtaigv() {
        return item_pvtaigv;
    }

    public void setItem_pvtaigv(double item_pvtaigv) {
        this.item_pvtaigv = item_pvtaigv;
    }

    public int getIddetalle() {
        return iddetalle;
    }

    public void setIddetalle(int iddetalle) {
        this.iddetalle = iddetalle;
    }

    public int getDocu_codigo() {
        return docu_codigo;
    }

    public void setDocu_codigo(int docu_codigo) {
        this.docu_codigo = docu_codigo;
    }

    public int getItem_orden() {
        return item_orden;
    }

    public void setItem_orden(int item_orden) {
        this.item_orden = item_orden;
    }

    public String getItem_unidad() {
        return item_unidad;
    }

    public void setItem_unidad(String item_unidad) {
        this.item_unidad = item_unidad;
    }

    public int getItem_cantidad() {
        return item_cantidad;
    }

    public void setItem_cantidad(int item_cantidad) {
        this.item_cantidad = item_cantidad;
    }

    public String getItem_codproducto() {
        return item_codproducto;
    }

    public void setItem_codproducto(String item_codproducto) {
        this.item_codproducto = item_codproducto;
    }

    public String getItem_descripcion() {
        return item_descripcion;
    }

    public void setItem_descripcion(String item_descripcion) {
        this.item_descripcion = item_descripcion;
    }

    public String getItem_afectacion() {
        return item_afectacion;
    }

    public void setItem_afectacion(String item_afectacion) {
        this.item_afectacion = item_afectacion;
    }

    public String getItem_tipo_precio_venta() {
        return item_tipo_precio_venta;
    }

    public void setItem_tipo_precio_venta(String item_tipo_precio_venta) {
        this.item_tipo_precio_venta = item_tipo_precio_venta;
    }

    public double getItem_pventa() {
        return item_pventa;
    }

    public void setItem_pventa(double item_pventa) {
        this.item_pventa = item_pventa;
    }

    public double getItem_pventa_nohonerosa() {
        return item_pventa_nohonerosa;
    }

    public void setItem_pventa_nohonerosa(double item_pventa_nohonerosa) {
        this.item_pventa_nohonerosa = item_pventa_nohonerosa;
    }

    public double getItem_to_subtotal() {
        return item_to_subtotal;
    }

    public void setItem_to_subtotal(double item_to_subtotal) {
        this.item_to_subtotal = item_to_subtotal;
    }

    public double getItem_to_igv() {
        return item_to_igv;
    }

    public void setItem_to_igv(double item_to_igv) {
        this.item_to_igv = item_to_igv;
    }
    
    
}
