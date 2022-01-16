package utility;

import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JScrollPane;

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
	public MapCheckerFrame(ISPDriver driver) {
		super("Strategic Primer Map Checker", driver, new Dimension(640, 320), true, x -> {},
			"Map Checker");
		this.driver = driver;
		setBackground(Color.white);
		setContentPane(new JScrollPane(label));
		getContentPane().setBackground(Color.white);
	}

	private final ISPDriver driver;
	private final StreamingLabel label = new StreamingLabel();
	private void printParagraph(String paragraph) {
		printParagraph(paragraph, LabelTextColor.BLACK);
	}

	private void printParagraph(String paragraph, LabelTextColor color) {
		label.append(String.format("<p style=\"color:%s\">%s</p>", color, paragraph));
	}

	void customPrinter(String string) {
		printParagraph(string, LabelTextColor.YELLOW);
	}

	private void outHandler(String text) {
		if (text.startsWith("No errors")) {
			printParagraph(text, LabelTextColor.GREEN);
		} else {
			printParagraph(text);
		}
	}

	private void errHandler(String text) {
		printParagraph(text, LabelTextColor.RED);
	}

	private final MapCheckerCLI mapCheckerCLI = new MapCheckerCLI(this::outHandler, this::errHandler);

	public void check(Path filename) {
		mapCheckerCLI.check(filename, new Warning(this::customPrinter, true));
	}

	@Override
	public void acceptDroppedFile(Path file) {
		check(file);
	}
}
