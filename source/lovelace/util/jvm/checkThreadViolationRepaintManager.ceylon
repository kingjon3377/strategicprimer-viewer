import java.lang.ref {
    WeakReference
}


import javax.swing {
    JButton,
    JComponent,
    JEditorPane,
    JFrame,
    RepaintManager,
    SwingUtilities
}
import java.lang {
    ObjectArray,
    StackTraceElement,
    Thread
}

"This class is used to detect Event Dispatch Thread rule violations.

 See [How to Use Threads](http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html)
 for more info

 This is a modification of original idea of Scott Delap

 Initial version of ThreadCheckingRepaintManager can be found here: [Easily Find
 Swing Threading Mistakes](http://www.clientjava.com/blog/2004/08/20/1093059428000.html)"
by("Scott Delap", "Alexander Potochkin", "https://swinghelper.dev.java.net/")
shared class CheckThreadViolationRepaintManager extends RepaintManager {
    "Report a violation"
    static void violationFound("The component in which the violation was found" JComponent c,
            "The stack trace to report" ObjectArray<StackTraceElement> stackTrace) {
        process.writeLine();
        process.writeLine("EDT violation detected");
        process.writeLine(c.string);
        for (st in stackTrace) {
            process.writeLine("\tat ``st``");
        }
    }
    "Whether to run the complete check."
    shared variable Boolean completeCheck;

    "The last(-referenced?) component."
    variable WeakReference<JComponent>? lastComponent;

    shared new ("Whether to run the complete check (recommended)." Boolean shouldCompleteCheck = true)
            extends RepaintManager() {
        completeCheck = shouldCompleteCheck;
        lastComponent = null;
    }

    "Check thread violations for a component."
    void checkThreadViolations(JComponent c) {
        if (!SwingUtilities.eventDispatchThread && (completeCheck || c.showing)) {
            variable Boolean repaint = false;
            variable Boolean fromSwing = false;
            variable Boolean imageUpdate = false;
            value stackTrace = Thread.currentThread().stackTrace;
            for (st in stackTrace) {
                if (repaint && st.className.startsWith("javax.swing")) {
                    fromSwing = true;
                }
                if (repaint && "imageUpdate" == st.methodName) {
                    imageUpdate = true;
                }
                if ("repaint" == st.methodName) {
                    repaint = true;
                    fromSwing = false;
                }
            }
            if (imageUpdate) {
                // assuming it is java.awt.image.ImageObserver.imageUpdate(...)
                // image was asynchronously updated, that's ok
                return;
            }
            if (repaint && !fromSwing) {
                // no problems here, since repaint() is thread safe
                return;
            }
            // ignore the last processed component
            if (exists lastComp = lastComponent, c === lastComp.get()) {
                return;
            }
            lastComponent = WeakReference(c);
            violationFound(c, stackTrace);
        }
    }

    "Add (and check) an invalid component"
    shared actual void addInvalidComponent("the component to add" JComponent? component) {
        if (exists component) {
            checkThreadViolations(component);
            super.addInvalidComponent(component);
        }
    }

    "Add a dirty region."
    shared actual void addDirtyRegion("the component involved" JComponent? component,
            "The X coordinate of the dirty region" Integer x,
            "The Y coordinate of the dirty region" Integer y,
            "The width of the dirty region" Integer w,
            "The height of the dirty region" Integer h) {
        if (exists component) {
            checkThreadViolations(component);
            super.addDirtyRegion(component, x, y, w, h);
        }
    }
}

void simpleTest() {
    JFrame frame = JFrame("Am I on EDT?");
    frame.defaultCloseOperation = JFrame.exitOnClose;
    frame.add(JButton("JButton"));
    frame.pack();
    frame.visible = true;
    frame.dispose();
}

void imageUpdateTest() { // TODO: Surely this should be called from checkThreadViolations() somewhere?
    JFrame frame = JFrame();
    frame.defaultCloseOperation = JFrame.exitOnClose;
    JEditorPane editor = JEditorPane();
    frame.contentPane = editor;
    editor.contentType = "text/html";
    editor.text = """<html><img src="file:\\lala.png"></html>""";
    frame.setSize(300, 200);
    frame.visible = true;
    // frame.dispose(); // ???
}

variable JButton? test = null;

void repaintTest() {
    try {
        SwingUtilities.invokeAndWait(() {
                test = JButton();
                assert (exists localTest = test);
                localTest.setSize(100, 100);
            });
    } catch (Exception except) {
        except.printStackTrace();
    }
    assert (exists localTest = test);
    // repaint(Rectangle) should be ok
    localTest.repaint(localTest.bounds);
    localTest.repaint(0, 0, 100, 100);
    localTest.repaint();
}

shared void checkThreadViolations() {
    // set CheckThreadViolationRepaintManager
    RepaintManager.setCurrentManager(CheckThreadViolationRepaintManager());
    // Valid code
    SwingUtilities.invokeAndWait(simpleTest);
    process.writeLine("Valid code passed ...");
    repaintTest();
    process.writeLine("Repaint test - correct code");
    // Invalid code: stack trace expected in the following
    simpleTest();
}
