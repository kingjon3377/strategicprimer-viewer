package drivers.advancement;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.List;

import lovelace.util.Platform;
import static lovelace.util.BoxPanel.BoxAxis;
import lovelace.util.ListenedButton;
import lovelace.util.BoxPanel;
import lovelace.util.SimpleCardLayout;

/**
 * A panel to be the GUI to add items to a list.
 *
 * TODO: Move to lovelace.util?
 *
 * TODO: At least make a FlipPanel (JPanel laid out by CardLayout with methods
 * to flip forward and back) this can inherit from
 *
 * Or <del>try to convert back to a class now we have</del> merge with(?) SimpleCardLayout
 */
/* package */ final class ItemAdditionPanel extends JPanel implements AddRemoveSource {
	/**
	 * @param what What we're adding
	 */
	public ItemAdditionPanel(final String what) {
		this.what = what;
		layoutObj = new SimpleCardLayout(this);
		setPanelSizes(this);
		final JPanel first = new BoxPanel(BoxAxis.LineAxis);
		first.add(new ListenedButton("+", ignored -> {
				// I had wondered if Component.requestFocusInWindow() would make CardLayout
				// flip to the card containing the component, but it apparently doesn't
				// work that way.
				layoutObj.goNext();
				field.requestFocusInWindow();
			}));
		setPanelSizes(first);
		add(first);

		final JPanel second = new BoxPanel(BoxAxis.PageAxis);
		second.add(field);

		field.addActionListener(ignored -> okListener());
		field.setActionCommand("OK");

		final JPanel okPanel = new BoxPanel(BoxAxis.LineAxis);
		final JButton okButton = new ListenedButton("OK", this::okListener);
		okPanel.add(okButton);

		final JButton cancelButton = new ListenedButton("Cancel", ignored -> {
				layoutObj.goFirst();
				field.setText("");
			});

		// TODO: IIRC the Mac HIG requires OK and Cancel to be backwards from other platforms ...
		Platform.makeButtonsSegmented(okButton, cancelButton);
		okPanel.add(cancelButton);

		second.add(okPanel);
		setPanelSizes(second);
		add(second);
	}

	private final String what;

	private final List<AddRemoveListener> listeners = new ArrayList<>();

	private final JTextField field = new JTextField(10);

	@Override
	public void addAddRemoveListener(final AddRemoveListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeAddRemoveListener(final AddRemoveListener listener) {
		listeners.remove(listener);
	}

	private final SimpleCardLayout layoutObj;

	private static void setPanelSizes(final JPanel panel) {
		panel.setMinimumSize(new Dimension(60, 40));
		panel.setPreferredSize(new Dimension(80, 50));
		panel.setMaximumSize(new Dimension(90, 50));
	}

	private void okListener() {
		final String text = field.getText();
		for (final AddRemoveListener listener : listeners) {
			listener.add(what, text);
		}
		layoutObj.goFirst();
		field.setText("");
	}
}
