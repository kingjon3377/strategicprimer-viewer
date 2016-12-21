package view.util;

import controller.map.drivers.AdvancementStart;
import controller.map.drivers.DriverFailedException;
import controller.map.drivers.ExplorationGUI;
import controller.map.drivers.ISPDriver;
import controller.map.drivers.SPOptions;
import controller.map.drivers.ViewerStart;
import controller.map.drivers.WorkerStart;
import controller.map.misc.CLIHelper;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.NonNull;
import util.TypesafeLogger;

/**
 * A GUI to let the user choose which GUI to use. We do *not* make this extend
 * ApplicationFrame, because it's essentially a dialog, not an app.
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
public final class AppChooserFrame extends SPFrame {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(AppChooserFrame.class);

	/**
	 * Constructor taking a driver model.
	 *
	 * @param model   the driver model
	 * @param options options to pass to the driver
	 */
	public AppChooserFrame(final IDriverModel model, final SPOptions options) {
		super("SP App Chooser", model.getMapFile());

		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.add(button("Map Viewer", model, options, ViewerStart.class));
		buttonPanel.add(button("Worker Skill Advancement", model, options,
				AdvancementStart.class));
		buttonPanel.add(button("Unit Orders and Worker Management", model, options,
				WorkerStart.class));
		buttonPanel.add(button("Exploration", model, options, ExplorationGUI.class));
		setContentPane(new BorderedPanel(new JScrollPane(buttonPanel),
												new JLabel("Please choose one of the " +
																   "applications " +
																   "below:"),
												null, null, null));
		pack();
	}

	/**
	 * Constructor.
	 *
	 * @param params  the non-option parameters passed to main().
	 * @param options options to pass to the driver
	 */
	public AppChooserFrame(final SPOptions options, final List<@NonNull String> params) {
		super("SP App Chooser", Optional.empty());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final List<@NonNull String> parameters = Collections.unmodifiableList(params);
		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.add(button("Map Viewer", parameters, options, ViewerStart.class));
		buttonPanel.add(button("Worker Skill Advancement", parameters, options,
				AdvancementStart.class));
		buttonPanel.add(button("Unit Orders and Worker Management", parameters, options,
				WorkerStart.class));
		buttonPanel.add(button("Exploration", parameters, options, ExplorationGUI
																		   .class));
		setContentPane(new BorderedPanel(new JScrollPane(buttonPanel),
												new JLabel("Please choose one of the " +
																   "applications " +
																   "below:"),
												null, null, null));
		pack();
	}

	/**
	 * Create a button for a target.
	 *
	 * @param desc    the descriptive string
	 * @param params  the parameters to pass to the chosen app
	 * @param options options to pass to the driver
	 * @param target  the class
	 * @return the button
	 */
	private JButton button(final String desc, final List<String> params,
						   final SPOptions options,
						   final Class<? extends ISPDriver> target) {
		return new ListenedButton(desc, evt -> {
			try {
				target.getConstructor().newInstance()
						.startDriver(options, params.toArray(new String[params.size()]));
			} catch (final InstantiationException | IllegalAccessException
								   | NoSuchMethodException | InvocationTargetException
								   | DriverFailedException except) {
				final String message = except.getMessage();
				LOGGER.log(Level.SEVERE, message, except.getCause());
				ErrorShower.showErrorDialog(this, message);
				return;
			}
			SwingUtilities.invokeLater(() -> {
				setVisible(false);
				dispose();
			});
		});
	}

	/**
	 * Create a button for a target.
	 *
	 * @param desc    the descriptive string
	 * @param model   the driver model to pass to the chosen app
	 * @param target  the class
	 * @param options options to pass to the driver
	 * @return the button
	 */
	private JButton button(final String desc, final IDriverModel model,
						   final SPOptions options,
						   final Class<? extends ISPDriver> target) {
		return new ListenedButton(desc, evt -> {
			try {
				target.getConstructor().newInstance()
						.startDriver(new CLIHelper(), options, model);
			} catch (final InstantiationException | IllegalAccessException
								   | NoSuchMethodException | InvocationTargetException
								   | DriverFailedException except) {
				final String message = except.getMessage();
				LOGGER.log(Level.SEVERE, message, except.getCause());
				ErrorShower.showErrorDialog(this, message);
				return;
			}
			SwingUtilities.invokeLater(() -> {
				setVisible(false);
				dispose();
			});
		});
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
	 * Returns the title of this app.
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "SP App Chooser";
	}
}
