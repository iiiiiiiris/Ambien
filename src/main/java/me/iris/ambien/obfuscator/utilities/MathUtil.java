package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@UtilityClass
public class MathUtil {
    public static final Random RANDOM = new Random();

    public int randomInt() {
        return RANDOM.nextInt();
    }

//    public int randomInt(final int min, final int max) {
//        return min + (RANDOM.nextInt(max));
//    }

    public int randomInt(final int min, final int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    public int[] getTwoRandomInts(final int min, final int max) {
        final int[] ints = new int[2];
        ints[0] = randomInt(min, max);

        int secondInt = randomInt(min, max);
        if (ints[0] == secondInt) {
            while (ints[0] == secondInt) {
                secondInt = randomInt(min, max);
            }
        }

        ints[1] = secondInt;
        return ints;
    }
}
