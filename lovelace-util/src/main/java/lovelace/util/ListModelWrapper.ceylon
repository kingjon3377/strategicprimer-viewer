import javax.swing {
    ListModel
}

import java.lang {
    ArrayIndexOutOfBoundsException
}

"A class to adapt a [[ListModel]] to Ceylon's [[List]] interface."
shared class ListModelWrapper<Element>(ListModel<Element> wrapped)
        satisfies List<Element> {
    shared actual Element? getFromFirst(Integer index) {
        try {
            return wrapped.getElementAt(index);
        } catch (ArrayIndexOutOfBoundsException except) {
            return null;
        }
    }
    shared actual Integer? lastIndex =>
            if (wrapped.size == 0) then null else wrapped.size - 1;
    shared actual Integer hash => wrapped.hash;
    shared actual Boolean equals(Object that) =>
            if (is ListModelWrapper<out Anything> that) then wrapped==that.wrapped
            else false;
}
