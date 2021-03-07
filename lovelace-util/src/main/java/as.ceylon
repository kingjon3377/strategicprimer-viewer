"""If the given value is of the specified type, return it; otherwise, return null.

   Taken from [a Github issue
   comment](https://github.com/eclipse/ceylon/issues/4283#issuecomment-156661462)."""
by("Gavin King")
shared T? as<T>(Anything t) {
    if (is T t) {
        return t;
    } else {
        return null;
    }
}
