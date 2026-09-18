package com.example.app.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * "No nested/inner classes, records, or enums; each type gets its own
 * top-level file." (docs/CONVENTIONS.md)
 */
@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class StructureRulesTest {

	@ArchTest
	static final ArchRule noNestedTypes = noClasses().should(beNestedTypes())
			.because("each type gets its own top-level file (docs/CONVENTIONS.md)");

	private static ArchCondition<JavaClass> beNestedTypes() {
		return new ArchCondition<>("be a nested class, record, or enum") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				boolean nested = javaClass.isNestedClass();
				String message = javaClass.getFullName()
						+ " is a nested type; give it its own top-level file instead";
				events.add(new SimpleConditionEvent(javaClass, nested, message));
			}
		};
	}
}
