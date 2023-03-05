package com.navercorp.scavenger.javaagent.collecting;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isGetter;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.isPackagePrivate;
import static net.bytebuddy.matcher.ElementMatchers.isPrivate;
import static net.bytebuddy.matcher.ElementMatchers.isProtected;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isSetter;
import static net.bytebuddy.matcher.ElementMatchers.isSynthetic;
import static net.bytebuddy.matcher.ElementMatchers.isToString;
import static net.bytebuddy.matcher.ElementMatchers.nameMatches;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotationSource;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import lombok.RequiredArgsConstructor;

import com.navercorp.scavenger.javaagent.model.Config;

@RequiredArgsConstructor
public class ElementMatcherBuilder {
    private final Config config;

    public ElementMatcher<TypeDescription> buildClassMatcher() {
        ElementMatcher.Junction<NamedElement> packageNameMatcher = config.getPackagesWithEndingDot().stream()
            .map(ElementMatchers::nameStartsWith)
            .reduce(ElementMatcher.Junction::or)
            .orElse(none());

        ElementMatcher.Junction<NamedElement> excludePackageMatcher = config.getExcludePackagesWithEndingDot().stream()
            .map(ElementMatchers::nameStartsWith)
            .reduce(ElementMatcher.Junction::or)
            .orElse(none());

        ElementMatcher.Junction<AnnotationSource> annotationMatcher = config.getAnnotations().stream()
            .map(ElementMatchers::named)
            .map(ElementMatchers::isAnnotatedWith)
            .reduce(ElementMatcher.Junction::or)
            .orElse(any());

        ElementMatcher.Junction<NamedElement> additionalPackageMatcher = config.getAdditionalPackagesWithEndingDot().stream()
            .map(ElementMatchers::nameStartsWith)
            .reduce(ElementMatcher.Junction::or)
            .orElse(none());

        return packageNameMatcher
            .and(not(isSynthetic()))
            .and(not(isInterface()))
            .and(not(excludePackageMatcher))
            .and(annotationMatcher.or(additionalPackageMatcher));
    }

    public ElementMatcher<MethodDescription> buildMethodMatcher(TypeDescription typeDescription) {
        ElementMatcher.Junction<MethodDescription> matcher = any();

        if (config.isExcludeConstructors()) {
            matcher = matcher.and(not(isConstructor()));
        }

        if (config.isExcludeGetterSetter()) {
            ElementMatcher.Junction<MethodDescription> kotlinComponent
                = takesNoArguments().and(not(returns(TypeDescription.ForLoadedType.of(void.class)))).and(nameMatches("component\\d+"));

            matcher = matcher.and(not(
                isGetter().or(isSetter()).or(kotlinComponent)
            ));
        }

        ElementMatcher.Junction<MethodDescription> visibilityMatcher = none();
        switch (config.getMethodVisibility()) {
            case PRIVATE:
                visibilityMatcher = visibilityMatcher.or(isPrivate());
            case PACKAGE_PRIVATE:
                visibilityMatcher = visibilityMatcher.or(isPackagePrivate());
            case PROTECTED:
                visibilityMatcher = visibilityMatcher.or(isProtected());
            case PUBLIC:
                visibilityMatcher = visibilityMatcher.or(isPublic());
        }

        ElementMatcher.Junction<MethodDescription> declarationMatcher = isDeclaredBy(typeDescription);
        ElementMatcher.Junction<MethodDescription> trivialMethodMatchers = not(isToString().or(isSynthetic()));

        return matcher
            .and(declarationMatcher)
            .and(visibilityMatcher)
            .and(trivialMethodMatchers);
    }
}
