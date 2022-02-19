package utility;

import java.awt.Dimension;
import java.awt.Color;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JScrollPane;

import javax.swing.SwingUtilities;
import lovelace.util.StreamingLabel;
import static lovelace.util.StreamingLabel.LabelTextColor;

import java.nio.file.Path;

import drivers.common.ISPDriver;

import common.xmlio.Warning;

import drivers.gui.common.SPFrame;

/**
 * The map-checker GUI window.
 *
 * TODO: Merge into MapCheckerGUI
 */
/* package */ class MapCheckerFrame extends SPFrame {
	private static final class AutoDisposeExecutor extends WindowAdapter {
		public AutoDisposeExecutor() {
			executor = Executors.newSingleThreadExecutor();
			valid = true;
		}
		private boolean valid;
		private ExecutorService executor;

		@Override
		public void windowClosed(final WindowEvent event) {
			try {
				if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException except) {
				executor.shutdownNow();
			} finally {
				valid = false;
			}
		}

		private synchronized ExecutorService get() {
			if (!valid) {
				executor = Executors.newSingleThreadExecutor();
				valid = true;
			}
			return executor;
		}

		public void execute(Runnable task) {
			get().execute(task);
		}
	}
	public MapCheckerFrame(final ISPDriver driver) {
		super("Strategic Primer Map Checker", driver, new Dimension(640, 320), true, x -> {},
			"Map Checker");
		setBackground(Color.black);
		setContentPane(new JScrollPane(label));
		getContentPane().setBackground(Color.black);
		executor = new AutoDisposeExecutor();
		addWindowListener(executor);
	}

	private final AutoDisposeExecutor executor;
	private final StreamingLabel label = new StreamingLabel();
	private void printParagraph(final String paragraph) {
		printParagraph(paragraph, LabelTextColor.WHITE);
	}

	private void printParagraph(final String paragraph, final LabelTextColor color) {
		SwingUtilities.invokeLater(() -> label.append(String.format("<p style=\"color:%s\">%s</p>", color, paragraph)));
	}

	void customPrinter(final String string) {
		printParagraph(string, LabelTextColor.YELLOW);
	}

	private void outHandler(final String text) {
		if (text.startsWith("No errors")) {
			printParagraph(text, LabelTextColor.GREEN);
		} else {
			printParagraph(text);
		}
	}

	private void errHandler(final String text) {
		printParagraph(text, LabelTextColor.RED);
	}

	private final MapCheckerCLI mapCheckerCLI = new MapCheckerCLI(this::outHandler, this::errHandler);

	public void check(final Path filename) {
		executor.execute(() -> mapCheckerCLI.check(filename, new Warning(this::customPrinter, true)));
	}

	@Override
	public void acceptDroppedFile(final Path file) {
		check(file);
	}
}
