import java.awt.event {
    ActionListener,
    ActionEvent
}

import javax.swing {
    Action,
    AbstractAction
}

"A wrapper around an [[ActionListener]] (or equivalent lambda) that extends
 [[AbstractAction]], for the exceedingly common case of a JDK method requiring an
 [[Action]] when we don't need more functionality than a single method accepting an
 [[ActionEvent]]."
shared class ActionWrapper(Anything(ActionEvent)|ActionListener wrappedListener)
        extends AbstractAction() {
    Anything(ActionEvent) wrapped;
    if (is ActionListener wrappedListener) {
        wrapped = wrappedListener.actionPerformed;
    } else {
        wrapped = wrappedListener;
    }
    shared actual void actionPerformed(ActionEvent event) => wrapped(event);
}
