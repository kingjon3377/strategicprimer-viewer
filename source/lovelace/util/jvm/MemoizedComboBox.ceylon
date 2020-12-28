import java.lang {
    JString=String
}
import javax.swing {
    JTextField
}
import java.awt.event {
    ActionEvent
}
import ceylon.language.meta {
    classDeclaration
}
import ceylon.collection {
    MutableSet,
    HashSet
}

"Extends [[ImprovedComboBox]] to keep a running collection of values."
shared class MemoizedComboBox(Anything(String) logger = log.error)
        extends ImprovedComboBox<String>() {
    "The values we've had in the past."
    MutableSet<String> values = HashSet<String>();
    "Clear the combo box, but if its value was one we haven't had previously, add it
     to the drop-down list."
    shared void checkAndClear() {
        assert (is String? temp = selectedItem);
        if (exists item = temp?.trimmed, !item.empty, !values.contains(item)) {
            values.add(item);
            addItem(item);
        }
        selectedItem = null;
    }

    shared actual Object? selectedItem {
        Anything retval = super.selectedItem;
        if (is String retval) {
            return retval.trimmed;
        } else if (exists retval) {
            return retval.string.trimmed;
        } else {
            return null;
        }
    }

    assign selectedItem {
        if (is String|JString? selectedItem) {
            super.selectedItem = selectedItem;
        } else {
            logger("Failed to set selectedItem: must be a String.");
        }
    }

    shared String selectedString {
        if (is JTextField inner = editor.editorComponent) {
            value text = inner.text;
            if (!text.empty) {
                selectedItem = text;
                return text;
            }
        }
        assert (is String retval = selectedItem);
        return retval;
    }

    shared void addSubmitListener(Anything(ActionEvent) listener) {
        value inner = editor.editorComponent;
        if (is JTextField inner) {
            inner.addActionListener(listener);
        } else {
            logger("Editor wasn't a text field, but a ``
                classDeclaration(inner)``");
        }
    }
}
