package com.example.kihan.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

class ApiConventionTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.kihan");

    @Test
    void RestController는_api_경로로_시작해야_한다() {
        classes()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .and().areAnnotatedWith(RequestMapping.class)
                .should().beAnnotatedWith(RequestMapping.class)
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void 필드_주입을_사용하지_않아야_한다() {
        NO_CLASSES_SHOULD_USE_FIELD_INJECTION
                .allowEmptyShould(true)
                .check(CLASSES);
    }

}