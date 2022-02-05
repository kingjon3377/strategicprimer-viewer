package drivers.gui.common.about;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import java.awt.Component;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
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
public class AboutDialog extends SPDialog {
	public AboutDialog(@Nullable final Component parentComponent, @Nullable final String app) throws IOException {
		super(parentComponent instanceof Frame ? (Frame) parentComponent : null, "About");
		setLayout(new BorderLayout()); // TODO: Use a BorderedPanel for contentPane
		Iterable<String> resource = FileContentsReader.readFileContents(AboutDialog.class,
			"about.html");
		StringBuilder sb = new StringBuilder();
		resource.forEach(sb::append);
		String raw = sb.toString();
		String html = raw.replaceAll("App Name Here",
			(app == null || app.isEmpty()) ? "Strategic Primer Assistive Programs" : app);
		JEditorPane pane = new JEditorPane("text/html", html);
		pane.setCaretPosition(0); // scroll to the top
		pane.setEditable(false);
		JScrollPane scrollPane;
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
		add(BoxPanel.centeredHorizontalBox(new ListenedButton("Close", (ignored) -> dispose())),
			BorderLayout.PAGE_END);
		pack();
	}
}
