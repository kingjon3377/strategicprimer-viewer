package swinghelper;

import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

public class CheckThreadViolationRepaintManagerTest {
	void simpleTest() {
		JFrame frame = new JFrame("Am I on EDT?");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JButton("JButton"));
		frame.pack();
		frame.setVisible(true);
		frame.dispose();
	}

	void imageUpdateTest() { // TODO: Surely this should be called from checkThreadViolations() somewhere?
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JEditorPane editor = new JEditorPane();
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
				JButton localTest = test = new JButton();
				localTest.setSize(100, 100);
			});
		} catch (final Exception except) {
			except.printStackTrace();
		}
		JButton localTest = test;
		assert (localTest != null);
		// repaint(Rectangle) should be ok
		localTest.repaint(localTest.getBounds());
		localTest.repaint(0, 0, 100, 100);
		localTest.repaint();
	}

	public void checkThreadViolations() throws InvocationTargetException, InterruptedException {
		// set CheckThreadViolationRepaintManager
		RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
		// Valid code
		SwingUtilities.invokeAndWait(this::simpleTest);
		System.out.println("Valid code passed ...");
		repaintTest();
		System.out.println("Repaint test - correct code");
		// Invalid code: stack trace expected in the following
		simpleTest();
	}
}
