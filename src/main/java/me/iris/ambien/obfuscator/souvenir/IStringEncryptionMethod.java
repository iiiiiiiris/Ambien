package me.iris.ambien.obfuscator.souvenir;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public interface IStringEncryptionMethod extends Opcodes {

    String encrypt(String v, int key);
    String decrypt(String v, int key);
    MethodNode createDecrypt(String name);

}
