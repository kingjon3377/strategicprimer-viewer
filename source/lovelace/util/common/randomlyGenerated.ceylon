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
"An annotation to make a parameterized test randomly generate numbers."
shared annotation RandomGenerationAnnotation randomlyGenerated(Integer count,
        Integer max = /*runtime.maxArraySize*/ 2147483639)
        => RandomGenerationAnnotation(count, max);

shared final annotation class RandomGenerationAnnotation(Integer count, Integer max)
        satisfies OptionalAnnotation<RandomGenerationAnnotation,ValueDeclaration>
        & ArgumentProvider {
    object nothingProvider satisfies Iterator<Anything> {
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
            } else {
                throw AssertionError("Can't randomly generate ``declaredType`` yet");
            }
        }
        case (is OpenUnion) {
            for (innerType in type.caseTypes) {
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