import java.awt.event {
    ActionListener,
    ActionEvent
}

import javax.swing {
    Action,
    InputMap,
    JComponent,
    KeyStroke
}

"Set up a hot-key for an action that doesn't call a *menu* item."
shared void createHotKey(
        "The component defining the action's context"
        JComponent component,
        "The String to use to identify the action"
        String action,
        "The listener that should handle the action"
        Action|ActionListener|Anything(ActionEvent) handler,
        "See [[JComponent#getInputMap(Integer)]]."
        Integer condition,
        "The keys to use as hot-keys"
        KeyStroke* keys) {
    InputMap inputMap = component.getInputMap(condition);
    for (key in keys) {
        inputMap.put(key, action);
    }
    Action temp;
    if (is Action handler) {
        temp = handler;
    } else {
        temp = ActionWrapper(handler);
    }
    component.actionMap.put(action, temp);
}
