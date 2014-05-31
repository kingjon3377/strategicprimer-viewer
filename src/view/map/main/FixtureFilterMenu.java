package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import model.map.TileFixture;
import model.viewer.ZOrderFilter;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A menu to let the player turn of display of kinds of fixtures.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureFilterMenu extends JMenu implements ZOrderFilter,
		ActionListener {
	/**
	 * Map from fixture classes to menu-items representing them.
	 */
	private final Map<Class<? extends TileFixture>, JCheckBoxMenuItem> mapping =
			new HashMap<>();
	/**
	 * Constructor.
	 */
	public FixtureFilterMenu() {
		super("Display ...");
		setMnemonic(KeyEvent.VK_D);
		final JMenuItem all = new JMenuItem("All");
		all.addActionListener(this);
		add(all);
		final JMenuItem none = new JMenuItem("None");
		none.addActionListener(this);
		add(none);
		addSeparator();
	}

	/**
	 * @param fix a kind of fixture. We mark it Nullable because nulls got passed
	 *        in anyway.
	 * @return whether the view should display that kind of fixture
	 */
	@Override
	public boolean shouldDisplay(@Nullable final TileFixture fix) {
		// ESCA-JAVA0177:
		if (fix == null) {
			return false; // NOPMD
		}
		final JCheckBoxMenuItem item; // NOPMD
		if (mapping.containsKey(fix.getClass())) {
			item = mapping.get(fix.getClass());
		} else if ("null".equals(fix.shortDesc())) {
			item = new JCheckBoxMenuItem(fix.plural(), false);
			mapping.put(fix.getClass(), item);
		} else {
			item = new JCheckBoxMenuItem(fix.plural(), true);
			mapping.put(fix.getClass(), item);
			add(item);
		}
		return item.isSelected();
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return; // NOPMD
		} else if ("All".equals(evt.getActionCommand())) {
			for (final JCheckBoxMenuItem item : mapping.values()) {
				item.setSelected(true);
			}
		} else if ("None".equals(evt.getActionCommand())) {
			for (final JCheckBoxMenuItem item : mapping.values()) {
				item.setSelected(false);
			}
		}
	}
}
