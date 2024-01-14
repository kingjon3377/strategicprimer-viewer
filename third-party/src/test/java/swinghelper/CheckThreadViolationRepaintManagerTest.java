package swinghelper;

import javax.swing.WindowConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class CheckThreadViolationRepaintManagerTest {
	static void simpleTest() {
		final JFrame frame = new JFrame("Am I on EDT?");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(new JButton("JButton"));
		frame.pack();
		frame.setVisible(true);
		frame.dispose();
	}

	static void imageUpdateTest() { // TODO: Surely this should be called from checkThreadViolations() somewhere?
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		final JEditorPane editor = new JEditorPane();
		frame.setContentPane(editor);
		editor.setContentType("text/html");
		editor.setText("<html><img src=\"file:\\\\lala.png\"></html>");
		frame.setSize(300, 200);
		frame.setVisible(true);
		// frame.dispose(); // ???
	}

	@Nullable JButton test = null;

	void repaintTest() {
		try {
			SwingUtilities.invokeAndWait(() -> {
				final JButton localTest = test = new JButton();
				localTest.setSize(100, 100);
			});
		} catch (final Exception except) {
			except.printStackTrace();
		}
		final JButton localTest = Objects.requireNonNull(test);
		// repaint(Rectangle) should be ok
		localTest.repaint(localTest.getBounds());
		localTest.repaint(0, 0, 100, 100);
		localTest.repaint();
	}

	public void checkThreadViolations() throws InvocationTargetException, InterruptedException {
		// set CheckThreadViolationRepaintManager
		RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
		// Valid code
		SwingUtilities.invokeAndWait(CheckThreadViolationRepaintManagerTest::simpleTest);
		System.out.println("Valid code passed ...");
		repaintTest();
		System.out.println("Repaint test - correct code");
		// Invalid code: stack trace expected in the following
		simpleTest();
	}
}
