"The annotation class for the [[todo]] annotation."
by("Jonathan Lovelace")
shared final annotation class TodoAnnotation(
    "Explanations of what remains to be done about the annotated element"
    shared String* description)
        satisfies OptionalAnnotation<TodoAnnotation, Annotated> {}

"Annotation to describe a TODO item"
shared annotation TodoAnnotation todo(
    "Explanations of what remains to be done about the annotated element"
    String* description) => TodoAnnotation(*description);
