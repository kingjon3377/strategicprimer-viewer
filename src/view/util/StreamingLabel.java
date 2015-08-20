package view.util;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JLabel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A label that can be written to using a PrintWriter.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class StreamingLabel extends JLabel {
	/**
	 * The source for the string.
	 */
	private final StringWriter string = new StringWriter();

	/**
	 * The writer that can be printed to.
	 */
	private final PrintWriter writer = new StreamingLabelWriter(string, this);

	/**
	 * Constructor, to set the background color to black.
	 */
	public StreamingLabel() {
		setBackground(Color.black);
		setOpaque(true);
	}

	/**
	 * A PrintWriter that wraps a StringWriter and updates a JLabel with the
	 * writer's text.
	 */
	private static class StreamingLabelWriter extends PrintWriter {
		/**
		 * The writer to write to.
		 */
		private final StringWriter swriter;
		/**
		 * The label to update when we're written to.
		 */
		private final JLabel control;
		/**
		 * @param wrapped the writer we wrap
		 * @param label the label to update when written to
		 */
		protected StreamingLabelWriter(final StringWriter wrapped,
				final JLabel label) {
			super(wrapped);
			swriter = wrapped;
			control = label;
		}
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
		/**
		 * Update the label's text.
		 */
		private void updateText() {
			control.setText("<html>" + swriter.toString() + "</html>");
		}
	}
	/**
	 * @return the writer to "print" to.
	 */
	public PrintWriter getWriter() {
		return writer;
	}
}
