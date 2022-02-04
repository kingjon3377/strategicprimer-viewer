package drivers;

import drivers.common.DriverFactory;
import drivers.common.SPOptions;
import drivers.common.UtilityGUI;
import drivers.common.cli.ICLIHelper;
import drivers.gui.common.SPFrame;
import drivers.gui.common.SPMenu;
import drivers.gui.common.WindowCloseListener;
import drivers.map_viewer.ViewerGUIFactory;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import lovelace.util.BorderedPanel;
import lovelace.util.ListenedButton;

/* package */ class AppChooserGUI implements UtilityGUI {
	private static final Logger LOGGER = Logger.getLogger(AppChooserGUI.class.getName());
	public AppChooserGUI(ICLIHelper cli, SPOptions options) {
		this.cli = cli;
		this.options = options;
	}

	private final ICLIHelper cli;
	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private final List<String> additionalFiles = new ArrayList<>();

	boolean includeInGUIList(DriverFactory driver) {
		return driver.getUsage().includeInList(true);
	}

	@Override
	public void startDriver(String... args) {
		final JEditorPane tempComponent = new JEditorPane();
		final Font font = tempComponent.getFont();
		final Graphics2D pen = (Graphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
		final FontRenderContext context = pen.getFontRenderContext();
		// FIXME: Should probably calculate both of these as doubles, then *maybe* convert at the last moment.
		int width = 0;
		int height = 10;
		Iterable<DriverFactory> drivers = ServiceLoader.load(DriverFactory.class);
		for (DriverFactory driver : drivers) {
			if (!includeInGUIList(driver)) {
				continue;
			}
			final Rectangle2D dimensions = font.getStringBounds(driver.getUsage().getShortDescription(), context);
			width = Integer.max(width, (int) dimensions.getWidth());
			height += (int) dimensions.getHeight();
		}
		final SPFrame frame = new SPFrame("SP App Chooser", this, new Dimension(width, height));
		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		Consumer<DriverFactory> buttonHandler = (target) -> {
				new DriverWrapper(target).startCatchingErrors(cli, options,
					Stream.concat(Stream.of(args),
						additionalFiles.stream()).toArray(String[]::new));
				SwingUtilities.invokeLater(() -> {
						frame.setVisible(false);
						frame.dispose();
					});
			};
		for (DriverFactory driver : drivers) {
			buttonPanel.add(new ListenedButton(driver.getUsage().getShortDescription(),
				ignored -> buttonHandler.accept(driver)));
		}
		BorderedPanel mainPanel = BorderedPanel.verticalPanel(
			new JLabel("Please choose one of the applications below"),
			new JScrollPane(buttonPanel), null);
		frame.setContentPane(mainPanel);
		frame.pack();
		frame.setJMenuBar(SPMenu.forWindowContaining(mainPanel,
			SPMenu.createFileMenu(new IOHandler(this, new ViewerGUIFactory()::createDriver, cli)::actionPerformed, this),
			SPMenu.disabledMenu(SPMenu.createMapMenu(x -> {}, this)),
			SPMenu.disabledMenu(SPMenu.createViewMenu(x -> {}, this))));
		frame.addWindowListener(new WindowCloseListener(ignored -> frame.dispose()));
		frame.setVisible(true);
	}

	@Override
	public void open(Path path) {
		additionalFiles.add(path.toString());
	}
}
