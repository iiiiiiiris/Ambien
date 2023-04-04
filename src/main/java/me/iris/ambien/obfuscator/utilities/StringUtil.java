package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class StringUtil {
    private static final char[] CHARS = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm".toCharArray();

    public String randomString(final int len) {
        // Use current time in milliseconds as the seed to not repeat
        final Random random = new Random(System.currentTimeMillis());

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(CHARS[random.nextInt(CHARS.length)]);
        }

        return builder.toString();
    }

    public String repeat(final char c, final int len) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++)
            builder.append(c);
        return builder.toString();
    }

    public String build(final String[] strs) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            builder.append(strs[i]);
            if (i != strs.length - 1)
                builder.append(' ');
        }

        return builder.toString();
    }
}
