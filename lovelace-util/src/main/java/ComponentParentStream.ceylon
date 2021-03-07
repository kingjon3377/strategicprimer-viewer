import java.awt {
    Component
}
import javax.swing {
    JMenu,
    JPopupMenu
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
                if (!current exists) {
                    if (is JPopupMenu retval) {
                        current = retval.invoker;
                    } else if (is JMenu retval) {
                        current = retval.popupMenu;
                    }
                }
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
