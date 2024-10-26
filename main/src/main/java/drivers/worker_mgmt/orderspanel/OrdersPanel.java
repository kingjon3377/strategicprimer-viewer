package drivers.worker_mgmt.orderspanel;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
import legacy.map.Player;
import legacy.map.fixtures.mobile.IUnit;

import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.KeyEvent;

import java.awt.Color;
import java.util.function.BiFunction;

public final class OrdersPanel extends BorderedPanel implements OrdersContainer {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Color LIGHT_BLUE = new Color(135, 206, 250);
	private Player currentPlayer;

	private final BiFunction<Player, String, Collection<IUnit>> playerUnits;

	@FunctionalInterface
	// TODO: Move elsewhere; TODO: just use BiFunction<IUnit, Integer, String>? (See what callers try to pass in)
	public interface IOrdersSupplier {
		String getOrders(IUnit unit, int turn);
	}

	private final IOrdersSupplier ordersSupplier;

	@FunctionalInterface
	public interface IOrdersConsumer { // TODO: Move elsewhere
		void accept(IUnit unit, int turn, String orders);
	}

	private final @Nullable IOrdersConsumer ordersConsumer;

	@FunctionalInterface
	public interface IIsCurrent { // TODO: Move elsewhere; rename
		boolean isCurrent(IUnit unit, int turn);
	}

	private final IIsCurrent isCurrent;

	private final SpinnerNumberModel spinnerModel;
	private final Color defaultColor;
	private final JTextArea area = new JTextArea();

	@SuppressWarnings("MagicConstant")
	public OrdersPanel(final String description, final int currentTurn, final Player currentPlayer,
	                   final BiFunction<Player, String, Collection<IUnit>> playerUnits,
	                   final IOrdersSupplier ordersSupplier, final @Nullable IOrdersConsumer ordersConsumer,
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
			topLabel = "Orders for current selection, if a unit: (%sD)".formatted(Platform.SHORTCUT_DESCRIPTION);
		} else {
			topLabel = description + " for current selection, if a unit:";
		}

		setPageStart(BorderedPanel.horizontalPanel(new JLabel(topLabel), null,
				BorderedPanel.horizontalPanel(null, new JLabel("Turn "),
						new JSpinner(spinnerModel))));
		if (!Objects.isNull(ordersConsumer)) {
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

	private @Nullable Object selection = null;

	private void fixColor() {
		switch (selection) {
			case final IUnit sel when !isCurrent.isCurrent(sel,
					spinnerModel.getNumber().intValue()) -> area.setBackground(LIGHT_BLUE);
			case final String sel -> {
				final int turn = spinnerModel.getNumber().intValue();
				for (final IUnit unit : playerUnits.apply(currentPlayer, sel)) {
					if (!isCurrent.isCurrent(unit, turn)) {
						area.setBackground(LIGHT_BLUE);
						return;
					}
				}
				area.setBackground(defaultColor);
			}
			case null, default -> area.setBackground(defaultColor);
		}
	}

	/**
	 * If a unit is selected, change its orders to what the user wrote.
	 */
	@Override
	public void apply() {
		switch (selection) {
			case final IUnit sel -> {
				if (!Objects.isNull(ordersConsumer)) {
					ordersConsumer.accept(sel,
							spinnerModel.getNumber().intValue(),
							area.getText());
				}
				fixColor();
				getParent().getParent().repaint();
			}
			case final String sel -> {
				if (!Objects.isNull(ordersConsumer)) {
					final int turn = spinnerModel.getNumber().intValue();
					for (final IUnit unit : playerUnits.apply(currentPlayer, sel)) {
						ordersConsumer.accept(unit, turn, area.getText());
					}
				}
				fixColor();
				getParent().getParent().repaint();
			}
			case null, default -> {
			}
		}
	}

	/**
	 * Change the text in the area to either the current orders, if a unit
	 * is selected, or the empty string, if one is not.
	 */
	@Override
	public void revert() {
		switch (selection) {
			case final IUnit sel -> {
				area.setEnabled(true);
				area.setText(ordersSupplier.getOrders(sel,
						spinnerModel.getNumber().intValue()));
			}
			case final String sel -> {
				area.setEnabled(true);
				@Nullable String orders = null;
				final int turn = spinnerModel.getNumber().intValue();
				for (final IUnit unit : playerUnits.apply(currentPlayer, sel)) {
					if (Objects.isNull(orders)) {
						orders = ordersSupplier.getOrders(unit, turn);
					} else if (!orders.equals(ordersSupplier.getOrders(unit, turn))) {
						area.setText("");
						fixColor();
						return;
					}
				}
				area.setText(Optional.ofNullable(orders).orElse(""));
			}
			case null, default -> {
				area.setEnabled(false);
				area.setText("");
			}
		}
		fixColor();
	}

	/**
	 * Handle a changed value in the tree.
	 */
	@Override
	public void valueChanged(final TreeSelectionEvent event) {
		final TreePath selectedPath = event.getNewLeadSelectionPath();
		if (!Objects.isNull(selectedPath)) {
			final Object sel = selectedPath.getLastPathComponent();
			if (sel instanceof final DefaultMutableTreeNode dmtn) {
				selection = dmtn.getUserObject();
			} else {
				selection = sel;
			}
			revert();
		}
	}

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
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
