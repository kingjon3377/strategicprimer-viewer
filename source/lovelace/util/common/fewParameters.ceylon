import ceylon.language.meta.declaration {
    FunctionOrValueDeclaration,
    FunctionDeclaration,
    ValueDeclaration
}
import ceylon.test.engine.spi {
    ArgumentProvider,
    ArgumentProviderContext
}
import ceylon.random {
    randomize
}
"An annotation to specify a source of argument values for parameterized tests and a number
 to limit to. This is effectively an extension of [[ceylon.test::parameters]] to limit the
 number of test cases that are executed in any given pass through the test suite. If
 annotations were allowed to define non-literal default arguments, we would provide a
 default [[count]] of [[runtime.maxArraySize]], and if they were additionally allowed to
 accept non-primitive, non-metamodel arguments we would allow callers to pass in the
 RNG to use when choosing from the source."
shared annotation FewParametersAnnotation fewParameters(FunctionOrValueDeclaration source,
        Integer count) => FewParametersAnnotation(source, count);

"Annotation class for [[fewParameters]]."
shared final annotation class FewParametersAnnotation(FunctionOrValueDeclaration source,
        Integer count)
        satisfies OptionalAnnotation<FewParametersAnnotation, FunctionOrValueDeclaration>&
            ArgumentProvider {
    shared actual {Anything*} arguments(ArgumentProviderContext context) {
        switch (source)
        case (is FunctionDeclaration) {
            return randomize(source.apply<{Anything*}, []>()()).take(count);
        }
        case (is ValueDeclaration) {
            return randomize(source.apply<{Anything*}>().get()).take(count);
        }
    }
}
