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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LecturaXML {

    private static Log log = LogFactory.getLog(LecturaXML.class);

    public static String getRespuestaSunat(String path) {
        String respuesta = null;
        String nota = "";
//        Connection conn = null;

        try {
            log.info("LecturaXML.getRespuestaSunat - iniciamos Lectura del contenido del CDR " + path);
            DocumentBuilderFactory fabricaCreadorDocumento = DocumentBuilderFactory.newInstance();
            DocumentBuilder creadorDocumento = fabricaCreadorDocumento.newDocumentBuilder();
            Document documento = creadorDocumento.parse(path);
            //Obtener el elemento raíz del documento
            Element raiz = documento.getDocumentElement();

            //Obtener la lista de nodos que tienen etiqueta "ds:Reference"
            NodeList responsecode = raiz.getElementsByTagName("cbc:ResponseCode");
            for (int i = 0; i < responsecode.getLength(); i++) {
                Node empleado = responsecode.item(i);
                Node datoContenido = empleado.getFirstChild();
                respuesta = datoContenido.getNodeValue();
            }
            NodeList nodesc = raiz.getElementsByTagName("cbc:Description");
            for (int i = 0; i < nodesc.getLength(); i++) {
                Node empleado = nodesc.item(i);
                Node datoContenido = empleado.getFirstChild();
                respuesta = respuesta + "|" + datoContenido.getNodeValue();
            }
            NodeList note = raiz.getElementsByTagName("cbc:Note");
            for (int i = 0; i < note.getLength(); i++) {
                Node empleado = note.item(i);
                Node datoContenido = empleado.getFirstChild();
                nota = nota + datoContenido.getNodeValue() + "\\n";
            }

            String[] cdr = respuesta.split("\\|", 0);
            //=== Guardar el link

        } catch (org.xml.sax.SAXException ex) {
            System.out.println("ERROR: El formato XML del fichero no es correcto\n" + ex.getMessage());
            ex.printStackTrace();
            log.error("LecturaXML.getRespuestaSunat - Error : " + ex.toString());
            Logger.getLogger(LecturaXML.class.getName()).log(Level.SEVERE, null, ex);
            respuesta = "Error al leer el archivo de respuesta";
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo de respuesta\n" + ex.getMessage());
            ex.printStackTrace();
            log.error("LecturaXML.getRespuestaSunat - Error : " + ex.toString());
            respuesta = "Error al leer el archivo de respuesta";
            Logger.getLogger(LecturaXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            System.out.println("ERROR: No se ha podido crear el generador de documentos XML\n" + ex.getMessage());
            ex.printStackTrace();
            log.error("LecturaXML.getRespuestaSunat - Error : " + ex.toString());
            respuesta = "Error al leer el archivo de respuesta";
            Logger.getLogger(LecturaXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            log.error("LecturaXML.getRespuestaSunat - Error : " + ex.toString());
            Logger.getLogger(LecturaXML.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            ConnectionPool.closeConexion(conn);
        }
        return respuesta;
    }

    public static String obtenerDigestValue(String path) {
        String firma = null;
        try {
            DocumentBuilderFactory fabricaCreadorDocumento = DocumentBuilderFactory.newInstance();
            // *** CORRECCIÓN 1: Habilitar namespace awareness ***
            fabricaCreadorDocumento.setNamespaceAware(true);

            DocumentBuilder creadorDocumento = fabricaCreadorDocumento.newDocumentBuilder();
            Document documento = creadorDocumento.parse(path);

            // *** CORRECCIÓN 2: Buscar DigestValue usando namespace URI ***
            // El namespace de la firma digital es: http://www.w3.org/2000/09/xmldsig#
            NodeList listaDigestValue = documento.getElementsByTagNameNS(
                    "http://www.w3.org/2000/09/xmldsig#",
                    "DigestValue"
            );

            // Si encontramos al menos un DigestValue
            if (listaDigestValue.getLength() > 0) {
                Node digestValueNode = listaDigestValue.item(0);
                if (digestValueNode != null && digestValueNode.getTextContent() != null) {
                    firma = digestValueNode.getTextContent().trim();
                }
            }

        } catch (org.xml.sax.SAXException ex) {
            System.out.println("ERROR: El formato XML del fichero no es correcto\n" + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("ERROR: Se ha producido un error al leer el fichero\n" + ex.getMessage());
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            System.out.println("ERROR: No se ha podido crear el generador de documentos XML\n" + ex.getMessage());
            ex.printStackTrace();
        }
        return firma;
    }


}

