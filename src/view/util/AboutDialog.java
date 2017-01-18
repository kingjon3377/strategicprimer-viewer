package view.util;

import controller.map.misc.WindowCloser;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

/**
 * A dialog to explain what this program is, and the sources of code and graphics.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class AboutDialog extends SPDialog {
	/**
	 * The pattern to match the placeholder in the HTML.
	 */
	private static final Pattern PATTERN =
			Pattern.compile("App Name Here");

	/**
	 * Constructor. FIXME: Credits for other images?
	 * @param parent the parent window
	 * @param app    a string describing what the application is
	 */
	@SuppressWarnings("ConditionalExpression")
	public AboutDialog(@Nullable final Component parent, final String app) {
		super((parent instanceof Frame) ? (Frame) parent : null, "About");
		setLayout(new BorderLayout());
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(AboutDialog.class.getResourceAsStream(
						"/images/about.html")))) {
			final String origHtml = reader.lines().collect(Collectors.joining());
			final String html;
			if (app.isEmpty()) {
				html = PATTERN.matcher(origHtml)
							   .replaceAll("Strategic Primer Assistive Programs");
			} else {
				html = PATTERN.matcher(origHtml).replaceAll(app);
			}
			final JEditorPane pane = new JEditorPane("text/html", html);
			pane.setCaretPosition(0); // scroll to the top
			pane.setEditable(false);
			final JScrollPane scrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setMinimumSize(new Dimension(300, 400));
			scrollPane.setPreferredSize(new Dimension(400, 500));
			add(scrollPane, BorderLayout.CENTER);
		} catch (final IOException except) {
			TypesafeLogger.getLogger(AboutDialog.class)
					.log(Level.SEVERE, "I/O error in reading About page", except);
			add(new JLabel("<html><p>We failed to read the contents of the About page. " +
								   "This should never happen.</p></html>"));
		}
		final JButton close = new JButton("Close");
		add(BoxPanel.centeredHorizBox(close), BorderLayout.SOUTH);
		close.addActionListener(new WindowCloser(this));
		pack();
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
