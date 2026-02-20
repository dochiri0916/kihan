package com.dochiri.kihan.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Modifier;
import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class CodingConventionTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.kihan");

    @Test
    void API_URI는_api_도메인복수형_패턴이어야_한다() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .and().areAnnotatedWith(RequestMapping.class)
                .should(haveApiUriPattern())
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    private static ArchCondition<JavaClass> haveApiUriPattern() {
        return new ArchCondition<>("have API URI pattern /api/{domain-plural}/") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                Optional<String[]> paths = javaClass.tryGetAnnotationOfType(RequestMapping.class)
                        .map(RequestMapping::value);

                if (paths.isPresent() && paths.get().length > 0) {
                    String path = paths.get()[0];
                    if (!path.matches("^/api/[a-z]+s(/.*)?$") && !path.matches("^/api/[a-z]+-[a-z]+s(/.*)?$")) {
                        String message = String.format(
                                "Controller %s has RequestMapping '%s' which doesn't follow /api/{domain-plural}/ pattern",
                                javaClass.getSimpleName(),
                                path
                        );
                        events.add(SimpleConditionEvent.violated(javaClass, message));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaMethod> haveOnlyFinalParameters() {
        return new ArchCondition<>("have only final parameters") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                try {
                    java.lang.reflect.Method reflectMethod = method.reflect();
                    java.lang.reflect.Parameter[] parameters = reflectMethod.getParameters();

                    for (java.lang.reflect.Parameter parameter : parameters) {
                        if (!Modifier.isFinal(parameter.getModifiers())) {
                            String message = String.format(
                                    "Parameter '%s' in method %s.%s() is not final",
                                    parameter.getName(),
                                    method.getOwner().getSimpleName(),
                                    method.getName()
                            );
                            events.add(SimpleConditionEvent.violated(method, message));
                        }
                    }
                } catch (Exception e) {
                    // Skip if reflection fails
                }
            }
        };
    }

}