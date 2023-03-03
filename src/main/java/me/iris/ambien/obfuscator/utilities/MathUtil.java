package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class MathUtil {
    private static final Random RANDOM = new Random();

    public int randomInt() {
        return RANDOM.nextInt();
    }

    public int randomInt(final int min, final int max) {
        return min + (RANDOM.nextInt(max));
    }
}
