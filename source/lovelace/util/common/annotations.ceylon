import ceylon.language.meta.declaration {
	FunctionDeclaration,
	ClassOrInterfaceDeclaration
}
import ceylon.test.engine.spi {
	ArgumentListProvider,
	ArgumentProviderContext
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
shared final annotation class EnumSingleParameterAnnotation(ClassOrInterfaceDeclaration type)
		satisfies OptionalAnnotation<EnumSingleParameterAnnotation,FunctionDeclaration> & ArgumentListProvider {
	[Object] entuple(Object item) => [item];
	shared actual {Anything[]*} argumentLists(ArgumentProviderContext context) =>
			type.apply<Anything>().caseValues.coalesced.map(entuple);
}
"Annotation to replace [[ceylon.test::parameters]], providing each case value of an enumerated type as an
 argument for the test method in turn."
by("Jonathan Lovelace")
shared annotation EnumSingleParameterAnnotation enumeratedParameter(ClassOrInterfaceDeclaration type) =>
		EnumSingleParameterAnnotation(type);
"The annotation class for the [[enumeratedParameters]] annotation."
by("Jonathan Lovelace")
shared final annotation class EnumDoubleParameterAnnotation(ClassOrInterfaceDeclaration first, ClassOrInterfaceDeclaration second)
		satisfies OptionalAnnotation<EnumDoubleParameterAnnotation,FunctionDeclaration>&ArgumentListProvider {
	shared actual {Anything[]*} argumentLists(ArgumentProviderContext context) =>
			first.apply<Anything>().caseValues.product(second.apply<Anything>().caseValues);
}
"Annotation to replace [[ceylon.test::parameters]], providing each combination of values of two enumerated types in turn
 as arguments for the test method."
by("Jonathan Lovelace")
shared annotation EnumDoubleParameterAnnotation enumeratedParameters(ClassOrInterfaceDeclaration first,
	ClassOrInterfaceDeclaration second) => EnumDoubleParameterAnnotation(first, second);