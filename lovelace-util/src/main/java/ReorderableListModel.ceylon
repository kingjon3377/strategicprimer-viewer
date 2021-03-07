import javax.swing {
    DefaultListModel
}

import lovelace.util.common {
    Reorderable
}

"An extension of the [[DefaultListModel]] class to add an implementation of the
 [[Reorderable]] interface. For the convenience of callers, the class also takes its
 initial elements as initializer parameters."
shared class ReorderableListModel<Element>(Element* initialElements)
        extends DefaultListModel<Element>() satisfies Reorderable {
    shared actual void reorder(Integer fromIndex, Integer toIndex) {
        if (fromIndex != toIndex) {
            if (fromIndex > toIndex) {
                add(toIndex, remove(fromIndex));
            } else {
                add(toIndex - 1, remove(fromIndex));
            }
        }
    }
    initialElements.each(addElement);
}
