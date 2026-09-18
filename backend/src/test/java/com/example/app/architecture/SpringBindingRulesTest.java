package com.example.app.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * "Name Spring binding annotations explicitly." (docs/CONVENTIONS.md)
 */
@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class SpringBindingRulesTest {

	private static final Set<String> BINDING_ANNOTATIONS = Set.of(PathVariable.class.getName(),
			RequestParam.class.getName(), Param.class.getName());

	@ArchTest
	static final ArchRule springBindingAnnotationsDeclareName = methods().should(haveNamedSpringBindingAnnotations())
			.because("Spring MVC and Spring Data parameter binding should not depend on reflected parameter names");

	private static ArchCondition<JavaMethod> haveNamedSpringBindingAnnotations() {
		return new ArchCondition<>("have explicit names on Spring binding annotations") {
			@Override
			public void check(JavaMethod method, ConditionEvents events) {
				for (JavaParameter parameter : method.getParameters()) {
					for (JavaAnnotation<JavaParameter> annotation : parameter.getAnnotations()) {
						if (BINDING_ANNOTATIONS.contains(annotation.getRawType().getFullName())
								&& !declaresBindingName(annotation)) {
							String message = method.getFullName() + " parameter " + parameter.getIndex()
									+ " uses @" + annotation.getRawType().getSimpleName()
									+ " without an explicit value/name";
							events.add(SimpleConditionEvent.violated(method, message));
						}
					}
				}
			}
		};
	}

	private static boolean declaresBindingName(JavaAnnotation<JavaParameter> annotation) {
		return declaredString(annotation, "value").map(value -> !value.isBlank()).orElse(false)
				|| declaredString(annotation, "name").map(value -> !value.isBlank()).orElse(false);
	}

	private static Optional<String> declaredString(JavaAnnotation<JavaParameter> annotation, String propertyName) {
		return annotation.tryGetExplicitlyDeclaredProperty(propertyName)
				.filter(String.class::isInstance)
				.map(String.class::cast);
	}
}
