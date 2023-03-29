package me.iris.ambien.obfuscator.transformers.data;

public enum Stability {
    STABLE("Stable"),
    EXPERIMENTAL("Experimental");

    private final String name;
    Stability(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
