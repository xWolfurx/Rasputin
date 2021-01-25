package net.wolfur.rasputin.file.builder;

/**
 * Created by Wolfur on 17.10.2017.
 */

@SuppressWarnings("serial")
public class InvalidConfigurationException extends Exception {

    public InvalidConfigurationException() {

    }

    public InvalidConfigurationException(String msg) {
        super(msg);
    }

    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

    public InvalidConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
