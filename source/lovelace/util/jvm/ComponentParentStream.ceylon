import java.awt {
	Component
}
shared class ComponentParentStream satisfies {Component*} {
	static class ComponentParentIterator(variable Component widget)
			satisfies Iterator<Component> {
		shared actual Component|Finished next() {
			if (exists retval = widget.parent) {
				widget = retval;
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