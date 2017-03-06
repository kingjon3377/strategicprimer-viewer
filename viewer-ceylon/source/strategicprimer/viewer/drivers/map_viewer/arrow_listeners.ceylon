import ceylon.collection {
    HashMap
}

import java.awt.event {
    KeyEvent,
    ActionEvent,
    InputEvent
}

import javax.swing {
    InputMap,
    ActionMap,
    KeyStroke
}

import lovelace.util.jvm {
    platform,
    ActionWrapper
}
"A map from key-codes for arrow keys and the numeric keypad to Strings we will use to
 represent them."
Map<Integer, String> arrowInputs = HashMap<Integer, String> {
    KeyEvent.vkUp->"up", KeyEvent.vkDown->"down", KeyEvent.vkRight->"right",
    KeyEvent.vkLeft->"left", KeyEvent.vkKpDown->"down", KeyEvent.vkNumpad2->"down",
    KeyEvent.vkKpRight->"right", KeyEvent.vkNumpad6->"right", KeyEvent.vkKpUp->"up",
    KeyEvent.vkNumpad8->"up", KeyEvent.vkKpLeft->"left", KeyEvent.vkNumpad4->"left",
    KeyEvent.vkNumpad9->"up-right", KeyEvent.vkNumpad7->"up-left",
    KeyEvent.vkNumpad3->"down-right", KeyEvent.vkNumpad1->"down-left"
};
Anything(T) join<T>(Anything(T) first, Anything(T) second) {
    void retval(T arg) {
        first(arg);
        second(arg);
    }
    return retval;
}
"A map from Strings representing arrow-key key codes to the actions that should be mapped
 to them."
Map<String, Anything(DirectionSelectionChanger)> arrowActions =
        HashMap<String, Anything(DirectionSelectionChanger)> {
            "up"->DirectionSelectionChanger.up, "down"->DirectionSelectionChanger.down,
            "left"->DirectionSelectionChanger.left,
            "right"->DirectionSelectionChanger.right,
            "up-right"->join(DirectionSelectionChanger.up,
                DirectionSelectionChanger.right),
            "up-left"->join(DirectionSelectionChanger.up,
                DirectionSelectionChanger.left),
            "down-right"->join(DirectionSelectionChanger.down,
                DirectionSelectionChanger.right),
            "down-left"->join(DirectionSelectionChanger.down,
                DirectionSelectionChanger.left)
        };
Iterable<Entry<Integer, String>> maybe(Boolean condition,
        Iterable<Entry<Integer, String>> ifTrue) {
    if (condition) {
        return ifTrue;
    } else {
        return {};
    }
}
"""A map from key-codes that are used, when modified with a platgform-specific modifier,
   for "jumping," to the Strings we'll use to represent them."""
Map<Integer, String> jumpInputs = HashMap<Integer, String> {
    KeyEvent.vkHome->"ctrl-home", KeyEvent.vkEnd->"ctrl-end",
    *maybe(platform.systemIsMac, {
        KeyEvent.vkUp->"home", KeyEvent.vkKpUp->"home", KeyEvent.vkNumpad8->"home",
        KeyEvent.vkDown->"end", KeyEvent.vkKpDown->"end", KeyEvent.vkNumpad2->"end",
        KeyEvent.vkLeft->"caret",KeyEvent.vkKpLeft->"caret", KeyEvent.vkNumpad4->"caret",
        KeyEvent.vkRight->"dollar", KeyEvent.vkKpRight->"dollar",
        KeyEvent.vkNumpad6->"dollar"})
};
"A map from other key-codes to the Strings we'll use to represent them"
Map<Integer, String> otherInputs = HashMap<Integer, String> {
    KeyEvent.vkHome->"home", KeyEvent.vk0->"home", KeyEvent.vkNumpad0->"home",
    KeyEvent.vkEnd->"end", KeyEvent.vkNumberSign->"end", KeyEvent.vkDollar->"dollar",
    KeyEvent.vkCircumflex->"caret",
    // TODO: Test that this works; Java used Character.getNumericValue('#')
    '#'.integer->"end",'^'.integer->"caret"
};
void repeat<T>(Anything(T) func, T args, Integer times) {
    for (i in 0..times) {
        func(args);
    }
}
void repeatVoid(Anything() func, Integer times) {
    for (i in 0..times) {
        func();
    }
}
void setUpArrowListeners(DirectionSelectionChanger selListener, InputMap inputMap,
        ActionMap actionMap) {
    class DirectionListener(Anything() action, Integer num = 1)
            extends ActionWrapper((ActionEvent event) => repeatVoid(action, num)) { }
    Integer fiveMask = (platform.systemIsMac) then InputEvent.altDownMask
    else InputEvent.ctrlDownMask;
    for (stroke->action in arrowInputs) {
        inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
        inputMap.put(KeyStroke.getKeyStroke(stroke, fiveMask), "ctrl-``action``");
    }
    for (action->consumer in arrowActions) {
        actionMap.put(action, DirectionListener(() => consumer(selListener)));
        actionMap.put(action, DirectionListener(() => consumer(selListener), 5));
    }
    Integer jumpModifier = platform.shortcutMask;
    for (stroke->action in jumpInputs) {
        inputMap.put(KeyStroke.getKeyStroke(stroke, jumpModifier), action);
    }
    for (stroke->action in otherInputs) {
        inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
    }
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk3, InputEvent.shiftDownMask), "end");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk6, InputEvent.shiftDownMask), "caret");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk4, InputEvent.shiftDownMask), "dollar");
    Anything() join(Anything(DirectionSelectionChanger) first,
            Anything(DirectionSelectionChanger) second) {
        void retval() {
            first(selListener);
            second(selListener);
        }
        return retval;
    }
    actionMap.put("ctrl-home", DirectionListener(join(DirectionSelectionChanger.jumpUp,
        DirectionSelectionChanger.jumpLeft)));
    actionMap.put("home", DirectionListener(() => selListener.jumpUp()));
    actionMap.put("ctrl-end", DirectionListener(join(DirectionSelectionChanger.jumpDown,
        DirectionSelectionChanger.jumpRight)));
    actionMap.put("end", DirectionListener(() => selListener.jumpDown()));
    actionMap.put("caret", DirectionListener(() => selListener.jumpLeft()));
    actionMap.put("dollar", DirectionListener(() => selListener.jumpRight()));
}
