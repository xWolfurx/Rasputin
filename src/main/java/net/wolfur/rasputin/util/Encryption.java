package net.wolfur.rasputin.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Encryption {

    public static String encodeString(String string) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] data = string.getBytes(StandardCharsets.UTF_8);
        return encoder.encodeToString(data);
    }

    public static String decodeString(String string) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(string);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
