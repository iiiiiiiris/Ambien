package me.iris.ambien.obfuscator.transformers;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExclusionManager {
    private static final String EXCLUDE_DESC = "Lme/iris/ambien/library/annotations/Exclude;";

    private final List<String> excludedClasses;
    private final Map<String, String> excludedMethods;
    private final Map<String, List<String>> transformerClassExclusions;

    public ExclusionManager(JarWrapper wrapper) {
        this.excludedClasses = new ArrayList<>();
        this.excludedMethods = new TreeMap<>();
        this.transformerClassExclusions = new TreeMap<>();

        // Add excluded classes that were defined in the config
        excludedClasses.addAll(Ambien.get.excludedClasses);

        // Build a map of transformers and their excluded classes
        Ambien.get.transformerManager.getTransformers().forEach(transformer -> {
            final List<String> exclusions = transformer.excludedClasses.getOptions();
            if (!exclusions.isEmpty())
                transformerClassExclusions.put(transformer.getName(), exclusions);
        });

        // Check for exclusions
        wrapper.getClasses().forEach(classWrapper -> {
            final AtomicBoolean isClassExcluded = new AtomicBoolean(false);

            // Check if the Exclude annotation is present
            if (classWrapper.getNode().invisibleAnnotations != null && !classWrapper.getNode().invisibleAnnotations.isEmpty()) {
                classWrapper.getNode().invisibleAnnotations.forEach(annotationNode -> {
                    if (annotationNode.desc.equals(EXCLUDE_DESC)) {
                        excludedClasses.add(classWrapper.getName());
                        isClassExcluded.set(true);
                    }
                });
            }

            // Check if the Exclude annotation is present on methods
            if (!isClassExcluded.get() && !classWrapper.getMethods().isEmpty()) {
                classWrapper.getMethods().forEach(methodWrapper -> {
                    if (methodWrapper.getNode().invisibleAnnotations != null && !methodWrapper.getNode().invisibleAnnotations.isEmpty()) {
                        methodWrapper.getNode().invisibleAnnotations.forEach(annotationNode -> {
                            if (annotationNode.desc.equals(EXCLUDE_DESC))
                                excludedMethods.put(classWrapper.getName(), methodWrapper.getNode().name);
                        });
                    }
                });
            }
        });
    }

    public boolean isClassExcluded(final String transformerName, final String className) {
        if (excludedClasses.contains(className))
            return true;

        if (transformerClassExclusions.containsKey(transformerName))
            return transformerClassExclusions.get(transformerName).contains(className);

        return false;
    }

    public boolean isMethodExcluded(final String ownerClass, final String methodName) {
        if (excludedMethods.containsKey(ownerClass))
            return excludedMethods.get(ownerClass).contains(methodName);

        return false;
    }

    public void removeExcludeAnnotations(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            // Remove the annotation from classes
            if (classWrapper.getNode().invisibleAnnotations != null && !classWrapper.getNode().invisibleAnnotations.isEmpty()) {
                final List<AnnotationNode> copy = new ArrayList<>(classWrapper.getNode().invisibleAnnotations);
                copy.forEach(annotationNode -> {
                    if (annotationNode.desc.equals(EXCLUDE_DESC))
                        classWrapper.getNode().invisibleAnnotations.remove(annotationNode);
                });
            }

            // Remove the annotation from methods
            classWrapper.getMethods().forEach(methodWrapper -> {
                if (methodWrapper.getNode().invisibleAnnotations != null && !methodWrapper.getNode().invisibleAnnotations.isEmpty()) {
                    final List<AnnotationNode> copy = new ArrayList<>(methodWrapper.getNode().invisibleAnnotations);
                    copy.forEach(annotationNode -> {
                        if (annotationNode.desc.equals(EXCLUDE_DESC))
                            methodWrapper.getNode().invisibleAnnotations.remove(annotationNode);
                    });
                }
            });
        });
    }
}
