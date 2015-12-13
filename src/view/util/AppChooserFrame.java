package view.util;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import controller.map.drivers.AdvancementStart;
import controller.map.drivers.ExplorationGUI;
import controller.map.drivers.ISPDriver;
import controller.map.drivers.ISPDriver.DriverFailedException;
import controller.map.drivers.ViewerStart;
import controller.map.drivers.WorkerStart;
import model.misc.IDriverModel;
import util.NullCleaner;
import util.TypesafeLogger;

/**
 * A GUI to let the user choose which GUI to use. We do *not* make this extend
 * ApplicationFrame, because it's essentially a dialog, not an app.
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
public final class AppChooserFrame extends JFrame {
	/**
	 * Create a button for a target.
	 *
	 * @param desc the descriptive string
	 * @param params the parameters to pass to the chosen app
	 * @param target the class
	 * @return the button
	 */
	private JButton button(final String desc, final List<String> params,
			final Class<? extends ISPDriver> target) {
		return new ListenedButton(desc, new AppChoiceListenerArgs(target, params,
				this));
	}
	/**
	 * Create a button for a target.
	 *
	 * @param desc the descriptive string
	 * @param model the driver model to pass to the chosen app
	 * @param target the class
	 * @return the button
	 */
	private JButton button(final String desc, final IDriverModel model,
			final Class<? extends ISPDriver> target) {
		return new ListenedButton(desc, new AppChoiceListenerDriverModel(target, model,
				this));
	}
	/**
	 * Constructor taking a driver model.
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
				new JLabel("Please choose one of the applications below:"),
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
				new JLabel("Please choose one of the applications below:"),
				null, null, null));
		pack();
	}

	/**
	 * A class to start the selected app with originally-command-line arguments.
	 * @author Jonathan Lovelace
	 */
	private static final class AppChoiceListenerArgs implements ActionListener, Runnable {
		/**
		 * Logger for the inner class.
		 */
		private static final Logger LOGGER = TypesafeLogger
				.getLogger(AppChoiceListenerArgs.class);
		/**
		 * The app to start.
		 */
		private final Class<? extends ISPDriver> app;
		/**
		 * The parameters.
		 */
		private final String[] params;
		/**
		 * The app-chooser frame. So we can close it properly.
		 */
		private final AppChooserFrame outer;

		/**
		 * Constructor.
		 *
		 * @param frame the app to start if invoked
		 * @param parameters the parameters to pass to it
		 * @param acf the app-chooser frame, so we can close it when something
		 *        is selected.
		 */
		protected AppChoiceListenerArgs(final Class<? extends ISPDriver> frame,
				final List<String> parameters, final AppChooserFrame acf) {
			app = frame;
			final String[] array = parameters.toArray(new String[parameters.size()]);
			if (array == null) {
				throw new IllegalStateException(
						"Array created for parameters is null");
			}
			params = array;
			outer = acf;
		}

		/**
		 * Handle button press.
		 *
		 * @param evt the event to handle
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent evt) {
			try {
				app.newInstance().startDriver(params);
			} catch (InstantiationException | IllegalAccessException
					| DriverFailedException except) {
				final String msg = except.getMessage();
				final String message = NullCleaner.valueOrDefault(msg,
						"Exception with null message");
				LOGGER.log(Level.SEVERE, message, except.getCause());
				ErrorShower.showErrorDialog(outer, message);
				return;
			}
			SwingUtilities.invokeLater(this);
		}

		/**
		 * Close the frame.
		 */
		@Override
		public void run() {
			outer.setVisible(false);
			outer.dispose();
		}

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "AppChoiceListenerArgs";
		}
	}
	/**
	 * A class to start the selected app with originally-command-line arguments.
	 * @author Jonathan Lovelace
	 */
	private static final class AppChoiceListenerDriverModel implements ActionListener, Runnable {
		/**
		 * Logger for the inner class.
		 */
		private static final Logger LOGGER = TypesafeLogger
				.getLogger(AppChoiceListenerArgs.class);
		/**
		 * The app to start.
		 */
		private final Class<? extends ISPDriver> app;
		/**
		 * The driver model.
		 */
		private final IDriverModel model;
		/**
		 * The app-chooser frame. So we can close it properly.
		 */
		private final AppChooserFrame outer;

		/**
		 * Constructor.
		 *
		 * @param frame the app to start if invoked
		 * @param dmodel the driver model to pass to it
		 * @param acf the app-chooser frame, so we can close it when something
		 *        is selected.
		 */
		protected AppChoiceListenerDriverModel(final Class<? extends ISPDriver> frame,
				final IDriverModel dmodel, final AppChooserFrame acf) {
			app = frame;
			outer = acf;
			model = dmodel;
		}

		/**
		 * Handle button press.
		 *
		 * @param evt the event to handle
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent evt) {
			try {
				app.newInstance().startDriver(model);
			} catch (InstantiationException | IllegalAccessException
					| DriverFailedException except) {
				final String msg = except.getMessage();
				final String message = NullCleaner.valueOrDefault(msg,
						"Exception with null message");
				LOGGER.log(Level.SEVERE, message, except.getCause());
				ErrorShower.showErrorDialog(outer, message);
				return;
			}
			SwingUtilities.invokeLater(this);
		}

		/**
		 * Close the frame.
		 */
		@Override
		public void run() {
			outer.setVisible(false);
			outer.dispose();
		}

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "AppChoiceListenerDriverModel";
		}
	}
}
