import lovelace.util.common {
    todo
}

"An interface for UIs (etc.) for adding and removing items in lists."
todo("Combine with other 'source' interfaces now we have reified generics & union types",
    "Move to `lovelace.util`?")
interface AddRemoveSource {
    "Add a listener."
    shared formal void addAddRemoveListener(AddRemoveListener listener);

    "Remove a listener."
    shared formal void removeAddRemoveListener(AddRemoveListener listener);
}
