
package Modelo.Util;

import Modelo.Beans.ConfigSunatBean;
import java.util.Set;
import java.util.HashSet;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

    public String vruc;

    // ═══════════════════════════════════════════════════════════════════════
    // NUEVO: Variables para almacenar las credenciales desde ConfigSunatBean
    // ═══════════════════════════════════════════════════════════════════════
    private String usuarioSunat;
    private String claveSunat;

    /**
     * Constructor por defecto - usa valores por defecto (Beta)
     * Se mantiene para compatibilidad hacia atrás
     */
    public HeaderHandler() {
        // Valores por defecto (Beta) - se usarán si no se proporciona ConfigSunatBean
        this.usuarioSunat = "20609272016MODDATOS";
        this.claveSunat = "MODDATOS";
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * NUEVO CONSTRUCTOR: Recibe ConfigSunatBean con las credenciales
     * ═══════════════════════════════════════════════════════════════════════
     *
     * @param config ConfigSunatBean con la configuración cargada desde BD
     */
    public HeaderHandler(ConfigSunatBean config) {
        if (config != null) {
            // Obtener credenciales según el ambiente activo
            this.usuarioSunat = config.getUsuarioSunatActivo();
            this.claveSunat = config.getClaveSunatActiva();

            System.out.println("HeaderHandler: Credenciales cargadas desde ConfigSunatBean");
            System.out.println("  - Ambiente: " + config.getNombreAmbiente());
            System.out.println("  - Usuario: " + this.usuarioSunat);
        } else {
            // Valores por defecto si config es null
            this.usuarioSunat = "20609272016MODDATOS";
            this.claveSunat = "MODDATOS";
            System.out.println("HeaderHandler: Config null, usando valores por defecto (Beta)");
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * NUEVO MÉTODO: Permite actualizar las credenciales dinámicamente
     * ═══════════════════════════════════════════════════════════════════════
     */
    public void setCredenciales(ConfigSunatBean config) {
        if (config != null) {
            this.usuarioSunat = config.getUsuarioSunatActivo();
            this.claveSunat = config.getClaveSunatActiva();
        }
    }

    public boolean handleMessage(SOAPMessageContext smc) {

        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty.booleanValue()) {
            SOAPMessage message = smc.getMessage();

            try {

                SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();

                //esto agregue
                if(envelope.getHeader()!=null){envelope.getHeader().detachNode();}
                //hasta aqui arriba

                SOAPHeader header = envelope.addHeader();

                //agregue esto
                envelope.setPrefix("soapenv");
                header.setPrefix("soapenv");
                envelope.getBody().setPrefix("soapenv");
                envelope.removeAttribute("xmlns:S");

                SOAPElement ser =
                        envelope.addAttribute(new QName("xmlns:ser"), "http://service.sunat.gob.pe");
                envelope.removeAttribute("xmlns:soapenv");
                SOAPElement soapenv =
                        envelope.addAttribute(new QName("xmlns:soapenv"), "http://schemas.xmlsoap.org/soap/envelope/");

                SOAPElement wsse =
                        envelope.addAttribute(new QName("xmlns:wsse"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                //hasta aqui arriba

                SOAPElement security =
                        header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
                SOAPElement username = usernameToken.addChildElement("Username", "wsse");

                // ═══════════════════════════════════════════════════════════════════════
                // ANTES (hardcodeado):
                // username.addTextNode("20609272016CORPOTCE"); // PRODUCCION
                // username.addTextNode("20609272016MODDATOS"); // DEMO
                //
                // AHORA (desde ConfigSunatBean):
                // ═══════════════════════════════════════════════════════════════════════
                username.addTextNode(this.usuarioSunat);

                SOAPElement password = usernameToken.addChildElement("Password", "wsse");

                // ═══════════════════════════════════════════════════════════════════════
                // ANTES (hardcodeado):
                // password.addTextNode("DRAVErFACEL2"); // PRODUCCION
                // password.addTextNode("MODDATOS"); // DEMO
                //
                // AHORA (desde ConfigSunatBean):
                // ═══════════════════════════════════════════════════════════════════════
                password.addTextNode(this.claveSunat);

                //Print out the outbound SOAP message to System.out
                System.out.println("══════════════════════════════════════════════════════════════════");
                System.out.println("SOAP Request - Usuario: " + this.usuarioSunat);
                System.out.println("══════════════════════════════════════════════════════════════════");
                message.writeTo(System.out);
                System.out.println();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                //This handler does nothing with the response from the Web Service so
                //we just print out the SOAP message.
                SOAPMessage message = smc.getMessage();
                System.out.println("══════════════════════════════════════════════════════════════════");
                System.out.println("SOAP Response:");
                System.out.println("══════════════════════════════════════════════════════════════════");
                message.writeTo(System.out);
                System.out.println();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return outboundProperty;

    }

    public Set getHeaders() {
        // The code below is added on order to invoke Spring secured WS.
        // Otherwise,
        // http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd
        // won't be recognised
        final QName securityHeader = new QName(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "Security", "wsse");

        final HashSet headers = new HashSet();
        headers.add(securityHeader);

        return headers;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
    }

    public String getVruc() {
        return vruc;
    }

    public void setVruc(String vruc) {
        this.vruc = vruc;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NUEVOS GETTERS
    // ═══════════════════════════════════════════════════════════════════════

    public String getUsuarioSunat() {
        return usuarioSunat;
    }

    public String getClaveSunat() {
        return claveSunat;
    }
}
