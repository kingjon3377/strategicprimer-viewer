import javax.swing.tree {
    DefaultMutableTreeNode,
    MutableTreeNode
}

import lovelace.util.common {
    todo,
	anythingEqual
}

import strategicprimer.model.map {
    Point
}
import strategicprimer.report {
    IReportNode
}
"A node representing a list."
shared class ListReportNode extends DefaultMutableTreeNode satisfies IReportNode {
	static Integer boilerPlateLength = "<ul></ul>".size + 3;
	static Integer perChildBoilerPlate = "<li></li>".size + 1;
	variable String initialText;
	shared actual variable Point? localPoint;
	shared new (String initialText, Point? localPoint = null)
			extends DefaultMutableTreeNode(initialText) {
		this.initialText = initialText;
		this.localPoint = localPoint;
	}

    shared actual void appendNode(MutableTreeNode node) {
        if (isNonEmptyNode(node)) {
            super.add(node);
        }
    }
    shared actual void add(MutableTreeNode node) => appendNode(node);
    shared actual String text => initialText;
    assign text {
        super.userObject = text;
        initialText = text;
    }
    shared actual Integer htmlSize =>
            boilerPlateLength + text.size +
            Integer.sum(map(IReportNode.htmlSize).map(perChildBoilerPlate.plus));
    shared actual void produce(Anything(String) stream) {
        stream(text);
        stream("""
                  <ul>
                  """);
        for (node in this) {
            stream("<li>");
            node.produce(stream);
            stream("""</li>
                      """);
        }
        stream("""</ul>
                  """);
    }
    shared actual Boolean equals(Object that) {
        if (is ListReportNode that, that.initialText == initialText,
	            children() == that.children()) {
            return anythingEqual(localPoint, that.localPoint);
        } else {
            return false;
        }
    }
    shared actual Integer hash => initialText.hash;
    todo("Reflect the children")
    shared actual String string => text;
    shared actual void setUserObject(Object obj) => super.userObject = obj;
}
