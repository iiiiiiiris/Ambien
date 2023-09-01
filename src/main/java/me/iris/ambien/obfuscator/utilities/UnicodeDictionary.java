package me.iris.ambien.obfuscator.utilities;

import java.util.ArrayList;
import java.util.List;

public class UnicodeDictionary {
    public static List<String> arabic = new ArrayList<>();
    public static List<String> unicode = new ArrayList<>();

    static {
        for (int i = 0x060C; i <= 0x06FE; i++) { // Арабский текст
            arabic.add(Character.toString((char) i));
        }

        unicode.addAll(
                List.of(
                        "\u034C",
                        "\u035C",
                        "\u034E",
                        "\u0344",
                        "\u0306",
                        "\u0307",
                        "\u0321",
                        "\u0331"
                )
        );
    }
}
