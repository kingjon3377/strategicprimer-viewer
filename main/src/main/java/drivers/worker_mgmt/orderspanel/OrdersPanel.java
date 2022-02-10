package drivers.worker_mgmt.orderspanel;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import lovelace.util.BoxPanel;
import lovelace.util.ListenedButton;

import lovelace.util.BorderedPanel;
import static lovelace.util.MenuUtils.createHotKey;
import lovelace.util.Platform;
import common.map.Player;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.ProxyUnit;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.KeyEvent;

import java.awt.Color;
import java.util.function.BiFunction;

public class OrdersPanel extends BorderedPanel implements OrdersContainer {
	private static final Color LIGHT_BLUE = new Color(135, 206, 250);
	private Player currentPlayer;

	private final BiFunction<Player, String, Collection<IUnit>> playerUnits;

	@FunctionalInterface
	public static interface IOrdersSupplier { // TODO: Move elsewhere; TODO: just use BiFunction<IUnit, Integer, String>? (See what callers try to pass in)
		String getOrders(IUnit unit, int turn);
	}

	private final IOrdersSupplier ordersSupplier;

	@FunctionalInterface
	public static interface IOrdersConsumer { // TODO: Move elsewhere
		void accept(IUnit unit, int turn, String orders);
	}

	private final @Nullable IOrdersConsumer ordersConsumer;

	@FunctionalInterface
	public static interface IIsCurrent { // TODO: Move elsewhere; rename
		boolean isCurrent(IUnit unit, int turn);
	}

	private final IIsCurrent isCurrent;

	private final SpinnerNumberModel spinnerModel;
	private final Color defaultColor;
	private final JTextArea area = new JTextArea();

	public OrdersPanel(final String description, final int currentTurn, final Player currentPlayer,
	                   final BiFunction<Player, String, Collection<IUnit>> playerUnits,
	                   final IOrdersSupplier ordersSupplier, @Nullable final IOrdersConsumer ordersConsumer,
	                   final IIsCurrent isCurrent) {
		this.currentPlayer = currentPlayer;
		this.playerUnits = playerUnits;
		this.ordersSupplier = ordersSupplier;
		this.ordersConsumer = ordersConsumer;
		this.isCurrent = isCurrent;

		final int minimumTurn = (currentTurn < 0) ? currentTurn : -1;
		final int maximumTurn = Math.max(currentTurn, 100);
		spinnerModel = new SpinnerNumberModel(currentTurn, minimumTurn, maximumTurn, 1);
		defaultColor = area.getBackground();

		spinnerModel.addChangeListener(ignored -> revert());

		area.addKeyListener(new EnterListener(this::apply));

		createHotKey(area, "openOrders", ignored -> focusOnArea(),
			JComponent.WHEN_IN_FOCUSED_WINDOW,
			KeyStroke.getKeyStroke(KeyEvent.VK_D, Platform.SHORTCUT_MASK));

		final String topLabel;
		if ("Orders".equals(description)) {
			topLabel = String.format("Orders for current selection, if a unit: (%sD)",
				Platform.SHORTCUT_DESCRIPTION);
		} else {
			topLabel = description + " for current selection, if a unit:";
		}

		setPageStart(BorderedPanel.horizontalPanel(new JLabel(topLabel), null,
			BorderedPanel.horizontalPanel(null, new JLabel("Turn "),
				new JSpinner(spinnerModel))));
		if (ordersConsumer != null) {
			final JButton applyButton = new ListenedButton("Apply", this::apply);
			final JButton revertButton = new ListenedButton("Revert", this::revert);
			Platform.makeButtonsSegmented(applyButton, revertButton);

			final JPanel buttonPanel;
			if (Platform.SYSTEM_IS_MAC) {
				buttonPanel = BoxPanel.centeredHorizontalBox(revertButton, applyButton);
			} else {
				buttonPanel = BorderedPanel.horizontalPanel(revertButton, null,
					applyButton);
			}
			setPageEnd(buttonPanel);
		}

		setCenter(new JScrollPane(area));
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
	}

	@Nullable
	private Object selection = null;

	private void fixColor() {
		if (selection instanceof IUnit && !isCurrent.isCurrent((IUnit) selection,
				spinnerModel.getNumber().intValue())) {
			area.setBackground(LIGHT_BLUE);
		} else {
			area.setBackground(defaultColor);
		}
	}

	/**
	 * If a unit is selected, change its orders to what the user wrote.
	 */
	@Override
	public void apply() {
		if (selection instanceof IUnit) {
			if (ordersConsumer != null) {
				ordersConsumer.accept((IUnit) selection,
					spinnerModel.getNumber().intValue(),
					area.getText());
			}
			fixColor();
			getParent().getParent().repaint();
		}
	}

	/**
	 * Change the text in the area to either the current orders, if a unit
	 * is selected, or the empty string, if one is not.
	 */
	@Override
	public void revert() {
		if (selection instanceof IUnit) {
			area.setEnabled(true);
			area.setText(ordersSupplier.getOrders((IUnit) selection,
				spinnerModel.getNumber().intValue()));
		} else {
			area.setEnabled(false);
			area.setText("");
		}
		fixColor();
	}

	/**
	 * Handle a changed value in the tree.
	 */
	@Override
	public void valueChanged(final TreeSelectionEvent event) {
		final TreePath selectedPath = event.getNewLeadSelectionPath();
		if (selectedPath != null) {
			final Object sel = selectedPath.getLastPathComponent();
			final Object temp;
			if (sel instanceof DefaultMutableTreeNode) {
				temp = ((DefaultMutableTreeNode) sel).getUserObject();
			} else {
				temp = sel;
			}
			if (temp instanceof String) {
				final ProxyUnit proxyUnit = new ProxyUnit((String) temp);
				playerUnits.apply(currentPlayer, (String) temp)
					.forEach(proxyUnit::addProxied);
				selection = proxyUnit;
			} else {
				selection = temp;
			}
			revert();
		}
	}

	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		currentPlayer = newPlayer;
	}

	public void focusOnArea() {
		final boolean newlyGainingFocus = !area.isFocusOwner();
		area.requestFocusInWindow();
		fixColor();
		if (newlyGainingFocus) {
			area.selectAll();
		}
	}

	@Override
	public boolean selectText(final String selection) {
		final String text = area.getText().toLowerCase();
		final int index = text.indexOf(selection.toLowerCase());
		if (index < 0) {
			return false;
		} else {
			area.requestFocusInWindow();
			area.setCaretPosition(index);
			area.moveCaretPosition(index + selection.length());
			return true;
		}
	}
}
