package view.map.main;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import model.map.TileFixture;
import model.viewer.ZOrderFilter;
/**
 * A menu to let the player turn of display of kinds of fixtures.
 * @author Jonathan Lovelace
 *
 */
public class FixtureFilterMenu extends JMenu implements ZOrderFilter {
	/**
	 * Constructor.
	 */
	public FixtureFilterMenu() {
		super("Display ...");
	}
	/**
	 * @param fix a kind of fixture
	 * @return whether the view should display that kind of fixture
	 */
	@Override
	public boolean shouldDisplay(final TileFixture fix) {
		// ESCA-JAVA0177:
		final JCheckBoxMenuItem item;
		if (mapping.containsKey(fix.getClass())) {
			item = mapping.get(fix.getClass());
		} else {
			item = new JCheckBoxMenuItem(fix.plural(), true);
			mapping.put(fix.getClass(), item);
			add(item);
		}
		return item.isSelected();
	}
	/**
	 * Map from fixture classes to menu-items representing them.
	 */
	private final Map<Class<? extends TileFixture>, JCheckBoxMenuItem> mapping = new HashMap<>();
}
