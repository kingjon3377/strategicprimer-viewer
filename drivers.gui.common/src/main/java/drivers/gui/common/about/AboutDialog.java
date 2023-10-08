package drivers.gui.common.about;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import java.awt.Component;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollPane;

import lovelace.util.ListenedButton;
import lovelace.util.BoxPanel;
import lovelace.util.Platform;
import drivers.gui.common.SPDialog;
import lovelace.util.FileContentsReader;

/**
 * A dialog to explain what this program is, and the sources of code and graphics.
 *
 * FIXME: Credits for other images?
 */
public final class AboutDialog extends SPDialog {
	private static final long serialVersionUID = 1L;
	private static final Pattern APP_NAME = Pattern.compile("App Name Here");

	public AboutDialog(final @Nullable Component parentComponent, final @Nullable String app) throws IOException {
		super(parentComponent instanceof Frame f ? f : null, "About");
		setLayout(new BorderLayout()); // TODO: Use a BorderedPanel for contentPane
		final Iterable<String> resource = FileContentsReader.readFileContents(AboutDialog.class,
			"about.html");
		final StringBuilder sb = new StringBuilder();
		resource.forEach(sb::append);
		final String raw = sb.toString();
		final String html = APP_NAME.matcher(raw).replaceAll(Optional.ofNullable(app)
			.filter(Predicate.not(String::isEmpty)).orElse("Strategic Primer Assistive Programs"));
		final JEditorPane pane = new JEditorPane("text/html", html);
		pane.setCaretPosition(0); // scroll to the top
		pane.setEditable(false);
		final JScrollPane scrollPane;
		if (Platform.SYSTEM_IS_MAC) {
			scrollPane = new JScrollPane(pane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		} else {
			scrollPane = new JScrollPane(pane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		scrollPane.setMinimumSize(new Dimension(300, 400));
		scrollPane.setPreferredSize(new Dimension(400, 500));
		add(scrollPane, BorderLayout.CENTER);
		add(BoxPanel.centeredHorizontalBox(new ListenedButton("Close", this::dispose)),
			BorderLayout.PAGE_END);
		pack();
	}
}
