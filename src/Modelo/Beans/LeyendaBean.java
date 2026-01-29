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
public class LeyendaBean {
 private int id_leyenda;   
 private int docu_codigo;   
 private String leyenda_codigo;   
 private String leyenda_texto;   

    public int getId_leyenda() {
        return id_leyenda;
    }

    public void setId_leyenda(int id_leyenda) {
        this.id_leyenda = id_leyenda;
    }

    public int getDocu_codigo() {
        return docu_codigo;
    }

    public void setDocu_codigo(int docu_codigo) {
        this.docu_codigo = docu_codigo;
    }

    public String getLeyenda_codigo() {
        return leyenda_codigo;
    }

    public void setLeyenda_codigo(String leyenda_codigo) {
        this.leyenda_codigo = leyenda_codigo;
    }

    public String getLeyenda_texto() {
        return leyenda_texto;
    }

    public void setLeyenda_texto(String leyenda_texto) {
        this.leyenda_texto = leyenda_texto;
    }



}
