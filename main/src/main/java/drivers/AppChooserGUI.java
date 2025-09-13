package drivers;

import drivers.common.DriverFactory;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.UtilityGUI;
import drivers.common.cli.ICLIHelper;
import drivers.gui.common.SPFrame;
import drivers.gui.common.SPMenu;
import drivers.gui.common.WindowCloseListener;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MenuContainer;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import lovelace.util.BorderedPanel;
import lovelace.util.ListenedButton;

/* package */ final class AppChooserGUI implements UtilityGUI {
	public AppChooserGUI(final ICLIHelper cli, final SPOptions options) {
		this.cli = cli;
		this.options = options;
	}

	private final ICLIHelper cli;
	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private final Collection<String> additionalFiles = new ArrayList<>();

	private static boolean includeInGUIList(final DriverFactory driver) {
		return driver.getUsage().includeInList(IDriverUsage.DriverMode.Graphical);
	}

	@Override
	public void startDriver(final String... args) {
		final MenuContainer tempComponent = new JEditorPane();
		final Font font = tempComponent.getFont();
		final Graphics2D pen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
		final FontRenderContext context = pen.getFontRenderContext();
		double width = 0;
		double height = 10;
		final Iterable<DriverFactory> drivers = ServiceLoader.load(DriverFactory.class);
		for (final DriverFactory driver : drivers) {
			if (!includeInGUIList(driver)) {
				continue;
			}
			final Rectangle2D dimensions = font.getStringBounds(driver.getUsage().getShortDescription(), context);
			width = Double.max(width, dimensions.getWidth());
			height += dimensions.getHeight();
		}
		final SPFrame frame = new SPFrame("SP App Chooser", null, new Dimension((int) width, (int) height));
		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		final Consumer<DriverFactory> buttonHandler = (target) -> {
			new DriverWrapper(target).startCatchingErrors(cli, options,
					Stream.concat(Stream.of(args),
							additionalFiles.stream()).toArray(String[]::new));
			SwingUtilities.invokeLater(() -> {
				frame.setVisible(false);
				frame.dispose();
			});
		};
		for (final DriverFactory driver : drivers) {
			if (!includeInGUIList(driver)) {
				continue;
			}
			buttonPanel.add(new ListenedButton(driver.getUsage().getShortDescription(),
					ignored -> buttonHandler.accept(driver)));
		}
		final BorderedPanel mainPanel = BorderedPanel.verticalPanel(
				new JLabel("Please choose one of the applications below"),
				new JScrollPane(buttonPanel), null);
		frame.setContentPane(mainPanel);
		frame.pack();
		// TODO: Extract "noop" named lambda to replace newline-containing lambdas below
		frame.setJMenuBar(SPMenu.forWindow(frame,
				SPMenu.createFileMenu(new IOHandler(this, cli), AppChooserGUI.class),
				SPMenu.disabledMenu(SPMenu.createMapMenu(x -> {
				}, AppChooserGUI.class)),
				SPMenu.disabledMenu(SPMenu.createViewMenu(x -> {
				}, AppChooserGUI.class))));
		frame.addWindowListener(new WindowCloseListener(ignored -> frame.dispose()));
		frame.setVisible(true);
	}

	@Override
	public void open(final Path path) {
		additionalFiles.add(path.toString());
	}
}
