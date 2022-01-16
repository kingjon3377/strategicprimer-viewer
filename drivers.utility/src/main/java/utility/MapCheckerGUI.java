package utility;

import java.nio.file.Path;
import java.nio.file.Paths;
import drivers.common.UtilityGUI;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.SPMenu;
import drivers.gui.common.UtilityMenuHandler;
import java.util.stream.Stream;
import java.util.Objects;

/**
 * A driver to check every map file in a list for errors and report the results in a window.
 */
public class MapCheckerGUI implements UtilityGUI {
	private MapCheckerFrame window; // FIXME: Initialize this more safely now we're not working around Ceylon's 'this' restrictions
	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	private boolean initialized = false;

	private static final <T> void noop(T t) {}

	@Override
	public void startDriver(String... args) {
		if (!initialized) {
			initialized = true;
			window = new MapCheckerFrame(this);
			window.setJMenuBar(SPMenu.forWindowContaining(window.getContentPane(),
				SPMenu.createFileMenu(
					new UtilityMenuHandler(this, window)::handleEvent, this),
				SPMenu.disabledMenu(SPMenu.createMapMenu(MapCheckerGUI::noop, this)),
				SPMenu.disabledMenu(SPMenu.createViewMenu(MapCheckerGUI::noop, this))));
			window.addWindowListener(new WindowCloseListener(arg -> window.dispose()));
		}
		window.showWindow();
		Stream.of(args).filter(Objects::nonNull).map(Paths::get).forEach(window::check);
	}

	@Override
	public void open(Path path) {
		window.check(path);
	}
}
