import java.awt {
	Component
}
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
