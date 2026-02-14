package com.example.kihan.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class NamingConventionTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.kihan");

    @Test
    void Controller_클래스는_Controller로_끝나야_한다() {
        classes()
                .that().areAnnotatedWith(Controller.class)
                .or().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void Service_어노테이션은_Service로_끝나는_클래스에만_사용해야_한다() {
        classes()
                .that().areAnnotatedWith(Service.class)
                .should().haveSimpleNameEndingWith("Service")
                .orShould().haveSimpleNameEndingWith("Facade")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void Controller_클래스는_presentation_패키지에_있어야_한다() {
        classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..presentation..")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void Service로_끝나는_클래스는_application_패키지에_있어야_한다() {
        classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..application..")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

}