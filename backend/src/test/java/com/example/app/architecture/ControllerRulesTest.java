package com.example.app.architecture;

import static com.example.app.architecture.ArchPredicates.ARE_CONTROLLERS;
import static com.example.app.architecture.ArchPredicates.ARE_JPA_ENTITIES;
import static com.example.app.architecture.ArchPredicates.ARE_REPOSITORIES;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * "Controllers call services, not repositories." / "Controllers return
 * request/response models, not JPA entities." (docs/CONVENTIONS.md)
 */
@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class ControllerRulesTest {

	@ArchTest
	static final ArchRule controllersDoNotDependOnRepositories = noClasses().that(ARE_CONTROLLERS)
			.should()
			.dependOnClassesThat(ARE_REPOSITORIES)
			.because("controllers must call services, not repositories directly");

	@ArchTest
	static final ArchRule controllersDoNotDependOnEntities = noClasses().that(ARE_CONTROLLERS)
			.should()
			.dependOnClassesThat(ARE_JPA_ENTITIES)
			.because("controllers must expose request/response models, never JPA entities");
}
