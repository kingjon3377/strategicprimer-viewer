package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class FixtureFilterMenu extends JMenu implements ZOrderFilter, ActionListener {
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
		final JCheckBoxMenuItem item;
		if (mapping.containsKey(fix.getClass())) {
			item = mapping.get(fix.getClass());
		} else {
			item = new JCheckBoxMenuItem(fix.plural(), true);
			item.addActionListener(this);
			mapping.put(fix.getClass(), item);
			add(item);
		}
		return item.isSelected();
	}
	/**
	 * Map from fixture classes to menu-items representing them.
	 */
	private final Map<Class<? extends TileFixture>, JCheckBoxMenuItem> mapping = new HashMap<>();
	/**
	 * Handle clicks on menu items.
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if (evt.getSource() instanceof JCheckBoxMenuItem) {
//			final JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt.getSource();
//			item.setSelected(!item.isSelected());
			// FIXME: The menu item might handle this on its own!
		}
	}
}
