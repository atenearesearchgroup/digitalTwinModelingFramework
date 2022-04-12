package config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private final String outputClass;
    private final String inputClass;

    private final Map<String, String> outputAttributes;
    private final Map<String, String> commandsAttributes;

    private static ConfigurationManager config = null;

    @SuppressWarnings("unchecked")
    private ConfigurationManager(){
        String CONFIGURATION_FILE_PATH = "../../config/DTConfig.yaml";

        Map<String, Object> configurationParameters = loadConfigurationFile(getAbsolutePath() + CONFIGURATION_FILE_PATH);

        this.outputClass = configurationParameters.get("outputClass").toString();
        this.inputClass = configurationParameters.get("inputClass").toString();

        this.outputAttributes = getAttributes((Map<String, Object>) configurationParameters.get("outputAttributes"));
        this.commandsAttributes = getAttributes((Map<String, Object>) configurationParameters.get("commandAttributes"));
    }

    public static ConfigurationManager getConfig(){
        if(config == null){
            config = new ConfigurationManager();
        }
        return config;
    }

    private Map<String, Object> loadConfigurationFile(String path) {
        InputStream inputStream = null;
        Yaml yaml = new Yaml();
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return yaml.load(inputStream);
    }

    private Map<String, String> getAttributes(Map<String, Object> attsConfiguration){
        Map<String, String> result = new HashMap<>();
        for (String attName : attsConfiguration.keySet()) {
            result.put(attName, attsConfiguration.get(attName).toString());
        }
        return result;
    }

    private String getAbsolutePath(){
        File jarPath = new File(ConfigurationManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return jarPath.getParentFile().getAbsolutePath();
    }

    public String getOutputClass() {
        return outputClass;
    }

    public String getInputClass() {
        return inputClass;
    }

    public Map<String, String> getOutputAttributes() {
        return outputAttributes;
    }

    public Map<String, String> getCommandsAttributes() {
        return commandsAttributes;
    }

}
