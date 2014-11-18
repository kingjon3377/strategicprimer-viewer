package view.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.eclipse.jdt.annotation.Nullable;
/**
 * A dialog to explain what this program is, and the sources of code and graphics.
 * @author Jonathan Lovelace
 *
 */
public class AboutDialog extends JDialog {
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
		final StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		if (app.isEmpty()) {
			builder.append("<p>Assistive Programs Suite</p>");
		} else {
			builder.append("<p>");
			builder.append(app);
			builder.append("</p><p>Part of the Assistive Programs Suite</p>");
		}
		builder.append(
				"<p>for players and Judges of "
						+ "<a href=\"https://shinecycle.wordpress.com/archives/strategic-primer/\">Strategic Primer</a>.</p>");
		builder.append("<p>Developed by Jonathan Lovelace</p>");
		builder.append(
				"<p>Unit image by jreijonen from <a href=\"http://opengameart.org/content/faction-symbols-allies-axis\">OpenGameArt</a></p>");
		builder.append("<p>Cave image by MrBeast from <a href=\"http://opengameart.org/content/cave-tileset-0\">OpenGameArt</a></p>");
		builder.append("<p>Code managing the Window menu adapted from BSD-licensed code by Jeremy Wood, downloaded from <a href=\"http://javagraphics.java.net\">javagraphics.java.net</a></p>");
		builder.append("<p>Pair implementation by Peter Lawrey on <a href=\"https://stackoverflow.com/a/3646398\">StackOverflow</a>.</p>");
		builder.append(
				"<p>Code to resize components given a fixed width adapted from <a href=\"http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/\">Nobel Joergensen</a>.</p>");
		builder.append("<p>Drag-and-drop implementation uses code adapted from 'helloworld922' on the <a href=\"http://www.javaprogrammingforums.com/java-swing-tutorials/3141-drag-drop-jtrees.html\">Java Programming Forums</a>.</p>");
		builder.append("</html>");
		add(new JLabel(builder.toString()));
		final JButton close = new JButton("Close");
		add(close);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		pack();
	}
}
