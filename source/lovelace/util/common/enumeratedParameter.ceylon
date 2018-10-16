import ceylon.test.engine.spi {
    ArgumentListProvider,
    ArgumentProviderContext,
    ArgumentProvider
}
import ceylon.language.meta.declaration {
    FunctionOrValueDeclaration,
    ClassOrInterfaceDeclaration
}
import ceylon.test {
    parameters
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
"Annotation that provides each case value of an enumerated type in turn as an argument
 for a test method. May be applied either to the individual parameter or, if
 the method only takes one parameter, to the method as a whole."
by("Jonathan Lovelace")
see(`function parameters`)
shared annotation EnumSingleParameterAnnotation enumeratedParameter(
        ClassOrInterfaceDeclaration type) => EnumSingleParameterAnnotation(type);
