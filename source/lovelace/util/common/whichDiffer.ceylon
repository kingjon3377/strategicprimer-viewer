"Print, using the given method, which of the given pairs is the first non-equal."
shared void whichDiffer(Anything(String) write, [Anything, Anything]* pairs) {
    for ([first, second] in pairs) {
        if (!anythingEqual(first, second)) {
            write("``first else "null"`` != ``second else "null"``");
            return;
        }
    } else {
        write("All are equal");
    }
}