# Ambien

# Features
|   Feature   | Implemented |
| ----------- | ----------- |
| String encryption       | partial |
| Flow obfuscation        | partial |
| Number obfuscation      | partial |
| Invoke dynamics         | 🚫 |
| Crashers                | ✅ |
| Miscellaneous ZIP stuff | ✅ |
| GUI                     | 🚫 |
###### As of 1.2.1-beta release
###### Unimplemented features will be added at some point

# Usage
Download the latest jar [here](https://github.com/iiiiiiiris/Ambien/releases/latest)

Run the jar with no args (it will generate a default config for you to edit)

Run the Ambien jar with the path to your settings file as the first & only argument

**Note to developers: to see debug output, add `-Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG` to vm options**

# Media

## Original

``` java
    public static void main(String[] args) {
        final int someVar = 78;
        System.out.println(someVar);

        System.out.println("Yellow");

        for (int i = 0; i < 15; i++) {
            System.out.println(i);
        }

        try {
            System.out.println("uhmm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```


Full test jar source code can be found [here](./src/test/java/me/iris/testjar)

## Obfuscated

```java
    public static void main(String[] JTRM8tK85WLaQKce2BhjxOian8oF4nUIaYHdYdVVaBKsJ) {
        int var10000 = -67 ^ 94;
        int DAJCpTp5SkwmMUnzcp7ML2EBR1KJqL08ZBbp = ~(-((-67 & ~94) + 94 - (-67 & 94) ^ -83)) + 1;
        int var10001 = -58 ^ 5;
        System.out.println(~(-((-58 & ~5) + 5 - (-58 & 5) ^ -115)) + 1);
        byte[] var4 = new byte[]{107, 43, 112, 52, 56, 43, 112, 52, 47, 43, 112, 52, 47, 43, 112, 52, 50, 43, 112, 52, 75, 43, 112, 52};
        System.out.println(ONLxZK6w5auwOq2E5M8BYv6tvvfI4RGEWEkYNd4v(new String(A9WMJkzuXNvyaJqOloqmHA23wDwWgYf2wba24r24aGzFIbN4(var4)), 4WroerTItw1TdLAaZyEQi2weKq, 3518));
        int a0UUHPNFAWmwc27ys5ST1bJZrHm9zzlH7sKf7t6Alczoo90 = 0;
 
        while(true) {
            var10001 = 76 ^ -6;
            if (a0UUHPNFAWmwc27ys5ST1bJZrHm9zzlH7sKf7t6Alczoo90 >= ~(-((76 & ~-6) + -6 - (76 & -6) ^ -71)) + 1) {
                return;
            }
 
            System.out.println(a0UUHPNFAWmwc27ys5ST1bJZrHm9zzlH7sKf7t6Alczoo90);
            ++a0UUHPNFAWmwc27ys5ST1bJZrHm9zzlH7sKf7t6Alczoo90;
        }
    }
```

Full class decompilation can be seen [here](https://vip.ci/?947fc08f7460787b#EShMNTBW1CXanQopLTqXSFavASZDyE4dkn5KdnfPPoam)

Transformer settings can be seen [here](https://vip.ci/?ffe80453b066b624#7Ap2R4ijWKupHNiNHqAhScaRSt11Zx5xKsPHWeEGXCwM)

Crasher

![cfr](./web/media/crasher-cfr.png)

![fernflower](./web/media/crasher-fernflower.png)

![procyon](./web/media/crasher-procyon.png)

###### As of 1.2.1-beta release

# Dependencies
###### [Lombok](https://projectlombok.org/)

###### [ASM](https://asm.ow2.io/)

###### [SLF4J](https://www.slf4j.org/)

###### [GSON](https://github.com/google/gson)

###### [JCommander](https://github.com/cbeust/jcommander)

# Credits
###### [Recaf](https://github.com/Col-E/Recaf)

###### [jd-gui](https://github.com/java-decompiler/jd-gui)

###### [whoever wrote this](https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions)
