package com.example.app.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

/**
 * "Constructor injection only." (docs/CONVENTIONS.md)
 */
@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class InjectionRulesTest {

	@ArchTest
	static final ArchRule constructorInjectionOnly = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
}
