package utility;

import java.nio.file.Path;
import java.nio.file.Paths;

import drivers.common.UtilityGUI;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.SPMenu;
import drivers.gui.common.UtilityMenuHandler;

import java.util.stream.Stream;

/**
 * A driver to check every map file in a list for errors and report the results in a window.
 */
public final class MapCheckerGUI implements UtilityGUI {
	public MapCheckerGUI() {
		window = new MapCheckerFrame(this);
		window.setJMenuBar(SPMenu.forWindow(window,
				SPMenu.createFileMenu(
						new UtilityMenuHandler(this, window)::handleEvent, this),
				SPMenu.disabledMenu(SPMenu.createMapMenu(MapCheckerGUI::noop, this)),
				SPMenu.disabledMenu(SPMenu.createViewMenu(MapCheckerGUI::noop, this))));
		window.addWindowListener(new WindowCloseListener(arg -> window.dispose()));
	}

	private final MapCheckerFrame window;

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	@SuppressWarnings("EmptyMethod")
	private static <T> void noop(final T t) {
	}

	@Override
	public void startDriver(final String... args) {
		window.showWindow();
		Stream.of(args).map(Paths::get).forEach(window::check);
	}

	@Override
	public void open(final Path path) {
		window.check(path);
	}
}
