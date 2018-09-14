import ceylon.language.meta.declaration {
    ClassOrInterfaceDeclaration,
    FunctionOrValueDeclaration
}
import ceylon.test.engine.spi {
    ArgumentListProvider,
    ArgumentProviderContext,
    ArgumentProvider
}
"The annotation class for the [[todo]] annotation."
by("Jonathan Lovelace")
shared final annotation class TodoAnnotation(
    "Explanations of what remains to be done about the annotated element"
    shared String* description)
        satisfies OptionalAnnotation<TodoAnnotation, Annotated> {}

"Annotation to describe a TODO item"
by("Jonathan Lovelace")
shared annotation TodoAnnotation todo(
    "Explanations of what remains to be done about the annotated element"
    String* description) => TodoAnnotation(*description);

"The annotation class for the [[enumeratedParameter]] annotation."
by("Jonathan Lovelace")
shared final annotation class EnumSingleParameterAnnotation(
            ClassOrInterfaceDeclaration type)
        satisfies OptionalAnnotation<EnumSingleParameterAnnotation,
            FunctionOrValueDeclaration> & ArgumentListProvider & ArgumentProvider {
    [Object] entuple(Object item) => [item];
    shared actual {Anything[]*} argumentLists(ArgumentProviderContext context) =>
            type.apply<Anything>().caseValues.coalesced.map(entuple);
    shared actual {Anything*} arguments(ArgumentProviderContext context) =>
            type.apply<Anything>().caseValues.coalesced;

}
"Annotation to replace [[ceylon.test::parameters]], providing each case value of an
 enumerated type as an argument for the test method in turn."
by("Jonathan Lovelace")
shared annotation EnumSingleParameterAnnotation enumeratedParameter(
        ClassOrInterfaceDeclaration type) => EnumSingleParameterAnnotation(type);

"An annotation to make a parameterized test randomly generate numbers."
shared annotation RandomGenerationAnnotation randomlyGenerated(Integer count,
        Integer max = /*runtime.maxArraySize*/ 2147483639)
            => RandomGenerationAnnotation(count, max);

shared final annotation class RandomGenerationAnnotation(Integer count, Integer max)
        satisfies OptionalAnnotation<RandomGenerationAnnotation, FunctionOrValueDeclaration>
        & ArgumentProvider {
    shared actual {Anything*} arguments(ArgumentProviderContext context) =>
            // TODO: If context specifies types, return appropriate types for non-Integer args
            singletonRandom.integers(max).take(count);
}