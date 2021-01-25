package net.wolfur.rasputin.util;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

public class RandomStringGenerator {

    private final Random random;
    private final char[] symbols;
    private final char[] buf;

    public RandomStringGenerator(int length, Random random, Type type) {
        if(length < 1) throw new IllegalArgumentException();
        if(type.getCharacters().length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = type.getCharacters().toCharArray();
        this.buf = new char[length];
    }

    public RandomStringGenerator(int length, Random random) {
        this(length, random, Type.ALPHANUMERIC);
    }

    public RandomStringGenerator(int length) {
        this(length, new SecureRandom());
    }

    public RandomStringGenerator() {
        this(21);
    }

    public String nextString() {
        for(int index = 0; index < buf.length; index++) {
            buf[index] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

    public enum Type {

        ALPHA("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        NUMERIC("0123456789"),
        ALPHANUMERIC("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        private String characters;

        Type(String characters) {
            this.characters = characters;
        }

        public String getCharacters() {
            return this.characters;
        }

    }

}
