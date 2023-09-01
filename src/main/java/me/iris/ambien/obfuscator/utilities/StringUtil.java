package me.iris.ambien.obfuscator.utilities;

import lombok.experimental.UtilityClass;
import me.iris.ambien.obfuscator.Ambien;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class StringUtil {
    private static final char[] CHARS = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm".toCharArray();
    private final List<String> ILLEGAL_JAVA_NAMES = List.of(
            "abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do",
            "double", "else", "enum", "extends",
            "false", "final", "finally", "float",
            "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface",
            "long", "native", "new", "null",
            "package", "private", "protected", "public",
            "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while"
    );

    public String randomString(final int len) {
        // Use current time in milliseconds as the seed to not repeat
        final Random insurace = new Random(System.nanoTime());
        final Random random = new Random(System.currentTimeMillis() + insurace.nextInt());

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(CHARS[random.nextInt(CHARS.length)]);
        }

        return builder.toString();
    }

    public String genName(final int len) {
        if(!Ambien.get.naming.equals("random")) {
            return StringUtil.getNewName(Ambien.get.naming, "");
        }
        return StringUtil.randomString(len);
    }

    public String randomIllegalJavaName() {
        return ILLEGAL_JAVA_NAMES.get(ThreadLocalRandom.current().nextInt(0, ILLEGAL_JAVA_NAMES.size()));
    }


    public String randomSpace() {
        return randomString(ThreadLocalRandom.current().nextInt(5,10),"\n\u3000\u2007");
    }

    public static String randomStringByStringList(int length, List<String> stringPool) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            stringBuilder.append(stringPool.get(ThreadLocalRandom.current().nextInt(0, stringPool.size())));
        }

        return stringBuilder.toString();
    }

    public String randomString(int length, String pool) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i <= length; i++) {
            stringBuilder.append(pool.charAt(ThreadLocalRandom.current().nextInt(0, pool.length())));
        }

        return stringBuilder.toString();
    }

    public static List<String> readDictionaryFromFile(String filePath) {
        List<String> dictionary = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public static List<String[]> readCopyPastes(String filePath) {
        List<String[]> copyPastes = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            List<String> currentAhegao = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (!currentAhegao.isEmpty()) {
                        copyPastes.add(currentAhegao.toArray(new String[0]));
                        currentAhegao.clear();
                    }
                } else {
                    currentAhegao.add(line);
                }
            }

            if (!currentAhegao.isEmpty()) {
                copyPastes.add(currentAhegao.toArray(new String[0]));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyPastes;
    }

    public static String getNewName(String mode, String p) {
        if (mode.equals("barcode")) {
            final int len = MathUtil.randomInt(10, 50);
            final StringBuilder name = new StringBuilder();
            for (int i = 0; i < len; i++)
                name.append(MathUtil.RANDOM.nextBoolean() ? "I" : "l");
            return p + name;
        }

        if (mode.endsWith(".txt")) {
            List<String> stringList = readDictionaryFromFile(mode);
            if (!stringList.isEmpty()) {
                int randomIndex = MathUtil.randomInt(0, stringList.size());
                return p + stringList.get(randomIndex);
            }
        }

        return p + StringUtil.randomString(MathUtil.randomInt(10, 50));
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

    public boolean containsNonAlphabeticalChars(final String str) {
        final List<Character> charList = IntStream
                .range(0, CHARS.length)
                .mapToObj(c -> CHARS[c])
                .toList();
        for (char c : str.toCharArray()) {
            if (charList.contains(c))
                return true;
        }

        return false;
    }
}
