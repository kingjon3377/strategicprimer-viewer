package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.PlayerChangeListener;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.workermgmt.IWorkerModel;
import util.EmptyIterator;
import util.NullCleaner;
import view.util.ApplyButtonHandler;
import view.util.Applyable;
import view.util.BorderedPanel;
import view.util.ListenedButton;

/**
 * A panel for the user to enter a unit's orders.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class OrdersPanel extends BorderedPanel implements Applyable,
		TreeSelectionListener, PlayerChangeListener {
	/**
	 * The worker model to get units from if the user selected a kind.
	 */
	private final IWorkerModel model;
	/**
	 * The "null" player.
	 */
	private static final Player NULL_PLAYER = new Player(-1, "none");
	/**
	 * The current player.
	 */
	private Player player = NULL_PLAYER;
	/**
	 * The current selection.
	 */
	@Nullable
	private Object sel = null;

	/**
	 * The text area in which the user writes the orders.
	 */
	private final JTextArea area = new JTextArea();

	/**
	 * Constructor.
	 * @param wmodel the worker model
	 */
	public OrdersPanel(final IWorkerModel wmodel) {
		final ApplyButtonHandler handler = new ApplyButtonHandler(this);
		// Can't use the multi-arg constructor, because of the references to
		// 'this' below.
		final boolean onMac = System.getProperty("os.name").toLowerCase()
				.startsWith("mac os x");
		final String prefix;
		final int keyMask;
		if (onMac) {
			prefix = "\u2318";
			keyMask = InputEvent.META_DOWN_MASK;
		} else {
			prefix = "Ctrl+";
			keyMask = InputEvent.CTRL_DOWN_MASK;
		}
		setNorth(
				new JLabel(
						"Orders for current selection, if a unit: ("
								+ prefix
								+ "D)")).setCenter(new JScrollPane(area))
										.setSouth(new BorderedPanel()
												.setLineStart(new ListenedButton("Apply",
														handler))
										.setLineEnd(
												new ListenedButton("Revert", handler)));
		area.addKeyListener(new KeyAdapter() {
			private boolean isModifierPressed(final KeyEvent evt) {
				if (onMac && evt.isMetaDown()) {
					return true;
				} else if (!onMac && evt.isControlDown()) {
					return true;
				} else {
					return false;
				}
			}
			@Override
			public void keyPressed(@Nullable final KeyEvent evt) {
				if (evt != null && evt.getKeyCode() == KeyEvent.VK_ENTER
						&& isModifierPressed(evt)) {
					apply();

				}
			}
		});
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		model = wmodel;
		final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		final ActionMap actionMap = getActionMap();
		assert (inputMap != null && actionMap != null);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, keyMask), "openOrders");
		actionMap.put("openOrders", new AbstractAction() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent evt) {
				area.requestFocusInWindow();
			}
		});
	}

	/**
	 * If a unit is selected, change its orders to what the user wrote.
	 */
	@Override
	public void apply() {
		if (sel instanceof IUnit) {
			final IUnit selection = (IUnit) sel;
			selection.setOrders(NullCleaner
					.assertNotNull(area.getText().trim()));
			getParent().getParent().repaint();
		}
	}

	/**
	 * Change the text in the area to either the current orders, if a unit is
	 * selected, or the empty string, if one is not.
	 */
	@Override
	public void revert() {
		if (sel instanceof IUnit) {
			area.setText(((IUnit) sel).getOrders().trim());
		} else {
			area.setText("");
		}
	}

	/**
	 * @param evt the event to handle
	 */
	@Override
	public void valueChanged(@Nullable final TreeSelectionEvent evt) {
		if (evt != null) {
			final TreePath selPath = evt.getNewLeadSelectionPath();
			if (selPath == null) {
				return;
			}
			sel = selPath.getLastPathComponent();
			if (sel instanceof DefaultMutableTreeNode) {
				sel = ((DefaultMutableTreeNode) sel).getUserObject();
			}
			if (sel instanceof String) {
				sel =
						new ProxyUnit(NullCleaner.assertNotNull((String) sel),
								model.getUnits(player), player);
			}
			revert();
		}
	}
	/**
	 * @param old the previously selected player
	 * @param newPlayer the newly selected player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		player = newPlayer;
	}

	/**
	 * A "unit" that serves as the proxy, for orders purposes, for all units of
	 * a kind.
	 *
	 * FIXME: This should probably be removed in favor of the top-level ProxyUnit class.
	 */
	private static final class ProxyUnit implements IUnit {
		/**
		 * The kind we're interested in.
		 */
		private final String kind;
		/**
		 * The units we might be proxying.
		 */
		private final List<IUnit> units;
		/**
		 * The owner of the units.
		 */
		private final Player owner;
		/**
		 * @param unitKind the kind of unit to proxy for
		 * @param unitsList the units among which to proxy
		 * @param playr the current player
		 */
		protected ProxyUnit(final String unitKind, final List<IUnit> unitsList,
				final Player playr) {
			kind = unitKind;
			units = unitsList;
			owner = playr;
		}
		/**
		 * @return a copy of this proxy
		 * @param zero whether to "zero out" sensitive information
		 */
		@Override
		public IUnit copy(final boolean zero) {
			return new ProxyUnit(kind, units.stream().map(unit -> unit.copy(zero)).collect(Collectors.toList()),
					                    owner);
		}
		/**
		 * @return a dummy Z-value
		 */
		@Override
		public int getZValue() {
			return 0;
		}
		/**
		 * @return "proxies"
		 */
		@Override
		public String plural() {
			return "proxies";
		}
		/**
		 * @return "proxy"
		 */
		@Override
		public String shortDesc() {
			return "proxy";
		}
		/**
		 * @param fix A TileFixture to compare to
		 *
		 * @return the result of the comparison
		 */
		@Override
		public int compareTo(final TileFixture fix) {
			return fix.hashCode() - hashCode();
		}

		/**
		 * @return a dummy ID #
		 */
		@Override
		public int getID() {
			return -1;
		}
		/**
		 * @param fix a fixture
		 * @return whether it is this instance
		 */
		@Override
		public boolean equalsIgnoringID(final IFixture fix) {
			return this == fix;
		}
		/**
		 * @return a dummy image filename
		 */
		@Override
		public String getDefaultImage() {
			return "proxy.png";
		}
		/**
		 * @param image ignored
		 */
		@Override
		public void setImage(final String image) {
			throw new IllegalStateException("setImage called on ProxyImage");
		}
		/**
		 * @return the same dummy image filename
		 */
		@Override
		public String getImage() {
			return "proxy.png";
		}
		/**
		 * @return the specified kind
		 */
		@Override
		public String getKind() {
			return kind;
		}
		/**
		 * @param nKind ignored
		 */
		@Override
		public void setKind(final String nKind) {
			throw new IllegalStateException("setKind called on ProxyImage");
		}
		/**
		 * @return an empty iterator
		 */
		@Override
		public Iterator<UnitMember> iterator() {
			return new EmptyIterator<>();
		}
		/**
		 * @return a dummy name
		 */
		@Override
		public String getName() {
			return "proxy";
		}
		/**
		 * @param nomen ignored
		 */
		@Override
		public void setName(final String nomen) {
			throw new IllegalStateException("setName called on ProxyUnit");
		}
		/**
		 * @return the specified owner
		 */
		@Override
		public Player getOwner() {
			return owner;
		}
		/**
		 * @param owner ignored
		 */
		@Override
		public void setOwner(final Player owner) {
			throw new IllegalStateException("setOwner called on ProxyUnit");
		}
		/**
		 * @param obj ignored
		 * @param ostream the stream to write the error message to
		 * @param context the context in which to write the error message
		 * @throws IOException on error writing to stream
		 * @return false
		 */
		@Override
		public boolean isSubset(final IFixture obj, final Appendable ostream,
				final String context) throws IOException {
			ostream.append(context);
			ostream.append("\tisSubset called on ProxyUnit\n");
			return false;
		}

		/**
		 * @return the orders that every unit of this kind shares, or the empty
		 *         string if not all share the same orders.
		 */
		@Override
		public String getOrders() {
			String retval = null;
			for (final IUnit unit : units) {
				if (!kind.equals(unit.getKind())) {
					continue;
				} else if (retval == null) {
					retval = unit.getOrders();
				} else if (!retval.equals(unit.getOrders())) {
					return "";
				}
			}
			if (retval == null) {
				return "";
			} else {
				return retval;
			}
		}
		/**
		 * @param newOrders orders to set on every unit with this kind.
		 */
		@Override
		public void setOrders(final String newOrders) {
			units.stream().filter(unit -> kind.equals(unit.getKind())).forEach(unit -> unit.setOrders(newOrders));
		}
		/**
		 * @return "proxy"
		 */
		@Override
		public String verbose() {
			return "proxy";
		}

		/**
		 * TODO: We should probably throw, or at least log, an exception when
		 * this is called.
		 *
		 * @param member
		 *            ignored
		 */
		@Override
		public void addMember(final UnitMember member) {
			// Do nothing
		}

		/**
		 * TODO: We should probably throw, or at least log, an exception when
		 * this is called.
		 *
		 * @param member
		 *            ignored
		 */
		@Override
		public void removeMember(final UnitMember member) {
			// Do nothing
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "OrdersPanel#ProxyUnit";
		}
	}
}
