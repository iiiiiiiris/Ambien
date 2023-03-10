package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    private static final char[] CHARS = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm".toCharArray();

    public String randomString(final int len) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++)
            builder.append(CHARS[MathUtil.randomInt(0, CHARS.length)]);
        return builder.toString();
    }

    public String repeat(final char c, final int len) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++)
            builder.append(c);
        return builder.toString();
    }
}
