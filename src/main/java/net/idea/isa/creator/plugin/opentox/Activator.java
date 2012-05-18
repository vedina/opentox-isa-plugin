package net.idea.isa.creator.plugin.opentox;

import java.util.Hashtable;

import org.isatools.isacreator.plugins.host.service.PluginOntologyCVSearch;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/10/2011
 *         Time: 16:06
 */
public class Activator implements BundleActivator {



    public void start(BundleContext context) {

        Hashtable dict = new Hashtable();
        context.registerService(
                PluginOntologyCVSearch.class.getName(), new OpenToxRESTClient(), dict);
    }


    public void stop(BundleContext context) {
    }
}