package me.iris.ambien.obfuscator.transformers.implementations.data;

import me.iris.ambien.obfuscator.settings.data.implementations.ListSetting;
import me.iris.ambien.obfuscator.souvenir.CaesarEncryption;
import me.iris.ambien.obfuscator.souvenir.IStringEncryptionMethod;
import me.iris.ambien.obfuscator.souvenir.XorEncryption;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@TransformerInfo(
        name = "souvenir-string-encryption",
        category = Category.DATA,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW,
        description = "asdasd."
)
public class SouvenirStringEncryption extends Transformer {
    public final ListSetting stringBlacklist = new ListSetting("string-blacklist", new ArrayList<>());
    private final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();
    public List<IStringEncryptionMethod> methods = Arrays.asList(new CaesarEncryption(), new XorEncryption());
    SecureRandom r = new SecureRandom();

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper)
                .forEach(classWrapper -> {
                    List<MethodWrapper> methods = classWrapper.getTransformableMethods().stream()
                            .filter(MethodWrapper::hasInstructions)
                            .collect(Collectors.toList());
                    classMethodsMap.put(classWrapper, methods);
                });

        classMethodsMap.forEach((classWrapper, methods) ->
                        run(classWrapper.getNode())
                );
    }

    public void run(ClassNode cn) {
        if((cn.access & ACC_INTERFACE) == ACC_INTERFACE) {
            return;
        }
        AtomicBoolean has = new AtomicBoolean(false);
        IStringEncryptionMethod encryption = methods.get(r.nextInt(methods.size()));
        MethodNode decryptMethod = encryption.createDecrypt(String.valueOf(r.nextInt(1000)));
        cn.methods.forEach(mn -> {
            Arrays.stream(mn.instructions.toArray()).forEach(insn -> {
                if(insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode)insn).cst instanceof String && !stringBlacklist.getOptions().contains((String)((LdcInsnNode)insn).cst)){
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if(ldc.cst instanceof String){
                        int decryptValue = r.nextInt(30) + 6;
                        has.set(true);
                        ldc.cst = encryption.encrypt((String)ldc.cst, decryptValue);
                        mn.instructions.insert(ldc, new MethodInsnNode(Opcodes.INVOKESTATIC, cn.name, decryptMethod.name, "(Ljava/lang/String;I)Ljava/lang/String;", false));
                        mn.instructions.insert(ldc, new IntInsnNode(BIPUSH, decryptValue));

                    }
                }
            });
        });

        if(has.get()){
            cn.methods.add(decryptMethod);
        }
    }
}
