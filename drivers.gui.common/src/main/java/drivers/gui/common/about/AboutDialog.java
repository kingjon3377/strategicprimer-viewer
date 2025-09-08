package drivers.gui.common.about;

import org.jspecify.annotations.Nullable;

import java.io.IOException;

import java.awt.Component;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Pattern APP_NAME = Pattern.compile("App Name Here");
	private static final String DIALOG_TITLE = "About";
	public static final String APP_SUITE_TITLE = "Strategic Primer Assistive Programs";
	public static final String CLOSE_BUTTON = "Close";

	public AboutDialog(final @Nullable Component parentComponent, final @Nullable String app) throws IOException {
		super(parentComponent instanceof final Frame f ? f : null, DIALOG_TITLE);
		setLayout(new BorderLayout()); // TODO: Use a BorderedPanel for contentPane
		final String raw = FileContentsReader.streamFileContents(AboutDialog.class, Paths.get("about.html"))
				.collect(Collectors.joining());
		final String html = APP_NAME.matcher(raw).replaceAll(Optional.ofNullable(app)
				.filter(Predicate.not(String::isEmpty)).orElse(APP_SUITE_TITLE));
		final JScrollPane scrollPane = createMainPanel(html);
		add(scrollPane, BorderLayout.CENTER);
		add(BoxPanel.centeredHorizontalBox(new ListenedButton(CLOSE_BUTTON, this::dispose)),
				BorderLayout.PAGE_END);
		pack();
	}

	private static JScrollPane createMainPanel(final String html) {
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
		return scrollPane;
	}
}
