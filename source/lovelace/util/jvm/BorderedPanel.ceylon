import javax.swing {
    JPanel
}

import java.awt {
    Component,
    BorderLayout
}

import java.lang {
    Types
}

"A [[panel|JPanel]] laid out by a [[BorderLayout]], with helper methods/attributes to
 assign components to its different sectors. (This is especially helpful because
 BorderLayout requires [[Java String|java.lang::String]] sector-identifiers, and
 the Ceylon compiler automatically wraps them in [[Ceylon Strings|String]], then
 doesn't unwrap them because they're passed to methods taking Object, causing
 runtime failures if we don't wrap them in [[Types.nativeString]]."
shared class BorderedPanel extends JPanel {
    variable Component? centerLocal = null;
    "The central component."
    shared Component? center => centerLocal;
    assign center {
        if (exists temp = center) {
            add(temp, Types.nativeString(BorderLayout.center));
        } else if (exists temp = centerLocal) {
            remove(temp);
        }
        centerLocal = center;
    }

    variable Component? lineStartLocal = null;
    "The component in the 'line start' position; this is at the left, or 'west',
     in left-to-right locales."
    shared Component? lineStart => lineStartLocal;
    assign lineStart {
        if (exists temp = lineStart) {
            add(temp, Types.nativeString(BorderLayout.lineStart));
        } else if (exists temp = lineStartLocal) {
            remove(temp);
        }
        lineStartLocal = lineStart;
    }

    variable Component? lineEndLocal = null;
    "The component in the 'line end' position; this is at the right, or 'east',
     in left-to-right locales."
    shared Component? lineEnd => lineEndLocal;
    assign lineEnd {
        if (exists temp = lineEnd) {
            add(temp, Types.nativeString(BorderLayout.lineEnd));
        } else if (exists temp = lineEndLocal) {
            remove(temp);
        }
        lineEndLocal = lineEnd;
    }

    variable Component? pageStartLocal = null;
    "The component in the 'page start' position; this is at the top, or 'north',
     in left-to-right locales."
    shared Component? pageStart => pageStartLocal;
    assign pageStart {
        if (exists temp = pageStart) {
            add(temp, Types.nativeString(BorderLayout.pageStart));
        } else if (exists temp = pageStartLocal) {
            remove(temp);
        }
        pageStartLocal = pageStart;
    }

    variable Component? pageEndLocal = null;
    "The component in the 'page end' position; this is at the bottom, or 'south',
     in left-to-right locales."
    shared Component? pageEnd => pageEndLocal;
    assign pageEnd {
        if (exists temp = pageEnd) {
            add(temp, Types.nativeString(BorderLayout.pageEnd));
        } else if (exists temp = pageEndLocal) {
            remove(temp);
        }
        pageEndLocal = pageEnd;
    }

    "Default constructor."
    shared new (Component? center = null, Component? pageStart = null,
            Component? pageEnd = null, Component? lineEnd = null,
            Component? lineStart = null) extends JPanel(BorderLayout()) {
        this.center = center;
        this.pageStart = pageStart;
        this.pageEnd = pageEnd;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
    }

    "Constructor to arrange three components (pass [[null]] for any position to be left
     empty) in a vertical line."
    shared new verticalPanel(Component? pageStart, Component? center, Component? pageEnd)
        extends BorderedPanel(center, pageStart, pageEnd) { }
    "Constructor to arrange three components (pass [[null]] for any position to be left
     empty) in a horizontal line."
    shared new horizontalPanel(Component? lineStart, Component? center,
            Component? lineEnd) extends BorderedPanel(center, null, null, lineEnd,
                lineStart) { }
}
