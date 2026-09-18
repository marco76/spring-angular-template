package com.example.app.architecture;

import static com.example.app.architecture.ArchPredicates.ARE_JPA_ENTITIES;
import static com.example.app.architecture.ArchPredicates.ARE_REPOSITORIES;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * "Repositories and JPA entities live under feature/db/." / "Enums live
 * under feature/types/." (docs/CONVENTIONS.md)
 */
@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class PersistenceRulesTest {

	// allowEmptyShould: this template ships with no persistence layer yet, so
	// the rule has nothing to check until the first feature adds one - it
	// still fires the moment a matching class shows up anywhere else.
	@ArchTest
	static final ArchRule repositoriesAndEntitiesLiveUnderDb = classes().that(ARE_REPOSITORIES.or(ARE_JPA_ENTITIES))
			.should()
			.resideInAPackage("..db..")
			.because("repositories and entities are persistence details, isolated under feature/db/")
			.allowEmptyShould(true);

	// allowEmptyShould: no enums exist in the template yet; same reasoning as
	// above.
	@ArchTest
	static final ArchRule enumsLiveUnderTypes = classes().that()
			.areEnums()
			.should()
			.resideInAPackage("..types..")
			.because("feature enums live under feature/types/")
			.allowEmptyShould(true);
}
