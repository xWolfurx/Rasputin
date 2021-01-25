package net.wolfur.rasputin.file.builder;

import java.util.Map;

/**
 * Created by Wolfur on 16.10.2017.
 */

public interface Configuration extends ConfigurationSection {

    public void addDefault(String path, Object value);

    public void addDefaults(Map<String, Object> defaults);

    public void addDefaults(Configuration defaults);

    public void setDefaults(Configuration defaults);

    public Configuration getDefaults();

    public ConfigurationOptions options();
}
