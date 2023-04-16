package me.iris.ambien.obfuscator.transformers.implementations.optimization;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

@TransformerInfo(
        name = "remove-unused",
        category = Category.OPTIMIZATION,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "Removes unused methods & fields from classes."
)
public class RemoveUnused extends Transformer {
    /**
     * Prevent specific methods from classes from being removed
     * For example: me/iris/testjar/Entry/anotherMethod
     * This will exclude the main method from being removed
     */
    public final ListSetting exclusions = new ListSetting("exclusions", new ArrayList<>());

    @Override
    public void transform(JarWrapper wrapper) {
        removeMethods(wrapper);
        removeFields(wrapper);
    }

    private void removeMethods(final JarWrapper wrapper) {
        // Get methods from every class & store the names in a list
        final List<String> methods = new ArrayList<>();
        wrapper.getClasses().forEach(classWrapper ->
                classWrapper.getMethods().stream()
                        .filter(methodWrapper -> !methodWrapper.isInitializer())
                        .forEach(methodWrapper -> {
                            final MethodNode node = methodWrapper.getNode();

                            // make sure it isn't the main method
                            if (node.name.equals("main") && node.desc.equals("([Ljava/lang/String;)V")) return;

                            // Path & name of method
                            final String fullName = classWrapper.getNode().name + "/" + methodWrapper.getNode().name;

                            // make sure the method isn't in the exclusions list
                            if (exclusions.getOptions().contains(fullName)) return;

                            methods.add(fullName);
                        }));

        // Get all referenced methods
        final List<String> referencedMethods = new ArrayList<>();
        wrapper.getClasses().forEach(classWrapper ->
                classWrapper.getMethods().forEach(methodWrapper ->
                        methodWrapper.getInstructions()
                                .filter(insn -> insn instanceof MethodInsnNode)
                                .map(insn -> (MethodInsnNode)insn)
                                .forEach(insn -> referencedMethods.add(insn.owner + "/" + insn.name))));

        // Loop through methods & check if it was referenced somewhere
        for (String m : methods) {
            if (!referencedMethods.contains(m)) {
                final int lastSlashIdx = m.lastIndexOf("/");

                // Get class/owner from string
                final String owner = m.substring(0, lastSlashIdx);

                // Get method name
                final String method = m.substring(lastSlashIdx + 1);

                // Find & remove method
                getClasses(wrapper).forEach(classWrapper -> {
                    if (classWrapper.getNode().name.equals(owner))
                        classWrapper.getNode().methods.removeIf(methodNode -> methodNode.name.equals(method));
                });

                Ambien.LOGGER.info("Removed unused method: {}", m);
            }
        }
    }

    private void removeFields(final JarWrapper wrapper) {
        // Get methods from every class & store the names in a list
        final List<String> fields = new ArrayList<>();
        wrapper.getClasses().forEach(classWrapper ->
                classWrapper.getFields()
                        .forEach(fieldNode -> {
                            // Path & name of method
                            final String fullName = classWrapper.getNode().name + "/" + fieldNode.name;

                            // make sure the method isn't in the exclusions list
                            if (exclusions.getOptions().contains(fullName)) return;

                            fields.add(fullName);
                        }));

        // Get all referenced fields
        final List<String> referencedFields = new ArrayList<>();
        wrapper.getClasses().forEach(classWrapper ->
                classWrapper.getMethods().stream()
                        .filter(methodWrapper -> !methodWrapper.isInitializer())
                        .forEach(methodWrapper ->
                        methodWrapper.getInstructions()
                                .filter(insn -> insn instanceof FieldInsnNode)
                                .map(insn -> (FieldInsnNode)insn)
                                .forEach(insn -> {
                                    //System.out.println(insn.owner + "/" + insn.name);
                                    referencedFields.add(insn.owner + "/" + insn.name);
                                })));

        // Loop through fields & check if it was referenced somewhere
        for (String m : fields) {
            if (!referencedFields.contains(m)) {
                final int lastSlashIdx = m.lastIndexOf("/");

                // Get class/owner from string
                final String owner = m.substring(0, lastSlashIdx);

                // Get method name
                final String field = m.substring(lastSlashIdx + 1);

                // Find & remove method
                getClasses(wrapper).forEach(classWrapper -> {
                    if (classWrapper.getNode().name.equals(owner))
                        classWrapper.getNode().fields.removeIf(fieldNode -> fieldNode.name.equals(field));
                });

                Ambien.LOGGER.info("Removed unused field: {}", m);
            }
        }
    }
}
