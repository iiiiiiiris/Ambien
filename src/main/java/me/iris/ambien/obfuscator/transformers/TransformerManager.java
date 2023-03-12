package me.iris.ambien.obfuscator.transformers;

import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.implementations.data.MathStackMangler;
import me.iris.ambien.obfuscator.transformers.implementations.data.NumberXor;
import me.iris.ambien.obfuscator.transformers.implementations.data.Shuffle;
import me.iris.ambien.obfuscator.transformers.implementations.data.StringEncryption;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.*;
import me.iris.ambien.obfuscator.transformers.implementations.flow.*;
import me.iris.ambien.obfuscator.transformers.implementations.miscellaneous.*;
import me.iris.ambien.obfuscator.transformers.implementations.optimization.*;
import me.iris.ambien.obfuscator.transformers.implementations.packaging.*;

import java.util.*;

public class TransformerManager {
    private final List<Transformer> transformers;

    public TransformerManager() {
        this.transformers = new ArrayList<>();

        this.transformers.addAll(Arrays.asList(
                // Control flow
                new JunkCode(),
                new FakeJumps(),

                // Data
                new NumberXor(),
                new StringEncryption(),
                new MathStackMangler(),
                new Shuffle(),

                // Exploits
                new Crasher(),
                new ModifyAccess(),

                // Packaging
                new FolderClasses(),
                new Comment(),
                new FakeClasses(),
                new AggressiveCompression(),
                new RedHerring(),

                // Optimization
                new RemoveBloatInstructions(),
                new RemoveDebugInfo(),

                // Miscellaneous
                new LocalVariableRenamer(),
                new LineNumberRandomizer()
        ));

        // Sort transformers by ordinal
        transformers.sort(Comparator.comparingInt(transformer -> -transformer.getOrdinal().getIdx()));
    }

    public List<Transformer> getTransformers() {
        return transformers;
    }

    public Transformer getTransformer(String name) {
        for (Transformer transformer : transformers) {
            if (transformer.getName().equals(name))
                return transformer;
        }

        return null;
    }
}
