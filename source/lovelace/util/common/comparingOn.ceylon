"Compare an object with another object based on one field in particular."
shared Comparison(BaseType, BaseType) comparingOn<BaseType, FieldType>(
        "The field as a function of the object."
        FieldType(BaseType) extractor,
        "How to compare the corresponding field instances"
        Comparison(FieldType, FieldType) comparator) {
    return (BaseType first, BaseType second) =>
        comparator(extractor(first), extractor(second));
}