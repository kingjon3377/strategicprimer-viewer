package view.util;

import java.awt.Color;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JEditorPane;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A label that can be written to using a PrintWriter.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class StreamingLabel extends JEditorPane {
	/**
	 * Colors to use on a StreamingLabel. Enumerated to appease XSS-possibility warnings.
	 */
	public enum LabelTextColor {
		/**
		 * Yellow. Warnings.
		 */
		yellow,
		/**
		 * White. Normal text.
		 */
		white,
		/**
		 * Red. Errors.
		 */
		red,
		/**
		 * Green. Test passes etc.
		 */
		green
	}

	/**
	 * The writer that can be printed to.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "resource"})
	private final PrintWriter writer = new StreamingLabelWriter(new StringWriter(), this);

	/**
	 * Constructor, to set the background color to black.
	 */
	public StreamingLabel() {
		super("text/html", "<html><body bgcolor=\"#000000\"><p>&nbsp;</p></body></html>");
		setEditable(false);
		setBackground(Color.black);
		setOpaque(true);
	}

	/**
	 * A PrintWriter that wraps a StringWriter and updates a JLabel with the writer's
	 * text.
	 */
	private static final class StreamingLabelWriter extends PrintWriter {
		/**
		 * The writer to write to.
		 */
		private final StringWriter stringWriter;
		/**
		 * The label to update when we're written to.
		 */
		private final JEditorPane control;

		/**
		 * @param wrapped the writer we wrap
		 * @param label   the component to update when written to
		 */
		protected StreamingLabelWriter(final StringWriter wrapped,
									final JEditorPane label) {
			super(wrapped);
			stringWriter = wrapped;
			control = label;
		}

		/**
		 * Print a string and update the label.
		 *
		 * @param str the string to print
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
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
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		public void println(@Nullable final String str) {
			super.println(str);
			updateText();
		}

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
			control.setText("<html><body bgcolor=\"#000000\">" + stringWriter +
									"</body></html>");
		}

		/**
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public String toString() {
			return "StreamingLabelWriter";
		}

		/**
		 * @param csq a string to append
		 * @return this
		 */
		@SuppressWarnings("ReturnOfThis")
		@Override
		public StreamingLabelWriter append(@Nullable final CharSequence csq) {
			super.append(csq);
			updateText();
			return this;
		}

		/**
		 * @param format a format string
		 * @param args arguments to place in that string as per the spec
		 * @return this
		 */
		@SuppressWarnings({"ReturnOfThis", "OverloadedVarargsMethod", "resource"})
		@Override
		public PrintWriter printf(final String format, final Object @Nullable ... args) {
			super.printf(format, args);
			updateText();
			return this;
		}

	}

	/**
	 * @return the writer to "print" to.
	 */
	public PrintWriter getWriter() {
		return writer;
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
