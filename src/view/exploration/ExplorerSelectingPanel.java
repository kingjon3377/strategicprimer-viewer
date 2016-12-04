package view.exploration;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;
import javax.swing.text.Document;
import model.exploration.ExplorationUnitListModel;
import model.exploration.IExplorationModel;
import model.exploration.PlayerListModel;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.PlayerChangeListener;
import model.listeners.PlayerChangeSource;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import view.util.BorderedPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

/**
 * The panel that lets the user select the unit to explore with.
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
public final class ExplorerSelectingPanel extends BorderedPanel implements
		PlayerChangeSource, CompletionSource {
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
	 * The proportion between the two sides.
	 */
	private static final double PROPORTION = 0.5;
	/**
	 * The text on the 'start exploring' button.
	 */
	private static final String BUTTON_TEXT = "Start exploring!";
	/**
	 * The list of players.
	 */
	private final JList<Player> playerList;
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<CompletionListener> cListeners = new ArrayList<>();
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
	 * The exploration model.
	 */
	private final IExplorationModel model;

	/**
	 * Constructor.
	 *
	 * @param explorationModel the driver model
	 */
	public ExplorerSelectingPanel(final IExplorationModel explorationModel) {
		model = explorationModel;
		final PlayerListModel playerListModel = new PlayerListModel(explorationModel);
		explorationModel.addMapChangeListener(playerListModel);
		playerList = new JList<>(playerListModel);
		playerList.addListSelectionListener(evt -> {
			if (!playerList.isSelectionEmpty()) {
				final Player newPlayer = playerList.getSelectedValue();
				// Eclipse is mistaken when it says getSelectedValue() is @NonNull,
				// but we already checked, so it should be safe to omit the null
				// check.
				for (final PlayerChangeListener list : listeners) {
					list.playerChanged(null, newPlayer);
				}
			}
		});
		final ExplorationUnitListModel unitListModel =
				new ExplorationUnitListModel(explorationModel);
		addPlayerChangeListener(unitListModel);
		unitList = new JList<>(unitListModel);
		unitList.setCellRenderer((final JList<? extends @NonNull IUnit> list,
								  @Nullable final IUnit value, final int index,
								  final boolean isSelected, final boolean cellHasFocus)
										 -> {
			final ListCellRenderer<@Nullable Object> defRenderer =
					new DefaultListCellRenderer();
			final Component retval =
					defRenderer.getListCellRendererComponent(
							NullCleaner.assertNotNull(list), value, index,
							isSelected, cellHasFocus);
			if ((value != null) && (retval instanceof JLabel)) {
				((JLabel) retval).setText(String.format(
						"Unit of type %s, named %s",
						value.getKind(),
						value.getName()));
			}
			return NullCleaner.assertNotNull(retval);
		});
		final ActionListener buttonListener = evt -> {
			final IUnit selectedValue =
					unitList.getSelectedValue();
			if (!unitList.isSelectionEmpty()) {
				model.selectUnit(selectedValue);
				cListeners.forEach(CompletionListener::finished);
			}
		};
		mpField.addActionListener(buttonListener);
		setCenter(SplitWithWeights.horizontalSplit(PROPORTION, PROPORTION,
				verticalPanel(label("Players in all maps:"), playerList, null),
				verticalPanel(
						label(html("Units belonging to that player:",
								"(Selected unit will be used for exploration.)")),
						new JScrollPane(unitList), verticalPanel(null,
								horizontalPanel(label("Unit's Movement Points"), null,
										mpField),
								new ListenedButton(BUTTON_TEXT, buttonListener)))));
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
		final StringBuilder builder = new StringBuilder(len).append("<html><body>");
		for (final String para : paras) {
			builder.append("<p>").append(para).append("</p>");
		}
		return NullCleaner.assertNotNull(builder.append("</body></html>").toString());
	}

	/**
	 * @return the model underlying the field containing the running MP total.
	 */
	public Document getMPDocument() {
		return NullCleaner.assertNotNull(mpField.getDocument());
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
	 * Prevent serialization
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
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "ExplorerSelectingPanel including " + unitList.getModel().getSize() +
					   " units";
	}
}
