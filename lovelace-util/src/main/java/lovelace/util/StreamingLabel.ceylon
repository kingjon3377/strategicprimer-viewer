import javax.swing {
    JEditorPane
}

import java.awt {
    Color
}

"Possible colors for use by text in a [[StreamingLabel]]"
shared class LabelTextColor {
    shared actual String string;
    shared new yellow { string = "yellow"; }
    shared new white { string = "white"; }
    shared new red { string = "red"; }
    shared new green { string = "green"; }
    shared new black { string = "black"; }
}

"A label that can easily be written (appended) to."
shared class StreamingLabel extends JEditorPane {
    shared new () extends JEditorPane("text/html",
        """<html><body bgcolor="#ffffff"><p>&nbsp;</p></body></html>""") {}
    editable = false;
    setBackground(Color.white);
    opaque = true;
    StringBuilder buffer = StringBuilder();
    "Add text to the label."
    shared void append(String string) {
        buffer.append(string);
        text = "<html><body bgcolor=\"#ffffff\">``buffer``</body></html>";
        repaint();
    }
    "Add text to the label, followed by a newline."
    shared void appendLine(String string) {
        buffer.append(string);
        buffer.append("<br />");
        text = "<html><body bgcolor=\"#ffffff\">``buffer``</body></html>";
        repaint();
    }
}
