package swinghelper;

import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * This class is used to detect Event Dispatch Thread rule violations.
 * <p>
 * See <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How to Use Threads</a> for more info
 * <p>
 * This is a modification of original idea of Scott Delap
 * <p>
 * Initial version of ThreadCheckingRepaintManager can be found here:
 * <a href="http://www.clientjava.com/blog/2004/08/20/1093059428000.html">Easily
 * Find Swing Threading Mistakes</a>
 *
 * @author Scott Delap
 * @author Alexander Potochkin
 * @author https://swinghelper.dev.java.net/
 */
public final class CheckThreadViolationRepaintManager extends RepaintManager {
	/**
	 * Report a violation
	 *
	 * @param c          the component in which the violation was found
	 * @param stackTrace the stack trace to report
	 */
	static void violationFound(final JComponent c, final StackTraceElement... stackTrace) {
		System.out.println();
		System.out.println("EDT violation detected");
		System.out.println(c);
		for (final StackTraceElement st : stackTrace) {
			System.out.print("\tat ");
			System.out.println(st.toString());
		}
	}

	public enum CheckType {
		Complete, Partial;
		public boolean isComplete() {
			return Complete == this;
		}
	}

	/**
	 * Whether to run the complete check.
	 */
	private final CheckType checkType;

	/**
	 * The last(-referenced?) component.
	 */
	private @Nullable WeakReference<JComponent> lastComponent;

	/**
	 * @param shouldCompleteCheck Whether to run the complete check (recommended)
	 */
	public CheckThreadViolationRepaintManager(final CheckType checkType) {
		this.checkType = checkType;
		lastComponent = null;
	}

	public CheckThreadViolationRepaintManager() {
		this(CheckType.Complete);
	}

	/**
	 * Check thread violations for a component.
	 */
	void checkThreadViolations(final JComponent c) {
		if (!SwingUtilities.isEventDispatchThread() && (checkType.isComplete() || c.isShowing())) {
			boolean repaint = false;
			boolean fromSwing = false;
			boolean imageUpdate = false;
			final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (final StackTraceElement st : stackTrace) {
				if (repaint && st.getClassName().startsWith("javax.swing")) {
					fromSwing = true;
				}
				if (repaint && "imageUpdate".equals(st.getMethodName())) {
					imageUpdate = true;
				}
				if ("repaint".equals(st.getMethodName())) {
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
			if (!Objects.isNull(lastComponent) && c == lastComponent.get()) {
				return;
			}
			lastComponent = new WeakReference<>(c);
			violationFound(c, stackTrace);
		}
	}

	/**
	 * Add (and check) an invalid component
	 *
	 * @param component the component to add
	 */
	@Override
	public synchronized void addInvalidComponent(final @Nullable JComponent component) {
		if (!Objects.isNull(component)) {
			checkThreadViolations(component);
			super.addInvalidComponent(component);
		}
	}

	/**
	 * Add a dirty region.
	 *
	 * @param component the component involved
	 * @param x         the X coordinate of the dirty region
	 * @param y         the Y coordinate of the dirty region
	 * @param w         the width of the dirty region
	 * @param h         the height of the dirty region
	 */
	@Override
	public void addDirtyRegion(final @Nullable JComponent component,
							   final int x, final int y, final int w, final int h) {
		if (!Objects.isNull(component)) {
			checkThreadViolations(component);
			super.addDirtyRegion(component, x, y, w, h);
		}
	}
}
