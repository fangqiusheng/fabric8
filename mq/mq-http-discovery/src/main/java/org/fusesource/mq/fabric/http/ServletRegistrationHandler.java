package org.fusesource.mq.fabric.http;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.fusesource.fabric.service.support.AbstractComponent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Component(name = "org.fusesource.mq.fabric.http", description = "Fabric Discovery Servlet", immediate = true)
public class ServletRegistrationHandler extends AbstractComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletRegistrationHandler.class);

    @Reference(referenceInterface = HttpService.class)
    private HttpService httpService;
    @Reference(referenceInterface = CuratorFramework.class)
    private CuratorFramework curator;

    @Activate
    synchronized void activate(ComponentContext context, Map<String, String> properties) {
        activateComponent(context);
        try {
            FabricDiscoveryServlet discoveryServlet = new FabricDiscoveryServlet();
            discoveryServlet.setCurator(curator);
            HttpContext base = httpService.createDefaultHttpContext();
            httpService.registerServlet("/mq-discovery", discoveryServlet, createParams("mq-discovery"), base);
        } catch (Throwable t) {
            deactivateComponent();
            LOGGER.warn("Failed to register fabric maven proxy servlets, due to:" + t.getMessage());
        }
    }

    @Deactivate
    synchronized void deactivate() {
        try {
            try {
                if (httpService != null) {
                    httpService.unregister("/mq-discovery");
                }
            } catch (Exception ex) {
                LOGGER.warn("Http service returned error on servlet unregister. Possibly the service has already been stopped");
            }
        } finally {
            deactivateComponent();
        }
    }

    private Dictionary createParams(String name) {
        Dictionary d = new Hashtable();
        d.put("servlet-name", name);
        return d;
    }
}
