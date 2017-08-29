"""If the given value is of the specified type, return it; otherwise, return null.

   Taken from [a Github issue
   comment](https://github.com/ceylon/ceylon-spec/issues/1177#issuecomment-68575925)."""
by("Gavin King")
shared T? as<T>(Anything t) {
    if (is T t) {
        return t;
    } else {
        return null;
    }
}