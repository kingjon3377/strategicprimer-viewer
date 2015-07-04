package view.worker;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.PlayerChangeListener;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.workermgmt.IWorkerModel;
import util.EmptyIterator;
import util.NullCleaner;
import view.util.ApplyButtonHandler;
import view.util.Applyable;
import view.util.BorderedPanel;
import view.util.ListenedButton;

/**
 * A panel for the user to enter a unit's orders.
 *
 * @author Jonathan Lovelace
 *
 */
public class OrdersPanel extends BorderedPanel implements Applyable,
		TreeSelectionListener, PlayerChangeListener {
	/**
	 * The worker model to get units from if the user selected a kind.
	 */
	private final IWorkerModel model;
	/**
	 * The "null" player.
	 */
	private static final Player NULL_PLAYER = new Player(-1, "none");
	/**
	 * The current player.
	 */
	private Player player = NULL_PLAYER;
	/**
	 * The current selection.
	 */
	@Nullable
	private Object sel;

	/**
	 * The text area in which the user writes the orders.
	 */
	private final JTextArea area = new JTextArea();

	/**
	 * Constructor.
	 * @param wmodel the worker model
	 */
	public OrdersPanel(final IWorkerModel wmodel) {
		final ApplyButtonHandler handler = new ApplyButtonHandler(this);
		// Can't use the multi-arg constructor, because of the references to
		// 'this' below.
		setNorth(new JLabel("Orders for current selection, if a unit:"))
				.setCenter(new JScrollPane(area)).setSouth(
						new BorderedPanel().setLineStart(
								new ListenedButton("Apply", handler))
								.setLineEnd(
										new ListenedButton("Revert", handler)));
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		model = wmodel;
	}

	/**
	 * If a unit is selected, change its orders to what the user wrote.
	 */
	@Override
	public void apply() {
		if (sel instanceof IUnit) {
			final IUnit selection = (IUnit) sel;
			selection.setOrders(NullCleaner
					.assertNotNull(area.getText().trim()));
			getParent().getParent().repaint();
		}
	}

	/**
	 * Change the text in the area to either the current orders, if a unit is
	 * selected, or the empty string, if one is not.
	 */
	@Override
	public void revert() {
		if (sel instanceof IUnit) {
			area.setText(((IUnit) sel).getOrders().trim());
		} else {
			area.setText("");
		}
	}

	/**
	 * @param evt the event to handle
	 */
	@Override
	public void valueChanged(@Nullable final TreeSelectionEvent evt) {
		if (evt != null) {
			final TreePath selPath = evt.getNewLeadSelectionPath();
			if (selPath == null) {
				return;
			}
			sel = selPath.getLastPathComponent();
			if (sel instanceof DefaultMutableTreeNode) {
				sel = ((DefaultMutableTreeNode) sel).getUserObject();
			}
			if (sel instanceof String) {
				sel =
						new ProxyUnit(NullCleaner.assertNotNull((String) sel),
								model.getUnits(player), player);
			}
			revert();
		}
	}
	/**
	 * @param old the previously selected player
	 * @param newPlayer the newly selected player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		player = newPlayer;
	}

	/**
	 * A "unit" that serves as the proxy, for orders purposes, for all units of
	 * a kind.
	 */
	private static class ProxyUnit implements IUnit {
		/**
		 * The kind we're interested in.
		 */
		private final String kind;
		/**
		 * The units we might be proxying.
		 */
		private final List<IUnit> units;
		/**
		 * The owner of the units.
		 */
		private final Player owner;
		/**
		 * @param unitKind the kind of unit to proxy for
		 * @param unitsList the units among which to proxy
		 * @param playr the current player
		 */
		protected ProxyUnit(final String unitKind, final List<IUnit> unitsList,
				final Player playr) {
			kind = unitKind;
			units = unitsList;
			owner = playr;
		}
		/**
		 * @return a dummy Z-value
		 */
		@Override
		public int getZValue() {
			return 0;
		}
		/**
		 * @return "proxies"
		 */
		@Override
		public String plural() {
			return "proxies";
		}
		/**
		 * @return "proxy"
		 */
		@Override
		public String shortDesc() {
			return "proxy";
		}
		/**
		 * @return a dummy ID #
		 */
		@Override
		public int getID() {
			return -1;
		}
		/**
		 * @param fix a fixture
		 * @return whether it is this instance
		 */
		@Override
		public boolean equalsIgnoringID(final IFixture fix) {
			return this == fix;
		}
		/**
		 * @param o a fixture
		 * @return the result of comparing it to this
		 */
		@Override
		public int compareTo(@Nullable final TileFixture o) {
			if (o == null) {
				throw new IllegalArgumentException("Compared to null fixture");
			}
			return hashCode() - o.hashCode();
		}
		/**
		 * @return a dummy image filename
		 */
		@Override
		public String getDefaultImage() {
			return "proxy.png";
		}
		/**
		 * @param image ignored
		 */
		@Override
		public void setImage(final String image) {
			throw new IllegalStateException("setImage called on ProxyImage");
		}
		/**
		 * @return the same dummy image filename
		 */
		@Override
		public String getImage() {
			return "proxy.png";
		}
		/**
		 * @return the specified kind
		 */
		@Override
		public String getKind() {
			return kind;
		}
		/**
		 * @param nKind ignored
		 */
		@Override
		public void setKind(final String nKind) {
			throw new IllegalStateException("setKind called on ProxyImage");
		}
		/**
		 * @return an empty iterator
		 */
		@Override
		public Iterator<UnitMember> iterator() {
			return new EmptyIterator<>();
		}
		/**
		 * @return a dummy name
		 */
		@Override
		public String getName() {
			return "proxy";
		}
		/**
		 * @param nomen ignored
		 */
		@Override
		public void setName(final String nomen) {
			throw new IllegalStateException("setName called on ProxyUnit");
		}
		/**
		 * @return the specified owner
		 */
		@Override
		public Player getOwner() {
			return owner;
		}
		/**
		 * @param player ignored
		 */
		@Override
		public void setOwner(final Player player) {
			throw new IllegalStateException("setOwner called on ProxyUnit");
		}
		/**
		 * @param obj ignored
		 * @param ostream the stream to write the error message to
		 * @param context the context in which to write the error message
		 */
		@Override
		public boolean isSubset(final IUnit obj, final Appendable ostream,
				final String context) throws IOException {
			ostream.append(context);
			ostream.append("\tisSubset called on ProxyUnit\n");
			return false;
		}

		/**
		 * @return the orders that every unit of this kind shares, or the empty
		 *         string if not all share the same orders.
		 */
		@Override
		public String getOrders() {
			String retval = null;
			for (final IUnit unit : units) {
				if (!kind.equals(unit.getKind())) {
					continue;
				} else if (retval == null) {
					retval = unit.getOrders();
				} else if (!retval.equals(unit.getOrders())) {
					return "";
				}
			}
			if (retval == null) {
				return "";
			} else {
				return retval;
			}
		}
		/**
		 * @param newOrders orders to set on every unit with this kind.
		 */
		@Override
		public void setOrders(final String newOrders) {
			for (final IUnit unit : units) {
				if (kind.equals(unit.getKind())) {
					unit.setOrders(newOrders);
				}
			}
		}
		/**
		 * @return "proxy"
		 */
		@Override
		public String verbose() {
			return "proxy";
		}
		/**
		 * TODO: We should probably throw, or at least log, an exception when this is called
		 * @param member ignored
		 */
		@Override
		public void addMember(final UnitMember member) {
			// Do nothing
		}
		/**
		 * TODO: We should probably throw, or at least log, an exception when this is called
		 * @param member ignored
		 */
		@Override
		public void removeMember(final UnitMember member) {
			// Do nothing
		}
	}
}
