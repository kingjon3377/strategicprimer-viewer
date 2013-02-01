package view.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import controller.map.drivers.AdvancementStart;
import controller.map.drivers.ISPDriver;
import controller.map.drivers.ISPDriver.DriverFailedException;
import controller.map.drivers.ViewerStart;
/**
 * A GUI to let the user choose which GUI to use.
 * @author Jonathan Lovelace
 *
 */
public class AppChooserFrame extends JFrame implements ActionListener {
	/**
	 * A list of GUIs, with how we'd describe them.
	 */
	private final Map<String, Class<? extends ISPDriver>> targets = new HashMap<String, Class<? extends ISPDriver>>();

	/**
	 * Add a target.
	 * @param desc the descriptive string
	 * @param target the class
	 */
	private void addTarget(final String desc,
			final Class<? extends ISPDriver> target) {
		targets.put(desc, target);
	}
	/**
	 * Constructor.
	 * @param params the non-option parameters passed to main().
	 */
	public AppChooserFrame(final List<String> params) {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		addTarget("Map Viewer", ViewerStart.class);
		addTarget("Worker Advancement", AdvancementStart.class);
		add(new JLabel("Please choose one of the applications below:"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		parameters = Collections.unmodifiableList(params);
		for (String string : targets.keySet()) {
			final JButton button = new JButton(string); // NOPMD
			button.addActionListener(this);
			add(button);
		}
		pack();
	}
	/**
	 * The parameters passed to our caller's main(), other than options.
	 */
	private final List<String> parameters;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(AppChooserFrame.class.getName());
	/**
	 * Handle button press.
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if (targets.containsKey(evt.getActionCommand())) {
			final Class<? extends ISPDriver> driver = targets.get(evt.getActionCommand());
			try {
				driver.newInstance().startDriver(parameters.toArray(new String[parameters.size()]));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setVisible(false);
					}
				});
			} catch (InstantiationException except) {
				LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
				ErrorShower.showErrorDialog(this, except.getMessage());
			} catch (IllegalAccessException except) {
				LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
				ErrorShower.showErrorDialog(this, except.getMessage());
			} catch (DriverFailedException except) {
				LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
				ErrorShower.showErrorDialog(this, except.getMessage());
			}
		}
	}
}
