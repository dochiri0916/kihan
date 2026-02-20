package com.dochiri.kihan.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class DomainModelTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.kihan");

    @Test
    void Entity_클래스명은_단수형이어야_한다() {
        classes()
                .that().areAnnotatedWith(Entity.class)
                .should(notEndWithCommonPluralSuffixes())
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    @Test
    void Table_이름은_복수형이어야_한다() {
        classes()
                .that().areAnnotatedWith(Entity.class)
                .and().areAnnotatedWith(Table.class)
                .should(haveTableNameEndingWithS())
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    private static ArchCondition<JavaClass> notEndWithCommonPluralSuffixes() {
        return new ArchCondition<>("not have plural class names") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String simpleName = javaClass.getSimpleName();
                if (simpleName.endsWith("s") && !simpleName.endsWith("Status") && !simpleName.endsWith("Address")) {
                    String message = String.format(
                            "Entity class %s appears to be plural (ends with 's'). Entity names should be singular.",
                            javaClass.getFullName()
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> haveTableNameEndingWithS() {
        return new ArchCondition<>("have plural table names") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                Optional<String> tableName = javaClass.tryGetAnnotationOfType(Table.class)
                        .map(Table::name);

                if (tableName.isPresent() && !tableName.get().isEmpty()) {
                    String name = tableName.get();
                    if (!name.endsWith("s")) {
                        String message = String.format(
                                "Table name '%s' for entity %s should be plural (should end with 's')",
                                name,
                                javaClass.getSimpleName()
                        );
                        events.add(SimpleConditionEvent.violated(javaClass, message));
                    }
                }
            }
        };
    }

}
