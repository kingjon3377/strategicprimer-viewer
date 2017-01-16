package view.worker;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.function.BiFunction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import model.listeners.PlayerChangeListener;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.ProxyUnit;
import org.eclipse.jdt.annotation.Nullable;
import util.ActionWrapper;
import util.OnMac;
import view.util.Applyable;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.HotKeyCreator;
import view.util.ListenedButton;
import view.util.Revertible;

/**
 * A panel for the user to enter a unit's orders.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class OrdersPanel extends BorderedPanel
		implements Applyable, Revertible, TreeSelectionListener, PlayerChangeListener,
						   HotKeyCreator {
	/**
	 * The source of orders (or results).
	 */
	private final OrdersSupplier supplier;
	/**
	 * The consumer of orders (or results).
	 */
	private final OrdersConsumer consumer;
	/**
	 * The text area in which the user writes the orders.
	 */
	private final JTextArea area = new JTextArea();
	/**
	 * The model for the spinner to let the user choose what turn the orders go with.
	 */
	private final SpinnerNumberModel spinnerModel;
	/**
	 * A way to get all units belonging to the current player of a given kind, for
	 * proxying.
	 */
	private final BiFunction<Player, String, List<IUnit>> proxiedFunction;
	/**
	 * The current player.
	 */
	private Player player;
	/**
	 * The current selection.
	 */
	@Nullable
	private Object sel = null;

	/**
	 * Constructor.
	 *
	 * @param currentTurn      the turn to treat as current to start with
	 * @param currentPlayer    the player whose units we start with
	 * @param proxyingFunction A way to get all units belonging to the current player
	 *                            of a
	 *                         given kind, for proxying
	 * @param ordersSupplier   the method to get the current orders for a given unit
	 *                            for a
	 *                         given turn
	 * @param ordersConsumer   the method to set the current orders for a given unit
	 *                            for a
	 *                         given turn
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	public OrdersPanel(final int currentTurn, final Player currentPlayer,
					   final BiFunction<Player, String, List<IUnit>> proxyingFunction,
					   final OrdersSupplier ordersSupplier,
					   final OrdersConsumer ordersConsumer) {
		supplier = ordersSupplier;
		// Can't use the multi-arg constructor, because of the references to
		// 'this' below.
		final int minTurn;
		if (currentTurn < 0) {
			minTurn = currentTurn;
		} else {
			minTurn = -1;
		}
		final int maxTurn;
		if (currentTurn > 100) {
			maxTurn = currentTurn;
		} else {
			maxTurn = 100;
		}
		spinnerModel = new SpinnerNumberModel(currentTurn, minTurn, maxTurn, 1);
		if (ordersConsumer == null) {
			consumer = (unit, turn, orders) -> {
			};
			setPageStart(horizontalPanel(
					new JLabel("Results for current selection, if a unit"),
					null, horizontalPanel(null,
							new JLabel("Turn "), new JSpinner(spinnerModel))))
					.setCenter(new JScrollPane(area));
		} else {
			consumer = ordersConsumer;
			final ListenedButton applyButton =
					new ListenedButton("Apply", evt -> apply());
			final ListenedButton revertButton =
					new ListenedButton("Revert", evt -> revert());
			OnMac.makeButtonsSegmented(applyButton, revertButton);
			final JPanel buttonPanel;
			if (OnMac.SYSTEM_IS_MAC) {
				buttonPanel = BoxPanel.centeredHorizBox(applyButton, revertButton);
			} else {
				buttonPanel = horizontalPanel(applyButton, null, revertButton);
			}
			final String prefix = OnMac.SHORTCUT_DESC;
			setPageStart(horizontalPanel(
					new JLabel("Orders for current selection, if a unit: (" + prefix +
									   "D)"), null,
					horizontalPanel(null, new JLabel("Turn "),
							new JSpinner(spinnerModel))))
					.setCenter(new JScrollPane(area)).setPageEnd(buttonPanel);
		}
		area.addKeyListener(new ModifiedEnterListener());
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		// Prevent synthetic access warning
		final JTextArea localArea = area;
		final int keyMask = OnMac.SHORTCUT_MASK;
		createHotKey(this, "openOrders", new ActionWrapper(evt -> {
			final boolean newlyGainingFocus = !localArea.isFocusOwner();
			localArea.requestFocusInWindow();
			if (newlyGainingFocus) {
				localArea.selectAll();
			}
		}), WHEN_IN_FOCUSED_WINDOW, KeyStroke.getKeyStroke(KeyEvent.VK_D, keyMask));
		// TODO: We really ought to support writing the orders *then* setting the turn
		spinnerModel.addChangeListener(event -> revert());
		player = currentPlayer;
		proxiedFunction = proxyingFunction;
	}

	/**
	 * If a unit is selected, change its orders to what the user wrote.
	 */
	@Override
	public void apply() {
		if (sel instanceof IUnit) {
			consumer.setOrders((IUnit) sel, spinnerModel.getNumber().intValue(),
					area.getText());
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
			area.setText(
					supplier.getOrders((IUnit) sel, spinnerModel.getNumber().intValue
																					 ()));
		} else {
			area.setText("");
		}
	}

	/**
	 * Handle a changed value in the tree.
	 * @param evt the event to handle
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
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
				proxiedFunction.apply(player, kind).forEach(proxyUnit::addProxied);
				sel = proxyUnit;
			}
			revert();
		}
	}

	/**
	 * Handle a change in the current player.
	 * @param old       the previously selected player
	 * @param newPlayer the newly selected player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		player = newPlayer;
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * A simple toString().
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "OrdersPanel for player " + player;
	}

	/**
	 * An interface for a function to give us the orders (or results) for a particular
	 * unit for a particular turn.
	 */
	@FunctionalInterface
	public interface OrdersSupplier {
		/**
		 * Get the given unit's orders for the given turn. We don't use BiFunction
		 * because this needs to have a primitive second argument.
		 * @param unit the unit whose orders (or results) are wanted
		 * @param turn the turn for which the orders (or results) are wanted
		 * @return the orders (or results) for that unit for that turn
		 */
		String getOrders(IUnit unit, int turn);
	}

	/**
	 * An interface for a method to set the orders (or results) for a particular unit
	 * for a particular turn.
	 */
	@FunctionalInterface
	public interface OrdersConsumer {
		/**
		 * Set the given unit's orders for the given turn.
		 * @param unit   the unit whose orders (or results) are being set
		 * @param turn   the turn for which the orders (or results) are being set
		 * @param orders the orders (or results) to set for that unit for that turn
		 */
		void setOrders(IUnit unit, int turn, String orders);
	}

	/**
	 * A class to listen for ctrl-enter/cmd-enter.
	 */
	private class ModifiedEnterListener extends KeyAdapter {
		/**
		 * Protected so we can call it without synthetic-access warnings.
		 */
		protected ModifiedEnterListener() {
			// do nothing
		}

		/**
		 * On Control-Enter, or Command-Enter on a Mac, calls apply().
		 *
		 * @param evt a key-press event to handle
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		public void keyPressed(@Nullable final KeyEvent evt) {
			if ((evt != null) && (evt.getKeyCode() == KeyEvent.VK_ENTER) &&
						OnMac.isHotkeyPressed(evt)) {
				apply();
			}
		}
	}
}
