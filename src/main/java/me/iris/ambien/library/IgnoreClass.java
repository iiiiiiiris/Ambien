package me.iris.ambien.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Class will not have any transformers applied
 */
@Target(ElementType.TYPE)
public @interface IgnoreClass { }
