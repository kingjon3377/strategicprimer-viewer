package view.util;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JLabel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A label that can be written to using a PrintWriter.
 *
 * @author Jonathan Lovelace
 *
 */
public class StreamingLabel extends JLabel {
	/**
	 * Constructor, to set the background color to black.
	 */
	public StreamingLabel() {
		setBackground(Color.black);
		setOpaque(true);
	}

	/**
	 * The source for the string.
	 */
	private final StringWriter string = new StringWriter();
	/**
	 * The writer that can be printed to.
	 */
	private final PrintWriter writer = new PrintWriter(string) {
		/**
		 * Print a string and update the label.
		 *
		 * @param str the string to print
		 */
		@Override
		public void print(@Nullable final String str) {
			super.print(str);
			updateText();
		}

		/**
		 * Print a line and update the label.
		 *
		 * @param str the string to print
		 */
		@Override
		public void println(@Nullable final String str) {
			super.println(str);
			updateText();
		}
		// ESCA-JAVA0025: This is deliberate.
		/**
		 * Don't close the writer after every line!
		 */
		@Override
		public void close() {
			// Do nothing.
		}
	};

	/**
	 * @return the writer to "print" to.
	 */
	public PrintWriter getWriter() {
		return writer;
	}

	/**
	 * Update the label's text.
	 */
	public void updateText() {
		setText("<html>" + string.toString() + "</html>");
	}
}
