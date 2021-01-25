package net.wolfur.rasputin.file.builder.serialization;

import java.util.Map;

/**
 * Created by Wolfur on 16.10.2017.
 */
public interface ConfigurationSerializable {

    public Map<String, Object> serialize();
}
