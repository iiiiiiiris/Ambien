package me.iris.ambien.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Method will not have any transformers applied
 */
@Target(ElementType.METHOD)
public @interface IgnoreMethod { }
