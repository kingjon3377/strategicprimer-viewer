package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import model.listeners.PlayerChangeListener;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.ProxyUnit;
import model.workermgmt.IWorkerModel;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import view.util.Applyable;
import view.util.BorderedPanel;
import view.util.ListenedButton;
import view.util.Revertible;

/**
 * A panel for the user to enter a unit's orders.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class OrdersPanel extends BorderedPanel implements Applyable, Revertible,
																		TreeSelectionListener,
																		PlayerChangeListener {
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
	 *
	 * @param wmodel the worker model
	 */
	public OrdersPanel(final IWorkerModel wmodel) {
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
		setPageStart(
				new JLabel(
								  "Orders for current selection, if a unit: ("
										  + prefix
										  + "D)")).setCenter(new JScrollPane(area))
				.setPageEnd(new BorderedPanel()
									.setLineStart(new ListenedButton("Apply",
																			evt -> apply()))
									.setLineEnd(
											new ListenedButton("Revert", evt -> revert())));
		area.addKeyListener(new KeyAdapter() {
			private boolean isModifierPressed(final KeyEvent evt) {
				if (onMac) {
					return evt.isMetaDown();
				} else {
					return evt.isControlDown();
				}
			}

			@Override
			public void keyPressed(@Nullable final KeyEvent evt) {
				if ((evt != null) && (evt.getKeyCode() == KeyEvent.VK_ENTER)
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
		assert (inputMap != null) && (actionMap != null);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, keyMask), "openOrders");
		actionMap.put("openOrders", new AbstractAction() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent evt) {
				final boolean newlyGainingFocus = !area.isFocusOwner();
				area.requestFocusInWindow();
				if (newlyGainingFocus) {
					area.selectAll();
				}
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
	 * selected, or
	 * the empty string, if one is not.
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
				final String kind = (String) sel;
				final ProxyUnit proxyUnit = new ProxyUnit(kind);
				model.getUnits(player, kind).forEach(proxyUnit::addProxied);
				sel = proxyUnit;
			}
			revert();
		}
	}

	/**
	 * @param old       the previously selected player
	 * @param newPlayer the newly selected player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		player = newPlayer;
	}

}
