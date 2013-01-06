package view.map.main;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.map.HasKind;
import model.map.HasName;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.towns.Fortress;
import model.viewer.MapModel;
import model.viewer.PointIterator;
import util.IsNumeric;
import util.IteratorWrapper;
import view.util.SystemOut;
/**
 * A dialog to let the user find fixtures by ID, name, or "kind".
 * @author Jonathan Lovelace
 *
 */
public class FindDialog extends JDialog implements ActionListener {
	/**
	 * The text field holding the search string.
	 */
	private final JTextField search = new JTextField("", 20);
	/**
	 * The map model to change the selection in.
	 */
	private final MapModel map;
	/**
	 * A label to display error messages.
	 */
	private final JLabel errorLabel = new JLabel(
			"This text should vanish from the error-message label before the constructor ends.");
	/**
	 * Constructor.
	 *
	 * @param parent the parent to attach this dialog to
	 * @param model the map model to change the selection in
	 */
	public FindDialog(final Frame parent, final MapModel model) {
		super(parent);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		add(search);
		errorLabel.setText("");
		errorLabel.setMinimumSize(new Dimension(200, 15));
		errorLabel.setAlignmentX(CENTER_ALIGNMENT);
		errorLabel.setAlignmentY(LEFT_ALIGNMENT);
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createHorizontalGlue());
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalGlue());
		add(buttonPanel);
		map = model;
		pack();
	}
	/**
	 *
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("OK".equals(event.getActionCommand())) {
			search();
		}
		setVisible(false);
	}

	/**
	 * Search for the current pattern. If the pattern is found (as the ID of a
	 * fixture, or the name of a hasName, or the kind of a hasKind), select the
	 * tile containing the thing found. If the pattern is the empty string, don't search.
	 */
	public void search() {
		final String pattern = search.getText();
		if (pattern.isEmpty()) {
			return; // NOPMD
		}
		int id = Integer.MIN_VALUE;
		if (IsNumeric.isNumeric(pattern)) {
			id = Integer.parseInt(pattern);
		}
		final Iterable<Point> iter = new IteratorWrapper<Point>(new PointIterator(map, true));
		for (Point point : iter) {
			final Tile tile = map.getMainMap().getTile(point);
			for (final TileFixture fix : tile) {
				if (matches(pattern, id, fix)) {
					SystemOut.SYS_OUT.print("Found in point");
					SystemOut.SYS_OUT.println(point);
					map.setSelection(point);
					return;
				}
			}
		}
	}
	/**
	 * @param pattern a pattern
	 * @param id either MIN_INT, or (if pattern is numeric) its numeric equivalent
	 * @param fix a fixture
	 * @return whether the fixture matches the pattern or has id as its ID.
	 */
	private static boolean matches(final String pattern, final int id, final TileFixture fix) {
		if (fix.getID() == id
				|| (fix instanceof HasName && ((HasName) fix).getName()
						.contains(pattern))
				|| (fix instanceof HasKind && ((HasKind) fix).getKind()
						.contains(pattern))) {
			return true; // NOPMD
		} else if (fix instanceof Fortress) {
			for (Unit unit : ((Fortress) fix).getUnits()) {
				if (matches(pattern, id, unit)) {
					return true; // NOPMD
				}
			}
			return false; // NOPMD
		} else if (fix instanceof Unit) {
			for (UnitMember member : (Unit) fix) {
				// FIXME: Make a SearchableFixture supertype of TileFixture, UnitMember, and Worker, and search on *that* instead.
				if (member instanceof TileFixture && matches(pattern, id, (TileFixture) member)) {
					return true; // NOPMD
				} else if (member instanceof Worker) {
					final Worker worker = (Worker) member;
					if (worker.getID() == id
							|| worker.getName().contains(pattern)
							|| worker.getRace().contains(pattern)) {
						return true; // NOPMD
					}
				}
			}
			return false; // NOPMD
		}
		return false;
	}
}
