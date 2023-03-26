package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;
import me.iris.ambien.obfuscator.Ambien;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
public class ClassWrapper {
    @Getter
    private final String name;

    @Getter
    private final ClassNode node;

    @Getter
    private final boolean isLibraryClass;

    public ClassWrapper(String name, ClassNode node, boolean isLibraryClass) {
        this.name = name;
        this.node = node;
        this.isLibraryClass = isLibraryClass;
    }

    public boolean isInterface() {
        return (node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    public MethodNode getClassInit() {
        for (final Object methodObj : node.methods) {
            final MethodNode methodNode = (MethodNode)methodObj;
            if (methodNode.name.equals("<init>"))
                return methodNode;
        }

        return null;
    }

    public MethodNode getStaticClassInit() {
        for (final Object methodObj : node.methods) {
            final MethodNode methodNode = (MethodNode)methodObj;
            if (methodNode.name.equals("<clinit>"))
                return methodNode;
        }

        return null;
    }

    public CopyOnWriteArrayList<MethodNode> getTransformableMethods() {
        final CopyOnWriteArrayList<MethodNode> methods = new CopyOnWriteArrayList<>();
        for (final Object methodObj : node.methods) {
            final MethodNode methodNode = (MethodNode)methodObj;
            if (!Ambien.get.excludedClasses.contains(methodNode.name))
                methods.add(methodNode);
        }

        return methods;
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
    }

    public byte[] toByteArray() {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
