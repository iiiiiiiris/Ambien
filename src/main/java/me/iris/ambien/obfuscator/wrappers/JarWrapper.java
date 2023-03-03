package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JarWrapper {
    @Getter
    private final List<String> directories;

    @Getter
    private final List<ClassWrapper> classes;

    @Getter
    private final HashMap<String, byte[]> resources;

    public JarWrapper() {
        this.directories = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.resources = new HashMap<>();
    }
}
