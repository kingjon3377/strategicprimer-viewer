import java.awt.event {
    KeyEvent,
    ActionEvent,
    InputEvent
}

import javax.swing {
    InputMap,
    ActionMap,
    KeyStroke,
    AbstractAction
}

import lovelace.util.jvm {
    platform
}

"A collection of code to help set up the map viewer to listen to the arrow
 keys, the numeric keypad, and other motion keys."
object arrowListenerInitializer {
    "Key-codes for arrow keys and the numeric keypad to Strings we will use to
     represent them."
    {<Integer->String>*} arrowInputs = [
        KeyEvent.vkUp->"up", KeyEvent.vkDown->"down", KeyEvent.vkRight->"right",
        KeyEvent.vkLeft->"left", KeyEvent.vkKpDown->"down", KeyEvent.vkNumpad2->"down",
        KeyEvent.vkKpRight->"right", KeyEvent.vkNumpad6->"right", KeyEvent.vkKpUp->"up",
        KeyEvent.vkNumpad8->"up", KeyEvent.vkKpLeft->"left", KeyEvent.vkNumpad4->"left",
        KeyEvent.vkNumpad9->"up-right", KeyEvent.vkNumpad7->"up-left",
        KeyEvent.vkNumpad3->"down-right", KeyEvent.vkNumpad1->"down-left"
    ];

    Anything() join(Anything() first, Anything() second) {
        void retval() { // TODO: Inline and make join() use =>?
            first();
            second();
        }
        return retval;
    }

    """Key-codes that are used, when modified with a platgform-specific modifier,
       for "jumping," and the Strings we'll use to represent them."""
    {<Integer->String>*} jumpInputs = [ KeyEvent.vkHome->"ctrl-home",
        KeyEvent.vkEnd->"ctrl-end" ];

    """Key-codes that are used, when modified with the appropriate modifier, for "jumping"
       only on the Mac platform, and the Strings we'll use to represent them."""
    {<Integer->String>*} macJumpInputs = [ KeyEvent.vkUp->"home", KeyEvent.vkKpUp->"home",
        KeyEvent.vkNumpad8->"home", KeyEvent.vkDown->"end", KeyEvent.vkKpDown->"end",
        KeyEvent.vkNumpad2->"end", KeyEvent.vkLeft->"caret",KeyEvent.vkKpLeft->"caret",
        KeyEvent.vkNumpad4->"caret", KeyEvent.vkRight->"dollar",
        KeyEvent.vkKpRight->"dollar", KeyEvent.vkNumpad6->"dollar" ];

    "Other key-codes and the Strings we'll use to represent them"
    {<Integer->String>*} otherInputs = [
        KeyEvent.vkHome->"home", KeyEvent.vk0->"home", KeyEvent.vkNumpad0->"home",
        KeyEvent.vkEnd->"end", KeyEvent.vkNumberSign->"end", KeyEvent.vkDollar->"dollar",
        KeyEvent.vkCircumflex->"caret", '#'.integer->"end",'^'.integer->"caret"
    ];

    void repeatVoid(Anything() func, Integer times) { // Once lovelace.util.common::invoke exists, use it to condense this
        for (i in 0:times) {
            func();
        }
    }
    class DirectionListener(Anything() action, Integer num = 1)
            extends AbstractAction() {
        shared actual void actionPerformed(ActionEvent event) => repeatVoid(action, num);
    }

    shared void setUpArrowListeners(DirectionSelectionChanger selListener,
            InputMap inputMap, ActionMap actionMap) {
        Integer fiveMask = (platform.systemIsMac) then InputEvent.altDownMask
            else InputEvent.ctrlDownMask;
        for (stroke->action in arrowInputs) {
            inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
            inputMap.put(KeyStroke.getKeyStroke(stroke, fiveMask), "ctrl-``action``");
        }

        "Strings representing arrow-key key codes and the actions that should be
         mapped to them."
        {<String->Anything()>*} arrowActions =
                [
                    "up"->selListener.up,
                    "down"->selListener.down,
                    "left"->selListener.left,
                    "right"->selListener.right,
                    "up-right"->join(selListener.up, selListener.right),
                    "up-left"->join(selListener.up, selListener.left),
                    "down-right"->join(selListener.down, selListener.right),
                    "down-left"->join(selListener.down, selListener.left)
                ];

        for (action->consumer in arrowActions) {
            actionMap.put(action, DirectionListener(consumer));
            actionMap.put("ctrl-``action``", DirectionListener(consumer, 5));
        }

        Integer jumpModifier = platform.shortcutMask;
        for (stroke->action in jumpInputs) {
            inputMap.put(KeyStroke.getKeyStroke(stroke, jumpModifier), action);
        }

        if (platform.systemIsMac) {
            for (stroke->action in macJumpInputs) {
                inputMap.put(KeyStroke.getKeyStroke(stroke, jumpModifier), action);
            }
        }

        for (stroke->action in otherInputs) {
            inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
        }

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk3, InputEvent.shiftDownMask),
            "end");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk6, InputEvent.shiftDownMask),
            "caret");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.vk4, InputEvent.shiftDownMask),
            "dollar");

        actionMap.put("ctrl-home", DirectionListener(join(selListener.jumpUp,
            selListener.jumpLeft)));
        actionMap.put("home", DirectionListener(selListener.jumpUp));
        actionMap.put("ctrl-end", DirectionListener(join(selListener.jumpDown,
            selListener.jumpRight)));
        actionMap.put("end", DirectionListener(selListener.jumpDown));
        actionMap.put("caret", DirectionListener(selListener.jumpLeft));
        actionMap.put("dollar", DirectionListener(selListener.jumpRight));
    }
}
