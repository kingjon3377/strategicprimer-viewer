"A simple [[SPOptions]] implementation for drivers that don't accept options."
shared object emptyOptions satisfies SPOptions {
    shared actual SPOptions copy() => this;
    shared actual Boolean defines(String key) => false;
    shared actual String? get(String key) => null;
    shared actual String getArgument(String option) => "";
    shared actual Boolean hasOption(String option) => false;
    shared actual Iterator<String->String> iterator() => emptyIterator;
}

