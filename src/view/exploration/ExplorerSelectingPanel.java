package view.exploration;

import model.exploration.ExplorationModel;
import model.exploration.ExplorationUnitListModel;
import model.exploration.PlayerListModel;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.PlayerChangeListener;
import model.listeners.PlayerChangeSource;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import view.util.BorderedPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The panel that lets the user select the unit to explore with.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public final class ExplorerSelectingPanel extends BorderedPanel implements
		ListSelectionListener, PlayerChangeSource, ActionListener,
				CompletionSource {
	/**
	 * The list of players.
	 */
	private final JList<Player> playerList;

	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<CompletionListener> cListeners = new ArrayList<>();

	/**
	 * The minimum length of the HTML wrapper.
	 */
	private static final int MIN_HTML_LEN = "<html><body></body></html>"
			                                        .length();
	/**
	 * The length of the additional HTML tags for each paragraph.
	 */
	private static final int HTML_PAR_LEN = "<p></p>".length();
	/**
	 * The list of units.
	 */
	private final JList<IUnit> unitList;
	/**
	 * The list of player-change listeners.
	 */
	private final Collection<PlayerChangeListener> listeners = new ArrayList<>();

	/**
	 * The text-field containing the running MP total.
	 */
	private final JTextField mpField = new JTextField(5);

	/**
	 * The proportion between the two sides.
	 */
	private static final double PROPORTION = 0.5;
	/**
	 * The text on the 'start exploring' button.
	 */
	private static final String BUTTON_TEXT = "Start exploring!";
	/**
	 * The exploration model.
	 */
	private final ExplorationModel model;

	/**
	 * Constructor.
	 *
	 * @param emodel the driver model
	 */
	public ExplorerSelectingPanel(final ExplorationModel emodel) {
		model = emodel;
		final PlayerListModel plmodel = new PlayerListModel(emodel);
		emodel.addMapChangeListener(plmodel);
		playerList = new JList<>(plmodel);
		playerList.addListSelectionListener(this);
		final ExplorationUnitListModel unitListModel = new ExplorationUnitListModel(
				                                                                           emodel);
		addPlayerChangeListener(unitListModel);
		unitList = new JList<>(unitListModel);
		unitList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
			final ListCellRenderer<Object> defRenderer = new DefaultListCellRenderer();
			final Component retval =
					defRenderer.getListCellRendererComponent(
							NullCleaner.assertNotNull(list), value, index,
							isSelected, cellHasFocus);
			if (value != null && retval instanceof JLabel) {
				((JLabel) retval).setText(String.format(
						"Unit of type %s, named %s",
						value.getKind(),
						value.getName()));
			}
			return NullCleaner.assertNotNull(retval);
		});
		setCenter(new SplitWithWeights(JSplitPane.HORIZONTAL_SPLIT, PROPORTION,
				                              PROPORTION, new BorderedPanel(playerList,
						                                                           label
								                                                           ("Players in all maps:"),

						                                                           null,
						                                                           null,
						                                                           null),
				                              new BorderedPanel(unitList, label(html(
						                              "Units belonging to that player:",
						                              "(Selected unit will be used for " +
								                              "exploration.)")),
						                                               new BorderedPanel
								                                               (null,
								                                                                null,
								                                                                new ListenedButton(
										                                                                                  BUTTON_TEXT,
										                                                                                  this),
								                                                                mpField,
								                                                                label("Unit's Movement Points: ")),
						                                               null, null)));
	}

	/**
	 * @param text a String
	 * @return a JLabel with that string on it
	 */
	private static JLabel label(final String text) {
		return new JLabel(text);
	}

	/**
	 * @param paras Strings, each of which should be put in its own paragraph.
	 * @return them wrapped in HTML.
	 */
	private static String html(final String... paras) {
		int len = MIN_HTML_LEN;
		for (final String para : paras) {
			len += para.length() + HTML_PAR_LEN;
		}
		final StringBuilder builder = new StringBuilder(len)
				                              .append("<html><body>");
		for (final String para : paras) {
			builder.append("<p>").append(para).append("</p>");
		}
		return NullCleaner.assertNotNull(builder.append("</body></html>")
				                                 .toString());
	}

	/**
	 * @return the model underlying the field containing the running MP total.
	 */
	public Document getMPDocument() {
		return NullCleaner.assertNotNull(mpField.getDocument());
	}

	/**
	 * Handle the user selecting a different player.
	 *
	 * @param evt event
	 */
	@Override
	public void valueChanged(@Nullable final ListSelectionEvent evt) {
		if (!playerList.isSelectionEmpty()) {
			final Player newPlayer = playerList.getSelectedValue();
			// Eclipse is mistaken when it says getSelectedValue() is @NonNull,
			// but we already checked, so it should be safe to omit the null
			// check.
			for (final PlayerChangeListener list : listeners) {
				list.playerChanged(null, newPlayer);
			}
		}
	}

	/**
	 * Handle a press of the 'start exploring' button.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent event) {
		final IUnit selectedValue = unitList.getSelectedValue();
		// Eclipse claims getSelectedValue can never return null; the
		// documentation contradicts this, but it returns null when
		// isSelectionEmpty is true, which we do check.
		if (event != null
				    && BUTTON_TEXT.equalsIgnoreCase(event.getActionCommand())
				    && !unitList.isSelectionEmpty()) {
			model.selectUnit(selectedValue);
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(true);
			}
		}
	}

	/**
	 * @param list the listener to add
	 */
	@Override
	public void addPlayerChangeListener(final PlayerChangeListener list) {
		listeners.add(list);
	}

	/**
	 * @param list the listener to remove
	 */
	@Override
	public void removePlayerChangeListener(final PlayerChangeListener list) {
		listeners.remove(list);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addCompletionListener(final CompletionListener list) {
		cListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeCompletionListener(final CompletionListener list) {
		cListeners.remove(list);
	}
}
