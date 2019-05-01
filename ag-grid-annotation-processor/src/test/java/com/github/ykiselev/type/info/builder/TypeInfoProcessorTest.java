package com.github.ykiselev.type.info.builder;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 01.05.2019
 */
public class TypeInfoProcessorTest {

    @Test
    public void shouldProcess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new TypeInfoProcessor())
                .compile(JavaFileObjects.forResource("Item.java"));
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("com/github/ykiselev/type/info/builder/ItemTypeInfoFactory");
    }
}