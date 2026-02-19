/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo.Util;

/**
 *
 * @author LUISINHO
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Modelo.Beans.CabeceraBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class GeneralFunctions {

    private static Log log = LogFactory.getLog(LecturaXML.class);


    public static String crearZip(CabeceraBean items, String unidadEnvio, File signatureFile) {
        String resultado = "";
        try {
            //Mandar a zip
            log.info("generarXMLZipiadoFactura - Crear ZIP ");
            String inputFile = signatureFile.toString();
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(unidadEnvio + items.getEmpr_nroruc() + "-" + items.getDocu_tipodocumento() + "-" + items.getDocu_numero() + ".zip");

            byte b[] = new byte[2048];
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                ZipEntry entry2 = new ZipEntry(items.getEmpr_nroruc() + "-" + items.getDocu_tipodocumento() + "-" + items.getDocu_numero() + ".xml");
                zipOut.putNextEntry(entry2);
                System.out.println("==>Zip generado: " + items.getEmpr_nroruc() + "-" + items.getDocu_tipodocumento() + "-" + items.getDocu_numero() + ".zip");
                int len = 0;
                while ((len = in.read(b)) != -1) {
                    zipOut.write(b, 0, len);
                }
                zipOut.closeEntry();
            }
            out.close();
            in.close();
            log.info("generarXMLZipiadoFactura - Zip creado " + unidadEnvio + items.getEmpr_nroruc() + "-" + items.getDocu_tipodocumento() + "-" + items.getDocu_numero() + ".zip");

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("generarXMLZipiadoFactura - error  " + ex.toString());

        }
        return resultado;
    }
    
    public static String crearZip2(CabeceraBean items, String unidadEnvio, File signatureFile) {
        String resultado = "";
        try {
            //Mandar a zip
            log.info("generarXMLZipiadoFactura - Crear ZIP ");
            String inputFile = signatureFile.toString();
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(unidadEnvio + items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().replace("-", "")+"-1.zip");

            byte b[] = new byte[2048];
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                ZipEntry entry2 = new ZipEntry(items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().replace("-", "")+"-1.xml");
                zipOut.putNextEntry(entry2);
                System.out.println("==>Zip generado: " + items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().replace("-", "")+"-1.zip");
                int len = 0;
                while ((len = in.read(b)) != -1) {
                    zipOut.write(b, 0, len);
                }
                zipOut.closeEntry();
            }
            out.close();
            in.close();
            log.info("generarXMLZipiadoFactura - Zip creado " + unidadEnvio + items.getEmpr_nroruc() + "-RC-" + items.getDocu_fecha().replace("-", "")+"-1.zip");

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("generarXMLZipiadoFactura - error  " + ex.toString());

        }
        return resultado;
    }
    public static String crearZip3(CabeceraBean items, String unidadEnvio, File signatureFile) {
        String resultado = "";
        try {
            //Mandar a zip
            log.info("generarXMLZipiadoFactura - Crear ZIP ");
            String inputFile = signatureFile.toString();
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(unidadEnvio + items.getEmpr_nroruc() + "-RA-" + items.getDocu_fecha().replace("-", "")+"-1.zip");

            byte b[] = new byte[2048];
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                ZipEntry entry2 = new ZipEntry(items.getEmpr_nroruc() + "-RA-" + items.getDocu_fecha().replace("-", "")+"-1.xml");
                zipOut.putNextEntry(entry2);
                System.out.println("==>Zip generado: " + items.getEmpr_nroruc() + "-RA-" + items.getDocu_fecha().replace("-", "")+"-1.zip");
                int len = 0;
                while ((len = in.read(b)) != -1) {
                    zipOut.write(b, 0, len);
                }
                zipOut.closeEntry();
            }
            out.close();
            in.close();
            log.info("generarXMLZipiadoFactura - Zip creado " + unidadEnvio + items.getEmpr_nroruc() + "-RA-" + items.getDocu_fecha().replace("-", "")+"-1.zip");

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("generarXMLZipiadoFactura - error  " + ex.toString());

        }
        return resultado;
    }

    /**
     * Crea el archivo ZIP para la Comunicación de Baja
     * @param ruc RUC del emisor
     * @param identificador Identificador (RA-YYYYMMDD-correlativo)
     * @param unidadEnvio Ruta de la carpeta de envío
     * @param signatureFile Archivo XML firmado
     * @return Resultado del proceso
     */
    public static String crearZipComunicacionBaja(String ruc, String identificador, String unidadEnvio, File signatureFile) {
        String resultado = "";
        try {
            String nombreZip = ruc + "-" + identificador + ".zip";
            String nombreXml = ruc + "-" + identificador + ".xml";

            FileOutputStream fos = new FileOutputStream(unidadEnvio + nombreZip);
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);

            // Agregar el XML al ZIP
            java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry(nombreXml);
            zos.putNextEntry(ze);

            FileInputStream fis = new FileInputStream(signatureFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            fis.close();
            zos.closeEntry();
            zos.close();
            fos.close();

            resultado = "0|ZIP creado: " + nombreZip;
            System.out.println("ZIP creado exitosamente: " + unidadEnvio + nombreZip);

        } catch (Exception e) {
            resultado = "0100|Error al crear ZIP: " + e.getMessage();
            e.printStackTrace();
        }
        return resultado;
    }

    
}

