package me.iris.ambien.obfuscator.transformers;

import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.implementations.exploits.*;
import me.iris.ambien.obfuscator.transformers.implementations.flow.*;
import me.iris.ambien.obfuscator.transformers.implementations.miscellaneous.*;
import me.iris.ambien.obfuscator.transformers.implementations.optimization.*;
import me.iris.ambien.obfuscator.transformers.implementations.packaging.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformerManager {
    private final List<Transformer> transformers;

    public TransformerManager() {
        this.transformers = new ArrayList<>();

        this.transformers.addAll(Arrays.asList(
                // Control flow
                new JunkCode(),
                new Shuffle(),
                new LineNumberRandomizer(),
                new FakeJumps(),
                new NumberXor(),

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
                new LocalVariableRenamer()
        ));
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
