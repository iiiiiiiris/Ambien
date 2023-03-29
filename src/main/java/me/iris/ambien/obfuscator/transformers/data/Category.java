package me.iris.ambien.obfuscator.transformers.data;

public enum Category {
    CONTROL_FLOW("Control flow"),
    DATA("Data"),
    EXPLOITS("Exploits"),
    PACKAGING("Packaging"),
    OPTIMIZATION("Optimization"),
    MISCELLANEOUS("Miscellaneous");

    private final String name;
    Category(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
