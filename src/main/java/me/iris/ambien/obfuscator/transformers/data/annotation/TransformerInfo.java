package me.iris.ambien.obfuscator.transformers.data.annotation;

import me.iris.ambien.obfuscator.transformers.data.Category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TransformerInfo {
    String name();
    Category category();
    boolean enabledByDefault() default false;
}
