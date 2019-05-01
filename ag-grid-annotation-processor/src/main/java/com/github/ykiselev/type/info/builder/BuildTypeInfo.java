package com.github.ykiselev.type.info.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 01.05.2019
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface BuildTypeInfo {
}
