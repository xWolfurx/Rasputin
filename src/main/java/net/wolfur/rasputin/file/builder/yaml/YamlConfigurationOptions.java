package net.wolfur.rasputin.file.builder.yaml;

import org.apache.commons.lang3.Validate;

/**
 * Created by Wolfur on 17.10.2017.
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {

    private int indent = 2;

    protected YamlConfigurationOptions(YamlConfiguration configuration) {
        super(configuration);
    }

    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override
    public YamlConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions header(String value) {
        super.header(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions copyHeader(boolean value) {
        super.copyHeader(value);
        return this;
    }

    public int indent() {
        return indent;
    }

    public YamlConfigurationOptions indent(int value) {
        Validate.isTrue(value >= 2, "Indent must be at least 2 characters");
        Validate.isTrue(value <= 9, "Indent cannot be greater than 9 characters");

        this.indent = value;
        return this;
    }

}
