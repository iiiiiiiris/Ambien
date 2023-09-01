package me.iris.ambien.obfuscator.transformers;

import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.implementations.data.*;
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
                new UselessInstructions(),
                new FakeJumps(),
                new JunkCode(),
                new GotoShenanigans(),
                new MyJ2CFlow(),
                new ReferenceHider(),
                new CaesiumReference(),
                new Polymorph(),

                // Data
                new NumberObfuscation(),
                new StringEncryption(),
                new SouvenirStringEncryption(),
                new ColonialStringEncryption(),
                new Shuffle(),
                new XorBooleans(),

                // Exploits
                new Crasher(),
                new DuplicateResources(),
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
                new RemoveUnused(),

                // Miscellaneous
                new LineNumberRandomizer(),
                new ArgumentChecker(),
                new Ahegao(),
                new Metadata(),
                new Remapper()
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
