package view.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.eclipse.jdt.annotation.Nullable;
/**
 * A dialog to explain what this program is, and the sources of code and graphics.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class AboutDialog extends JDialog {
	/**
	 * @param parent the parent window
	 * @param app a string describing what the application is
	 */
	public AboutDialog(@Nullable final Component parent, final String app) {
		super(parent instanceof Frame ? (Frame) parent : null, "About");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(300, 390));
		setMinimumSize(new Dimension(300, 390));
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		final StringBuilder builder = new StringBuilder(1500 + app.length());
		builder.append("<html>");
		if (app.isEmpty()) {
			paragraph(builder, "Assistive Programs Suite");
		} else {
			paragraph(builder, app);
			paragraph(builder, "Part of the Assistive Programs Suite");
		}
		builder.append("<p>for players and Judges of ");
		link(builder, "https://shinecycle.wordpress.com/archives/strategic-primer",
				"Strategic Primer");
		builder.append("</p>");
		paragraph(builder, "Developed by Jonathan Lovelace");
		builder.append("<p>Released under the terms of ");
		link(builder, "https://www.gnu.org/licenses/gpl-3.0.en.html",
				"the GNU General Public License version 3");
		builder.append("</p>");
		builder.append("<p>Unit image by jreijonen from ");
		link(builder, "http://opengameart.org/content/faction-symbols-allies-axis",
				"OpenGameArt");
		builder.append("</p>");
		builder.append("<p>Cave image by MrBeast from ");
		link(builder, "http://opengameart.org/content/cave-tileset-0", "OpenGameArt");
		builder.append("</p>");
		paragraph(builder,
				"Minotaur, troll, and ogre images by 'www.36peas.com', licensed under CC-BY");
		builder.append(
				"<p>Window menu managed by BSD-licensed code by Jeremy Wood, downloaded from ");
		link(builder, "http://javagraphics.java.net", "javagraphics.java.net");
		builder.append("</p>");
		builder.append("<p>Pair implementation by Peter Lawrey on ");
		link(builder, "https://stackoverflow.com/a/3646398", "StackOverflow");
		builder.append("</p>");
		builder.append(
				"<p>Code to resize components given a fixed width adapted from ");
		link(builder, "http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/",
				"Nobel Joergensen");
		builder.append("</p>");
		builder.append(
				"<p>Drag-and-drop implementation uses code adapted from 'helloworld922' on the ");
		link(builder, "http://www.javaprogrammingforums.com/java-swing-tutorials/3141-drag-drop-jtrees.html",
				"Java Programming Forums");
		builder.append("</p>");
		builder.append("<p>WrapLayout taken from ");
		link(builder, "http://tips4java.wordpress.com/2008/11/06/wrap-layout/",
				"tips4java.wordpress.com");
		builder.append(", which released code to be used \"without restriction\".</p>");
		builder.append("<p>Code to make Tab work properly in combo boxes adapted from ");
		link(builder, "http://stackoverflow.com/a/24336768", "StackOverflow user Joshua Goldberg");
		builder.append("</p>");
		builder.append("</html>");
		add(new JLabel(builder.toString()));
		final JButton close = new JButton("Close");
		add(close);
		close.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		System.out.println(builder.toString());
		pack();
	}
	/**
	 * Add a link to the string.
	 * @param builder the StringBuilder to write to.
	 * @param href the target of the link
	 * @param text the text of the link
	 */
	private static void link(final StringBuilder builder, final String href, final String text) {
		builder.append("<a href=\"");
		builder.append(href);
		builder.append("\">");
		builder.append(text);
		builder.append("</a>.");
	}
	/**
	 * Add a paragraph to the string.
	 * @param builder the StringBuilder to write to
	 * @param text the text of the paragraph
	 */
	private static void paragraph(final StringBuilder builder, final String text) {
		builder.append("<p>");
		builder.append(text);
		builder.append("</p>");
	}
}
