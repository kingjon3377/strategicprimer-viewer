import java.awt {
    Component
}

"A [[stream|Iterable]] of a component's [[parent|Component.parent]], its
 parent, and so on until a component's parent is null."
shared class ComponentParentStream satisfies {Component*} {
    static class ComponentParentIterator(Component widget)
            satisfies Iterator<Component> {
        variable Component? current = widget;
        shared actual Component|Finished next() {
            if (exists retval = current) {
                current = retval.parent;
                return retval;
            } else {
                return finished;
            }
        }
    }
    Component widget;
    shared new (Component component) { widget = component; }
    shared actual Iterator<Component> iterator() => ComponentParentIterator(widget);
}
