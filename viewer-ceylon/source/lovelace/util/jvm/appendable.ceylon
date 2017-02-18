import java.lang {
    Appendable,
    CharSequence
}
"An implementation of Appendable that wraps a lambda."
shared class AppendableHelper(Anything(String) wrapped) satisfies Appendable {
    shared actual Appendable append(CharSequence csq) {
        wrapped(csq.string);
        return this;
    }
    shared actual Appendable append(CharSequence csq, Integer start, Integer end) =>
            append(csq.subSequence(start, end));
    shared actual Appendable append(Character c) => append(c.string);
}