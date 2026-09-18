package com.example.app.architecture;

import static com.example.app.architecture.ArchPredicates.ARE_CONTROLLERS;
import static com.example.app.architecture.ArchPredicates.ARE_REPOSITORIES;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * "Service-layer transactions." (docs/CONVENTIONS.md) - keep
 * {@code @Transactional} off controllers and repositories so services stay
 * the one place transaction boundaries are decided.
 */
@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class TransactionRulesTest {

	@ArchTest
	static final ArchRule onlyServicesDeclareTransactionBoundaries = noClasses().that(ARE_CONTROLLERS.or(ARE_REPOSITORIES))
			.should()
			.beAnnotatedWith(Transactional.class)
			.because("services own transactions; controllers and repositories should not declare their own");
}
