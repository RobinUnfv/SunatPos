package Modelo.Util;

import Modelo.Beans.ConfigSunatBean;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

/**
 * ══════════════════════════════════════════════════════════════════════════════════════
 * HEADER HANDLER RESOLVER - VERSIÓN REFACTORIZADA
 * ══════════════════════════════════════════════════════════════════════════════════════
 *
 * CAMBIOS REALIZADOS:
 * - Ahora puede recibir ConfigSunatBean para pasar las credenciales al HeaderHandler
 * - Se mantiene compatibilidad hacia atrás con el constructor sin parámetros
 *
 * ══════════════════════════════════════════════════════════════════════════════════════
 */
public class HeaderHandlerResolver implements HandlerResolver {

    private String vruc;
    private ConfigSunatBean configSunat;

    /**
     * Constructor por defecto - mantiene compatibilidad hacia atrás
     */
    public HeaderHandlerResolver() {
        this.configSunat = null;
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * NUEVO CONSTRUCTOR: Recibe ConfigSunatBean con las credenciales
     * ═══════════════════════════════════════════════════════════════════════
     *
     * @param config ConfigSunatBean con la configuración cargada desde BD
     */
    public HeaderHandlerResolver(ConfigSunatBean config) {
        this.configSunat = config;
    }

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        List<Handler> handlerChain = new ArrayList<Handler>();

        // ═══════════════════════════════════════════════════════════════════════
        // ANTES:
        // HeaderHandler hh = new HeaderHandler();
        //
        // AHORA (con ConfigSunatBean):
        // ═══════════════════════════════════════════════════════════════════════
        HeaderHandler hh;
        if (configSunat != null) {
            hh = new HeaderHandler(configSunat);
        } else {
            hh = new HeaderHandler();
        }

        hh.setVruc(vruc);
        handlerChain.add(hh);

        return handlerChain;
    }

    public String getVruc() {
        return vruc;
    }

    public void setVruc(String vruc) {
        this.vruc = vruc;
    }

    public ConfigSunatBean getConfigSunat() {
        return configSunat;
    }

    public void setConfigSunat(ConfigSunatBean configSunat) {
        this.configSunat = configSunat;
    }
}