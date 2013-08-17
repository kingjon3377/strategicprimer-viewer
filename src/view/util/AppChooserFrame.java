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

import controller.map.drivers.AdvancementStart;
import controller.map.drivers.ISPDriver;
import controller.map.drivers.ISPDriver.DriverFailedException;
import controller.map.drivers.ViewerStart;
import controller.map.drivers.WorkerStart;
/**
 * A GUI to let the user choose which GUI to use.
 * @author Jonathan Lovelace
 *
 */
public class AppChooserFrame extends JFrame {
	/**
	 * Create a button for a target.
	 * @param desc the descriptive string
	 * @param params the parameters to pass to the chosen app
	 * @param target the class
	 * @return the button
	 */
	private JButton button(final String desc, final List<String> params,
			final Class<? extends ISPDriver> target) {
		final JButton button = new JButton(desc);
		button.addActionListener(new AppChoiceListener(target, params, this));
		return button;
	}
	/**
	 * Constructor.
	 * @param params the non-option parameters passed to main().
	 */
	public AppChooserFrame(final List<String> params) {
		super("SP App Chooser");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final BorderedPanel contentPane = new BorderedPanel();
		contentPane.setNorth(new JLabel("Please choose one of the applications below:"));
		final List<String> parameters = Collections.unmodifiableList(params);
		final JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.add(button("Map Viewer", parameters, ViewerStart.class));
		buttonPanel.add(button("Worker Skill Advancement", parameters, AdvancementStart.class));
		buttonPanel.add(button("Unit Orders and Worker Management", parameters, WorkerStart.class));
		contentPane.setCenter(new JScrollPane(buttonPanel));
		setContentPane(contentPane);
		pack();
	}
	/**
	 * A class to start the selected app.
	 */
	private static class AppChoiceListener implements ActionListener, Runnable {
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
		 * @param frame the app to start if invoked
		 * @param parameters the parameters to pass to it
		 * @param acf the app-chooser frame, so we can close it when something is selected.
		 */
		AppChoiceListener(final Class<? extends ISPDriver> frame,
				final List<String> parameters, final AppChooserFrame acf) {
			app = frame;
			params = parameters.toArray(new String[parameters.size()]);
			outer = acf;
		}
		/**
		 * Handle button press.
		 * @param evt the event to handle
		 */
		@Override
		public void actionPerformed(final ActionEvent evt) {
			try {
				app.newInstance().startDriver(params);
			} catch (InstantiationException except) {
				LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
				ErrorShower.showErrorDialog(outer, except.getMessage());
			} catch (IllegalAccessException except) {
				LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
				ErrorShower.showErrorDialog(outer, except.getMessage());
			} catch (DriverFailedException except) {
				LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
				ErrorShower.showErrorDialog(outer, except.getMessage());
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
		 * Logger.
		 */
		private static final Logger LOGGER = Logger.getLogger(AppChooserFrame.class.getName());
	}
}
