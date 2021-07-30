package plugin;

import org.tzi.use.runtime.IPluginRuntime;
import org.tzi.use.runtime.impl.Plugin;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class DigitalTwinConnector extends Plugin {

	final protected String PLUGIN_ID = "useDigitalTwinConnector";

	public String getName() {
		return this.PLUGIN_ID;
	}

	public void run(IPluginRuntime pluginRuntime) throws Exception {
		// Nothing to initialize
	}
}
