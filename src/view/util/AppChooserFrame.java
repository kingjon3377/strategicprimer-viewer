package view.util;

import controller.map.drivers.AdvancementStart;
import controller.map.drivers.DriverFailedException;
import controller.map.drivers.ExplorationGUI;
import controller.map.drivers.ISPDriver;
import controller.map.drivers.ViewerStart;
import controller.map.drivers.WorkerStart;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.TypesafeLogger;

/**
 * A GUI to let the user choose which GUI to use. We do *not* make this extend
 * ApplicationFrame, because it's essentially a dialog, not an app.
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
public final class AppChooserFrame extends JFrame {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(AppChooserFrame.class);
	/**
	 * Create a button for a target.
	 *
	 * @param desc   the descriptive string
	 * @param params the parameters to pass to the chosen app
	 * @param target the class
	 * @return the button
	 */
	private JButton button(final String desc, final List<String> params,
						   final Class<? extends ISPDriver> target) {
		return new ListenedButton(desc, evt -> {
			try {
				target.getConstructor().newInstance()
						.startDriver(params.toArray(new String[params.size()]));
			} catch (final InstantiationException | IllegalAccessException
					               | NoSuchMethodException | InvocationTargetException
					               | DriverFailedException except) {
				final String msg = except.getMessage();
				final String message = NullCleaner.valueOrDefault(msg,
						"Exception with null message");
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
	 * @param desc   the descriptive string
	 * @param model  the driver model to pass to the chosen app
	 * @param target the class
	 * @return the button
	 */
	private JButton button(final String desc, final IDriverModel model,
						   final Class<? extends ISPDriver> target) {
		return new ListenedButton(desc, evt -> {
			try {
				target.getConstructor().newInstance().startDriver(model);
			} catch (final InstantiationException | IllegalAccessException
					               | NoSuchMethodException | InvocationTargetException
					               | DriverFailedException except) {
				final String msg = except.getMessage();
				final String message = NullCleaner.valueOrDefault(msg,
						"Exception with null message");
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
	 * Constructor taking a driver model.
	 *
	 * @param model the driver model
	 */
	public AppChooserFrame(final IDriverModel model) {
		super("SP App Chooser");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.add(button("Map Viewer", model, ViewerStart.class));
		buttonPanel.add(button("Worker Skill Advancement", model,
				AdvancementStart.class));
		buttonPanel.add(button("Unit Orders and Worker Management", model,
				WorkerStart.class));
		buttonPanel.add(button("Exploration", model, ExplorationGUI.class));
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
	 * @param params the non-option parameters passed to main().
	 */
	public AppChooserFrame(final List<@NonNull String> params) {
		super("SP App Chooser");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final List<@NonNull String> parameters =
				NullCleaner.assertNotNull(Collections.unmodifiableList(params));
		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.add(button("Map Viewer", parameters, ViewerStart.class));
		buttonPanel.add(button("Worker Skill Advancement", parameters,
				AdvancementStart.class));
		buttonPanel.add(button("Unit Orders and Worker Management", parameters,
				WorkerStart.class));
		buttonPanel.add(button("Exploration", parameters, ExplorationGUI.class));
		setContentPane(new BorderedPanel(new JScrollPane(buttonPanel),
												new JLabel("Please choose one of the " +
																   "applications " +
																   "below:"),
												null, null, null));
		pack();
	}

}
