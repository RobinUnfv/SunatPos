/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo.Beans;

import java.sql.Date;

/**
 *
 * @author LUISINHO
 */
public class PagoBean {
 private int idpago;   
 private int docu_codigo;   
 private Date fecha;
 private Double monto;
 private int nrocuota;

    public int getIdpago() {
        return idpago;
    }

    public void setIdpago(int idpago) {
        this.idpago = idpago;
    }

    public int getDocu_codigo() {
        return docu_codigo;
    }

    public void setDocu_codigo(int docu_codigo) {
        this.docu_codigo = docu_codigo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public int getNrocuota() {
        return nrocuota;
    }

    public void setNrocuota(int nrocuota) {
        this.nrocuota = nrocuota;
    }



}
