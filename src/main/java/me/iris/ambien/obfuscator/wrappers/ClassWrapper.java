package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.CompetentClassWriter;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
public class ClassWrapper implements Opcodes {
    @Getter
    private final String name;

    @Getter
    @Setter
    private ClassNode node;

    @Getter
    private final List<MethodWrapper> methods;

    @Getter
    private final boolean isLibraryClass;

    public ClassWrapper(String name, ClassNode node, boolean isLibraryClass) {
        this.name = name;
        this.node = node;
        this.methods = new ArrayList<>();
        this.isLibraryClass = isLibraryClass;

        // Import methods from class
        Arrays.stream(node.methods.toArray())
                .map(methodObj -> (MethodNode)methodObj)
                .forEach(methodNode -> methods.add(new MethodWrapper(methodNode)));
    }

    public MethodNode getStaticInitializer() {
        // Check if the init method already exists in the class.
        for (MethodWrapper wrapper : methods) {
            if (wrapper.getNode().name.equals("<clinit>"))
                return wrapper.getNode();
        }

        // Create init method & return it
        final MethodBuilder builder = new MethodBuilder()
                .setAccess(ACC_STATIC)
                .setName("<clinit>")
                .setDesc("()V");
        final MethodNode methodNode = builder.buildNode();

        // Add return insn
        methodNode.instructions.add(new InsnNode(RETURN));

        // Add method to class
        addMethod(methodNode);

        // Return method
        return methodNode;
    }

    public List<MethodWrapper> getTransformableMethods() {
        return methods.stream()
                .filter(method -> !Ambien.get.exclusionManager.isMethodExcluded(name, method.getNode().name))
                .collect(Collectors.toList());
    }

    public boolean isInterface() {
        return (node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    public boolean isEnum() {
        return (node.access & Opcodes.ACC_ENUM) == Opcodes.ACC_ENUM;
    }


    public boolean isAnnotation() {
        return (node.access & Opcodes.ACC_ANNOTATION) == Opcodes.ACC_ANNOTATION;
    }

    public CopyOnWriteArrayList<FieldNode> getFields() {
        final CopyOnWriteArrayList<FieldNode> fields = new CopyOnWriteArrayList<>();
        for (final Object fieldObj : node.fields) {
            fields.add((FieldNode)fieldObj);
        }

        return fields;
    }

    public void addField(final FieldNode fieldNode) {
        node.fields.add(fieldNode);
    }

    public void addMethod(final MethodNode methodNode) {
        node.methods.add(methodNode);
        methods.add(new MethodWrapper(methodNode));
    }

    public byte[] toByteArray() {
        Ambien.LOGGER.debug("Converting class to bytes: {}", name);

        try {
            // Attempt to get bytes of class using COMPUTE_FRAMES
            final CompetentClassWriter writer = new CompetentClassWriter(ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            return writer.toByteArray();
        } catch (TypeNotPresentException | ArrayIndexOutOfBoundsException | NegativeArraySizeException e) {
            if (e instanceof NegativeArraySizeException)
                Ambien.LOGGER.warn("NegativeArraySizeException thrown when attempting to write class \"{}\" using COMPUTE_MAXS, this is most likely caused by malformed bytecode.", name);
            else
                Ambien.LOGGER.warn("Attempting to write class \"{}\" using COMPUTE_MAXS, some errors may appear during runtime.", name);

            // Attempt to get bytes of class using COMPUTE_MAXS
            final CompetentClassWriter writer = new CompetentClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        }
    }
}
