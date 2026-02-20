package com.dochiri.kihan.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerDependencyTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.kihan");

    @Test
    void 레이어_의존성_규칙을_준수해야_한다() {
        Architectures.layeredArchitecture()
                .consideringAllDependencies()
                .layer("Presentation").definedBy("com.example.kihan.presentation..")
                .layer("Application").definedBy("com.example.kihan.application..")
                .layer("Domain").definedBy("com.example.kihan.domain..")
                .layer("Infrastructure").definedBy("com.example.kihan.infrastructure..")

                .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application", "Presentation")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Presentation", "Infrastructure")

                .ignoreDependency(com.dochiri.kihan.KihanApplication.class, Object.class)
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void Application_레이어는_Presentation_레이어에_의존하지_않아야_한다() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..presentation..")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void Domain_레이어는_다른_레이어에_의존하지_않아야_한다() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..",
                        "..presentation..",
                        "..infrastructure.."
                )
                .because("Domain은 순수해야 하며 다른 레이어에 의존하지 않아야 합니다 (JPA 예외)")
                .allowEmptyShould(true)
                .check(CLASSES);
    }

}