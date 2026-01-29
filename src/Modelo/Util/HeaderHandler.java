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
//                SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                
//aguegue esto 
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
 //hastaqui arriba
 
                 SOAPElement security = 
                  header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
//                usernameToken.addAttribute(new QName("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
                SOAPElement username = usernameToken.addChildElement("Username", "wsse");

                //aqui va el RUC JUNTO AL USUARIO SECUNDARIO DE LA EMPRESA
                username.addTextNode("20601441102SUDASYST");

                SOAPElement password = usernameToken.addChildElement("Password", "wsse");
//                password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
                //aqui va la clave sol
                password.addTextNode("Lucho789");
                //Print out the outbound SOAP message to System.out
                    message.writeTo(System.out);
                    System.out.println("");

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                    //This handler does nothing with the response from the Web Service so
            //we just print out the SOAP message.
            SOAPMessage message = smc.getMessage();
            message.writeTo(System.out);
            System.out.println("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return outboundProperty;

    }
/*QUITE ESTO
    public Set getHeaders() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return null;
    }
*/
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
        //throw new UnsupportedOperationException("Not supported yet.");
        return true;
    }

    public void close(MessageContext context) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getVruc() {
        return vruc;
    }

    public void setVruc(String vruc) {
        this.vruc = vruc;
    }
}
