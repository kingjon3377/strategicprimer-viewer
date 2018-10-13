import ceylon.test.engine.spi {
    ArgumentListProvider,
    ArgumentProviderContext,
    ArgumentProvider
}
import ceylon.language.meta.declaration {
    FunctionOrValueDeclaration,
    ClassOrInterfaceDeclaration
}
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
