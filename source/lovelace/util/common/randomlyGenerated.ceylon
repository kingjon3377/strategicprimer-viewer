import ceylon.test.engine.spi {
    ArgumentProviderContext,
    ArgumentProvider
}
import ceylon.language.meta.declaration {
    OpenIntersection,
    OpenUnion,
    nothingType,
    ValueDeclaration,
    OpenTypeVariable,
    OpenClassOrInterfaceType,
    OpenType
}
import ceylon.whole {
    Whole,
    wholeNumber
}

"Annotation to make a parameterized test randomly generate numbers. Apply to a parameter
 of a method annotated with [[ceylon.test::test]], and the test will be run [[count]] times
 (with the same values for all other parameters, as usual) with randomly generated numbers
 between 0 and [[max]] passed to the annotated parameter. Can currently generate
 [[integers|Integer]] and [[floats|Float]]."
shared annotation RandomGenerationAnnotation randomlyGenerated(
        "How many different numbers to generate."
        Integer count,
        "The upper bound of the range from which to select random numbers."
        Integer max = /*runtime.maxArraySize*/ 2147483639)
        => RandomGenerationAnnotation(count, max);

"The annotation class for the [[randomlyGenerated]] annotation."
shared final annotation class RandomGenerationAnnotation(
                "How many different numbers to generate."
                Integer count,
                "The upper bound of the range from which to select random numbers."
                Integer max)
        satisfies OptionalAnnotation<RandomGenerationAnnotation,ValueDeclaration>
        & ArgumentProvider {
    object nothingProvider satisfies Iterator<Anything> { // TODO: Should be static
        suppressWarnings("expressionTypeNothing")
        shared actual Anything next() => nothing;
    }
    {Anything*} argumentsForType(OpenType type) {
        switch (type)
        case (is OpenClassOrInterfaceType) {
            value declaredType = type.declaration;
            if (declaredType == `class Integer`) {
                return singletonRandom.integers(max).take(count);
            } else if (declaredType == `class Float`) {
                return singletonRandom.floats().map(max.float.times).take(count);
            } else if (declaredType == `interface Whole`) {
                return singletonRandom.integers(max).map(wholeNumber).take(count);
            } else { // TODO: Make a 'native' for Decimal.
                throw AssertionError("Can't randomly generate ``declaredType`` yet");
            }
        }
        case (is OpenUnion) {
            for (innerType in type.caseTypes) { // TODO: Extract helper method: this idiom occurs twice in this overlong class.
                try {
                    return argumentsForType(innerType);
                } catch (AssertionError error) {
                    // ignore
                }
            }
            throw AssertionError(
                "Can't randomly generate values for a union of types none of which we handle");
        }
        case (is OpenIntersection) {
            for (innerType in type.satisfiedTypes) {
                try {
                    return argumentsForType(innerType);
                } catch (AssertionError error) {
                    // ignore
                }
            }
            throw AssertionError(
                "Can't randomly generate values for an intersection of types we don't handle");
        }
        case (is OpenTypeVariable) {
            throw AssertionError(
                "No idea how to randomly generate values for a TypeVariable type");
        }
        case (nothingType) {
            return IteratorWrapper(nothingProvider);
        }
    }
    shared actual {Anything*} arguments(ArgumentProviderContext context) {
        assert (exists declaration = context.parameterDeclaration);
        return argumentsForType(declaration.openType);
    }
}
