package com.github.ykiselev.type.info.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 01.05.2019
 */
@SupportedAnnotationTypes({"com.github.ykiselev.type.info.builder.BuildTypeInfo"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class TypeInfoProcessor extends AbstractProcessor {

    private static final String TARGET_CLASS_POSTFIX = "TypeInfoFactory";

    private static final ClassName OBJECT_ATTRIBUTE = ClassName.get("com.github.ykiselev.ag.grid.data.types", "ObjectAttribute");

    private static final ClassName INT_ATTRIBUTE = ClassName.get("com.github.ykiselev.ag.grid.data.types", "IntAttribute");

    private static final ClassName LONG_ATTRIBUTE = ClassName.get("com.github.ykiselev.ag.grid.data.types", "LongAttribute");

    private static final ClassName DOUBLE_ATTRIBUTE = ClassName.get("com.github.ykiselev.ag.grid.data.types", "DoubleAttribute");

    private static final ClassName TYPE_INFO = ClassName.get("com.github.ykiselev.ag.grid.data.types", "TypeInfo");

    private static final ClassName DEFAULT_TYPE_INFO = ClassName.get("com.github.ykiselev.ag.grid.data.types", "DefaultTypeInfo");

    private static final Set<String> IGNORED_METHODS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "toString",
                    "hashCode"
            ))
    );

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean result = false;
        for (TypeElement annotation : annotations) {
            if (!annotation.getQualifiedName().contentEquals("com.github.ykiselev.type.info.builder.BuildTypeInfo")) {
                continue;
            }
            result = true;
            roundEnv.getElementsAnnotatedWith(annotation)
                    .stream()
                    .filter(e -> e instanceof TypeElement)
                    .map(TypeElement.class::cast)
                    .forEach(this::process);
        }

        return result;
    }

    private void process(TypeElement typeElement) {
        final PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
        final String targetClassSimpleName = typeElement.getSimpleName() + TARGET_CLASS_POSTFIX;
        try {
            final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(typeElement.getQualifiedName() + TARGET_CLASS_POSTFIX);
            try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
                final String packageName = packageElement.getQualifiedName().toString();
                final JavaFile javaFile = JavaFile.builder(packageName, generateTypeInfo(typeElement, targetClassSimpleName))
                        .indent("    ")
                        .build();
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }

    private TypeSpec generateTypeInfo(TypeElement typeElement, String className) {
        final ClassName originalClassName = ClassName.get(typeElement);
        final ParameterizedTypeName typeInfo = ParameterizedTypeName.get(TYPE_INFO, originalClassName);

        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(typeInfo);

        final CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("return new $T<>($T.asList(", DEFAULT_TYPE_INFO, ClassName.get(Arrays.class));

        final Predicate<ExecutableElement> filter = e -> {
            final Set<Modifier> modifiers = e.getModifiers();
            // should be public and not static
            if (modifiers.contains(Modifier.STATIC) || !modifiers.contains(Modifier.PUBLIC)) {
                return false;
            }
            // should have non-void return type
            if (e.getReturnType().getKind() == TypeKind.VOID) {
                return false;
            }
            // should not have arguments
            if (!e.getParameters().isEmpty()) {
                return false;
            }
            return !IGNORED_METHODS.contains(e.getSimpleName().toString());
        };
        final Iterator<ExecutableElement> it = typeElement.getEnclosedElements()
                .stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(ExecutableElement.class::cast)
                .filter(filter).iterator();
        boolean first = true;
        while (it.hasNext()) {
            if (!first) {
                codeBlock.add(",");
            }
            codeBlock.add("\n");
            first = false;
            final ExecutableElement element = it.next();
            final TypeMirror type = element.getReturnType();
            final String propertyName = propertyName(element.getSimpleName().toString());
            switch (type.getKind()) {
                case INT:
                    codeBlock.add("new $T<>(\"$L\", $T::$L)", INT_ATTRIBUTE, propertyName, originalClassName, element.getSimpleName());
                    break;

                case LONG:
                    codeBlock.add("new $T<>(\"$L\", $T::$L)", LONG_ATTRIBUTE, propertyName, originalClassName, element.getSimpleName());
                    break;

                case DOUBLE:
                    codeBlock.add("new $T<>(\"$L\", $T::$L)", DOUBLE_ATTRIBUTE, propertyName, originalClassName, element.getSimpleName());
                    break;

                default:
                    codeBlock.add("new $T<>(\"$L\", $T.class, $T::$L)", OBJECT_ATTRIBUTE, propertyName, ClassName.get(element.getReturnType()), originalClassName, element.getSimpleName());
            }
        }

        codeBlock.add("\n))");

        final TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(methodBuilder.addStatement(codeBlock.build()).build());

        return builder.build();
    }

    private String propertyName(String getterName) {
        String result = getterName;
        if (result.startsWith("get")) {
            result = result.substring(3);
        } else if (result.startsWith("is")) {
            result = result.substring(2);
        }
        final char first = result.charAt(0);
        if (Character.isUpperCase(first)) {
            result = Character.toLowerCase(first) + result.substring(1);
        }
        return result;
    }
}