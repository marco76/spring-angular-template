package com.example.app.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.Entity;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

/**
 * Class predicates shared across the {@code architecture} package's rule
 * classes, so each rule class can stay focused on one concern (see
 * {@link ControllerRulesTest}, {@link PersistenceRulesTest},
 * {@link TransactionRulesTest}) without redefining what counts as a
 * controller/repository/entity.
 *
 * Classes are matched by their actual Spring/JPA role (annotation, or for
 * Spring Data repository interfaces, assignability to {@link Repository}),
 * not by naming convention alone - a class named {@code FeatureController}
 * that forgot {@code @RestController}, or one annotated
 * {@code @RestController} but not named {@code *Controller}, are both still
 * caught.
 */
final class ArchPredicates {

	static final DescribedPredicate<JavaClass> ARE_CONTROLLERS = simpleNameEndingWith("Controller")
			.or(annotatedWith(RestController.class))
			.or(annotatedWith(Controller.class))
			.as("are controllers");

	// Spring Data repository interfaces (JpaRepository, CrudRepository, ...)
	// all extend the org.springframework.data.repository.Repository marker,
	// so assignableTo catches them without relying on a *Repository name or
	// an explicit @Repository (which Spring Data interfaces don't declare).
	static final DescribedPredicate<JavaClass> ARE_REPOSITORIES = simpleNameEndingWith("Repository")
			.or(assignableTo(Repository.class))
			.as("are repositories");

	static final DescribedPredicate<JavaClass> ARE_JPA_ENTITIES = simpleNameEndingWith("Entity")
			.or(annotatedWith(Entity.class))
			.as("are JPA entities");

	private ArchPredicates() {
	}
}
