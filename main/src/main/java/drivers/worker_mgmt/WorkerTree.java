package drivers.worker_mgmt;

import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.stream.StreamSupport;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.function.Function;
import java.awt.image.BufferedImage;
import common.map.fixtures.UnitMember;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Color;
import common.map.fixtures.mobile.worker.ProxyWorker;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.IJob;
import common.map.HasImage;
import common.map.IFixture;
import common.map.HasMutableKind;
import common.map.Player;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeModelListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.ToolTipManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.DropMode;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalPlurals;
import drivers.map_viewer.ImageLoader;
import drivers.map_viewer.FixtureEditMenu;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import worker.common.IWorkerTreeModel;
import common.idreg.IDRegistrar;
import java.util.function.ToIntFunction;
import java.util.function.IntSupplier;
import java.util.Collections;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Consumer;
import org.javatuples.Pair;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A tree of a player's units.
 */
public class WorkerTree extends JTree implements UnitMemberSelectionSource, UnitSelectionSource {
	private static final Logger LOGGER = Logger.getLogger(WorkerTree.class.getName());
	// TODO: Move into the inner class that uses this
	private static final DefaultTreeCellRenderer DEFAULT_STORER = new DefaultTreeCellRenderer();
	private final List<Pair<String, ToIntFunction<WorkerStats>>> statReferencesList =
		Collections.unmodifiableList(Arrays.asList(Pair.with("Str", WorkerStats::getStrength),
			Pair.with("Dex", WorkerStats::getDexterity),
			Pair.with("Con", WorkerStats::getConstitution),
			Pair.with("Int", WorkerStats::getIntelligence),
			Pair.with("Wis", WorkerStats::getWisdom),
			Pair.with("Cha", WorkerStats::getCharisma)));

	private IWorkerTreeModel wtModel;

	/**
	 * @param wtModel The tree model
	 * @param players The players in the map
	 * @param turnSource How to get the current turn
	 * @param orderCheck Whether we should visually warn if orders contain
	 * substrings indicating remaining work or if a unit named "unassigned"
	 * is nonempty TODO: enum instead of boolean
	 * @param idf The factory to use to generate ID numbers.
	 */
	public WorkerTree(IWorkerTreeModel wtModel, Iterable<Player> players, IntSupplier turnSource,
			boolean orderCheck, IDRegistrar idf) {
		// TODO: Assign such parameters as are actually used outside initialization to fields.
		setModel(wtModel);
		this.wtModel = wtModel;
		setRootVisible(false);
		setDragEnabled(true);
		setShowsRootHandles(true);
		setDropMode(DropMode.ON);

		setTransferHandler(new WorkerTreeTransferHandler(wtModel, selectionModel, this::isExpanded,
			this::collapsePath));

		setCellRenderer(new UnitMemberCellRenderer(wtModel, turnSource, orderCheck));
		addTreeSelectionListener(this::treeSelectionChanged);

		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}

		wtModel.addTreeModelListener(new TreeModelHandler(this)); // was 'tml'; TODO: do we need it after this?

		wtModel.addTreeModelListener((TreeModelListener) getAccessibleContext());

		ToolTipManager.sharedInstance().registerComponent(this);
		addMouseListener(new TreeMouseListener(this, players, wtModel, idf));
	}

	private static Function<Pair<String, ToIntFunction<WorkerStats>>, String> statHelper(
			WorkerStats stats) {
		return pair -> String.format("%s %s", pair.getValue0(),
			WorkerStats.getModifierString(pair.getValue1().applyAsInt(stats)));
	}

	@Override
	@Nullable
	public String getToolTipText(MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1) {
			return null;
		}
		WorkerStats stats = Optional.ofNullable(getPathForLocation(event.getX(), event.getY()))
			.map(TreePath::getLastPathComponent).map(wtModel::getModelObject)
			.filter(IWorker.class::isInstance).map(IWorker.class::cast)
			.map(IWorker::getStats).orElse(null);
		if (stats == null) {
			return null;
		} else {
			return statReferencesList.stream().map(statHelper(stats))
				.collect(Collectors.joining(", ", "<html><p>", "</p></html>"));
		}
	}

	private final List<UnitSelectionListener> selectionListeners = new ArrayList<>();
	private final List<UnitMemberListener> memberListeners = new ArrayList<>();

	@Override
	public void addUnitMemberListener(UnitMemberListener listener) {
		memberListeners.add(listener);
	}

	@Override
	public void addUnitSelectionListener(UnitSelectionListener listener) {
		selectionListeners.add(listener);
	}

	@Override
	public void removeUnitMemberListener(UnitMemberListener listener) {
		memberListeners.remove(listener);
	}

	@Override
	public void removeUnitSelectionListener(UnitSelectionListener listener) {
		selectionListeners.remove(listener);
	}

	private void treeSelectionChanged(TreeSelectionEvent event) {
		Object sel = Optional.ofNullable(event.getNewLeadSelectionPath())
			.map(TreePath::getLastPathComponent).map(wtModel::getModelObject).orElse(null);
		if (sel instanceof IUnit) {
			LOGGER.fine("Selection in workerTree is an IUnit");
			for (UnitSelectionListener listener : selectionListeners) {
				listener.selectUnit((IUnit) sel);
			}
			IWorker proxy = new ProxyWorker((IUnit) sel);
			for (UnitMemberListener listener : memberListeners) {
				listener.memberSelected(null, proxy);
			}
		} else if (sel instanceof UnitMember) {
			LOGGER.fine("workerTree selection is a UnitMember, but not an IUnit");
			for (UnitSelectionListener listener : selectionListeners) {
				listener.selectUnit(null);
			}
			for (UnitMemberListener listener : memberListeners) {
				listener.memberSelected(null, (UnitMember) sel);
			}
		} else {
			if (sel instanceof String) {
				LOGGER.fine("workerTree selection is a String, i.e. a unit-kind node");
			} else if (sel == null) {
				LOGGER.fine("Selection in workerTree is null");
			} else {
				LOGGER.warning("Unexpected type of selection in workerTree: " +
					sel.getClass());
			}
			for (UnitSelectionListener listener : selectionListeners) {
				listener.selectUnit(null);
			}
			for (UnitMemberListener listener : memberListeners) {
				listener.memberSelected(null, null);
			}
		}
	}

	// TODO: Move this class to its own file?
	private static class WorkerTreeTransferHandler extends TransferHandler {
		private final IWorkerTreeModel wtModel;
		private final Predicate<TreePath> isExpanded;
		private final TreeSelectionModel selectionModel;
		private final Consumer<TreePath> collapsePath;
		public WorkerTreeTransferHandler(IWorkerTreeModel wtModel,
				TreeSelectionModel selectionModel, Predicate<TreePath> isExpanded,
				Consumer<TreePath> collapsePath) {
			this.wtModel = wtModel;
			this.isExpanded = isExpanded;
			this.selectionModel = selectionModel;
			this.collapsePath = collapsePath;
		}
		/**
		 * Unit members can only be moved, not copied or linked.
		 */
		@Override
		public int getSourceActions(JComponent component) {
			return TransferHandler.MOVE;
		}

		/**
		 * Create a transferable representing the selected node(s). In Ceylon the return type
		 * was the union type <code>{@link
		 * UnitMemberTransferable}|{@link UnitTransferable}?</code>,
		 * but since we can't do that in Java (other than with {@link
		 * either.Either}, which would conflict with the signature of
		 * the interface this is implementing) we declare it here as
		 * the supertype Transferable.
		 */
		@Override
		@Nullable
		public Transferable createTransferable(JComponent component) {
			TreePath[] paths = selectionModel.getSelectionPaths();
			List<Pair<UnitMember, IUnit>> membersToTransfer = new ArrayList<>();
			List<IUnit> unitsToTransfer = new ArrayList<>();

			for (TreePath path : paths) {
				Object last = path.getLastPathComponent();
				Object parentObj = path.getParentPath().getLastPathComponent();
				if (last != null && parentObj != null) {
					Object parent = wtModel.getModelObject(parentObj);
					Object selection = wtModel.getModelObject(last);
					if (parent instanceof IUnit && selection instanceof UnitMember) {
						membersToTransfer.add(Pair.with((UnitMember) selection,
							(IUnit) parent));
					} else if (selection instanceof IUnit &&
							selection instanceof HasMutableKind) {
						unitsToTransfer.add((IUnit) selection);
					} else {
						LOGGER.info("Selection included non-UnitMember: "
							+ selection.getClass());
					}
				}
			}

			if (membersToTransfer.isEmpty()) {
				if (unitsToTransfer.isEmpty()) {
					return null;
				} else {
					// TODO: Make UnitTransferable take Iterable in its constructor
					return new UnitTransferable(unitsToTransfer.stream()
						.toArray(IUnit[]::new));
				}
			} else {
				if (!unitsToTransfer.isEmpty()) { // TODO: combine with containing else?
					LOGGER.warning("Selection included both units and unit members");
				}
				return new UnitMemberTransferable(membersToTransfer.stream().
					toArray(Pair[]::new));
			}
		}

		/**
		 * Whether a drag here is possible.
		 */
		@Override
		public boolean canImport(TransferSupport support) {
			if (support.isDataFlavorSupported(UnitMemberTransferable.FLAVOR) &&
					support.getDropLocation() instanceof JTree.DropLocation &&
					((JTree.DropLocation) support.getDropLocation()).getPath() != null) {
				Object last = ((JTree.DropLocation) support.getDropLocation())
					.getPath().getLastPathComponent();
				Object lastObj = Optional.ofNullable(last).map(wtModel::getModelObject)
					.orElse(null);
				if (lastObj instanceof IUnit || lastObj instanceof UnitMember) {
					return true;
				}
			}
			if (support.isDataFlavorSupported(UnitTransferable.FLAVOR) &&
					support.getDropLocation() instanceof JTree.DropLocation &&
					((JTree.DropLocation) support.getDropLocation()).getPath() != null) {
				Object last = ((JTree.DropLocation) support.getDropLocation())
					.getPath().getLastPathComponent();
				Object lastObj = Optional.ofNullable(last).map(wtModel::getModelObject)
					.orElse(null);
				if (lastObj instanceof String) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Handle a drop.
		 */
		@Override
		public boolean importData(TransferSupport support) {
			if (canImport(support) &&
					support.getDropLocation() instanceof JTree.DropLocation &&
					((JTree.DropLocation) support.getDropLocation()).getPath() != null) {
				TreePath path = ((JTree.DropLocation) support.getDropLocation()).getPath();
				Object pathLast = path.getLastPathComponent();
				if (pathLast == null) {
					return false;
				}
				Object local = wtModel.getModelObject(pathLast);
				Object tempTarget;
				TreePath targetPath;
				if (local instanceof UnitMember) {
					targetPath = path.getParentPath();
					tempTarget = wtModel.getModelObject(targetPath
						.getLastPathComponent());
				} else { // local instanceof IUnit, if canImport() worked
					targetPath = path;
					tempTarget = local;
				}
				Transferable trans = support.getTransferable();
				boolean shouldBeExpanded = isExpanded.test(targetPath);
				try {
					if (tempTarget instanceof IUnit &&
							trans.isDataFlavorSupported(
								UnitMemberTransferable.FLAVOR)) {
						List<Pair<UnitMember, IUnit>> list =
							(List<Pair<UnitMember, IUnit>>)
								trans.getTransferData(
									UnitMemberTransferable.FLAVOR);
						for (Pair<UnitMember, IUnit> pair : list) {
							wtModel.moveMember(pair.getValue0(),
								pair.getValue1(), (IUnit) tempTarget);
						}
						if (!shouldBeExpanded) {
							collapsePath.accept(targetPath);
						}
						return true;
					} else if (tempTarget instanceof String &&
							trans.isDataFlavorSupported(
								UnitTransferable.FLAVOR)) {
						List<IUnit> list = (List<IUnit>)
							trans.getTransferData(UnitTransferable.FLAVOR);
						for (IUnit unit : list) {
							wtModel.changeKind(unit, (String) tempTarget);
						}
						return true;
					} else {
						return false;
					}
				} catch (UnsupportedFlavorException except) {
					LOGGER.log(Level.SEVERE, "Impossible unsupported data flavor",
						except);
					return false;
				} catch (IOException except) {
					LOGGER.log(Level.SEVERE, "I/O error in transfer after we checked",
						except);
					return false;
				}
			} else {
				return false;
			}
		}
	}

	// TODO: Move this class to its own file?
	private static class UnitMemberCellRenderer extends DefaultTreeCellRenderer {
		public UnitMemberCellRenderer(IWorkerTreeModel wtModel, IntSupplier turnSource,
				boolean orderCheck) {
			this.wtModel = wtModel;
			this.turnSource = turnSource;
			this.orderCheck = orderCheck;
		}

		private final IWorkerTreeModel wtModel;
		private final IntSupplier turnSource;
		private final boolean orderCheck;

		// TODO: The (int) casts here were all halfEven().integer (before the '+1') in Ceylon;
		// I'm not sure whether the port introduces one-pixel differences.
		private static Icon createDefaultFixtureIcon() {
			int imageSize = 24;
			BufferedImage temp = new BufferedImage(imageSize, imageSize,
				BufferedImage.TYPE_INT_ARGB);
			Graphics2D pen = temp.createGraphics();
			Color saveColor = pen.getColor();
			pen.setColor(Color.RED);
			double margin = 0.15;
			int firstCorner = ((int) (imageSize * margin)) + 1;
			int firstDimension = (int) (imageSize * (1.0 - (margin * 2.0)));
			int firstArcDimension = (int) (imageSize * (margin / 2.0));
			pen.fillRoundRect(firstCorner, firstCorner, firstDimension,
				firstDimension, firstArcDimension, firstArcDimension);
			pen.setColor(saveColor);
			int secondCorner = (int) ((imageSize / 2.0) - (imageSize * margin)) + 1;
			int secondDimension = (int) (imageSize * margin * 2.0);
			int secondArcDimension = (int) ((imageSize * margin) / 2.0);
			pen.fillRoundRect(secondCorner, secondCorner, secondDimension,
				secondDimension, secondArcDimension, secondArcDimension);
			pen.dispose();
			return new ImageIcon(temp);
		}

		private static Icon DEFAULT_FIXTURE_ICON = createDefaultFixtureIcon();

		@Nullable
		private static Icon getIconForFile(String filename) {
			try {
				return ImageLoader.loadIcon(filename);
			}  catch (FileNotFoundException|NoSuchFileException except) {
				LOGGER.severe(String.format("Image file images/%s not found`", filename));
				LOGGER.log(Level.FINE, "with stack trace", except);
				return null;
			} catch (IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error reading image", except);
				return null;
			}
		}

		private static String jobString(IJob job) {
			return String.format("%s %d", job.getName(), job.getLevel());
		}

		private static String jobCSL(IWorker worker) {
			if (StreamSupport.stream(worker.spliterator(), true).allMatch(IJob::isEmpty)) {
				return "";
			} else {
				return StreamSupport.stream(worker.spliterator(), false)
					.filter(j -> !j.isEmpty())
					.map(UnitMemberCellRenderer::jobString)
					.collect(Collectors.joining(", ", " (", ")"));
			}
		}

		// TODO: may need to rename to avoid collision with superclass method
		private static Icon getIcon(HasImage obj) {
			String image = obj.getImage();
			if (!image.isEmpty()) {
				Icon icon = getIconForFile(image);
				if (icon != null) {
					return icon;
				}
			}
			return Optional.ofNullable(getIconForFile(obj.getDefaultImage()))
				.orElse(DEFAULT_FIXTURE_ICON);
		}

		/**
		 * Returns true if orders contain FIXME, false, if orders
		 * contain TODO or XXX, and null otherwise (or if the unit has
		 * no members, as such a unit is probably an administrative
		 * holdover that <em>shouldn't</em> have orders, and if it does
		 * the orders will be ignored).
		 *
		 * TODO: Convert to enum
		 */
		@Nullable
		private Boolean shouldChangeBackground(IUnit item) {
			if (item.isEmpty()) {
				return null;
			}
			String orders = item.getLatestOrders(turnSource.getAsInt()).toLowerCase();
			if (orders.contains("fixme")) {
				return true;
			} else if (orders.contains("todo") || orders.contains("xxx")) {
				return false;
			} else {
				return null;
			}
		}

		/**
		 * Returns true if orders (for any unit with this "kind")
		 * contain FIXME, false, if orders contain TODO or XXX, and
		 * null otherwise (except that units with no members are ignored).
		 *
		 * TODO: Convert to enum
		 */
		@Nullable
		private Boolean shouldChangeBackground(String item) {
			Boolean retval = null;
			// TODO: Can we avoid the collector step?
			for (IUnit unit : StreamSupport.stream(wtModel.childrenOf(item).spliterator(), false)
					.map(wtModel::getModelObject).filter(IUnit.class::isInstance)
					.map(IUnit.class::cast).collect(Collectors.toList())) {
				Boolean unitPaint = shouldChangeBackground(unit);
				if (unitPaint != null) {
					if (unitPaint) {
						return true;
					} else {
						retval = false;
					}
				}
			}
			return retval;
		}

		// In Ceylon 'tree' and 'object' had to be marked nullable, but I'm not going
		// to bother in Java; that *should* never happen, and in Ceylon the first statement
		// of the method was to assert both are non-null.
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object item, boolean selected,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component component = super.getTreeCellRendererComponent(tree, item,
				selected, expanded, leaf, row, hasFocus);
			Object internal = Optional.of(item).filter(DefaultMutableTreeNode.class::isInstance)
				.map(DefaultMutableTreeNode.class::cast)
				.map(DefaultMutableTreeNode::getUserObject).orElse(item);
			if (internal instanceof HasImage && component instanceof JLabel) {
				((JLabel) component).setIcon(getIcon((HasImage) internal));
			}
			boolean shouldWarn = false;
			boolean shouldError = false;
			if (internal instanceof IWorker && component instanceof JLabel) {
				IWorker worker = (IWorker) internal;
				if ("human".equals(worker.getRace())) {
					((JLabel) component).setText(String.format("<html><p>%s%s</p></html>",
						worker.getName(), jobCSL(worker)));
				} else {
					((JLabel) component).setText(String.format(
						"<html><p>%s, a %s%s</p></html>", worker.getName(),
						worker.getRace(), jobCSL(worker)));
				}
			} else if (internal instanceof Animal && component instanceof JLabel) {
				Animal animal = (Animal) internal;
				Map<String, Integer> maturityAges = MaturityModel.getMaturityAges();
				if (animal.getBorn() >= 0 && MaturityModel.getCurrentTurn() >= 0 &&
						maturityAges.containsKey(animal.getKind()) &&
						MaturityModel.getCurrentTurn() - animal.getBorn() <
							maturityAges.get(animal.getKind())) {
					int age = MaturityModel.getCurrentTurn() - animal.getBorn();
					if (animal.getPopulation() > 1) {
						((JLabel) component).setText(String.format(
							"%d %d-turn-old %s", animal.getPopulation(), age,
							AnimalPlurals.get(animal.getKind())));
					} else {
						((JLabel) component).setText(String.format(
							"%d-turn-old %s", age, animal.getKind()));
					}
				} else if (animal.getPopulation() > 1) {
					((JLabel) component).setText(String.format("%d %s",
						animal.getPopulation(),
						AnimalPlurals.get(animal.getKind())));
				} // else leave the default of animal.toString()
			} else if (internal instanceof IUnit &&
					component instanceof DefaultTreeCellRenderer) {
				IUnit unit = (IUnit) internal;
				if (expanded || unit.isEmpty()) {
					((DefaultTreeCellRenderer) component).setText(unit.getName());
				} else {
					((DefaultTreeCellRenderer) component).setText(String.format(
						"%s (%d workers)", unit.getName(),
						unit.stream().filter(IWorker.class::isInstance).count()));
				}
				Boolean result = shouldChangeBackground(unit);
				if (result != null) {
					if (result) {
						shouldError = true;
						shouldWarn = false;
					} else {
						shouldWarn = true;
					}
				}
			} else if (orderCheck && internal instanceof String) {
				Boolean result = shouldChangeBackground((String) internal);
				if (result != null) {
					if (result) {
						shouldError = true;
						shouldWarn = false;
					} else {
						shouldWarn = true;
					}
				}
			}
			if (component instanceof DefaultTreeCellRenderer) {
				DefaultTreeCellRenderer comp = (DefaultTreeCellRenderer) component;
				if (shouldError) {
					comp.setBackgroundSelectionColor(Color.pink);
					comp.setBackgroundNonSelectionColor(Color.pink);
					comp.setTextSelectionColor(Color.black);
					comp.setTextNonSelectionColor(Color.black);
				} else if (shouldWarn) {
					comp.setBackgroundSelectionColor(Color.yellow);
					comp.setBackgroundNonSelectionColor(Color.yellow);
					comp.setTextSelectionColor(Color.black);
					comp.setTextNonSelectionColor(Color.black);
				} else {
					comp.setBackgroundSelectionColor(DEFAULT_STORER
						.getBackgroundSelectionColor());
					comp.setBackgroundNonSelectionColor(DEFAULT_STORER
						.getBackgroundNonSelectionColor());
					comp.setTextSelectionColor(Color.white);
					comp.setTextNonSelectionColor(Color.black);
				}
			}
			return component;
		}
	}

	private static class TreeModelHandler implements TreeModelListener {
		// TODO: Combine with some other class? Probably the tree itself.

		public TreeModelHandler(JTree tree) {
			this.tree = tree;
		}

		private final JTree tree;

		@Override
		public void treeStructureChanged(TreeModelEvent event) {
			TreePath path = event.getTreePath();
			TreePath parent = Optional.ofNullable(path)
				.map(TreePath::getParentPath).orElse(null);
			if (parent != null) {
				tree.expandPath(parent);
			} else if (parent != null) {
				tree.expandPath(path);
			}
			for (int i = 0; i < tree.getRowCount(); i++) {
				tree.expandRow(i);
			}
			tree.updateUI();
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent event) {
			TreePath path = event.getTreePath().pathByAddingChild(event.getChildren()[0]);
			LOGGER.finer("About to remove this path from selection: " + path);
			tree.removeSelectionPath(path);
			tree.updateUI();
		}

		@Override
		public void treeNodesInserted(TreeModelEvent event) {
			TreePath path = event.getTreePath();
			if (path != null) {
				tree.expandPath(path);
				TreePath parent = path.getParentPath();
				if (parent != null) {
					tree.expandPath(parent);
				}
			}
			tree.updateUI();
		}

		@Override
		public void treeNodesChanged(TreeModelEvent event) {
			Optional.ofNullable(event.getTreePath()).map(TreePath::getParentPath)
				.ifPresent(tree::expandPath);
			tree.updateUI();
		}
	}

	private static class TreeMouseListener extends MouseAdapter {
		public TreeMouseListener(JTree tree, Iterable<Player> players, IWorkerTreeModel wtModel,
				IDRegistrar idf) {
			this.tree = tree;
			this.wtModel = wtModel;
			this.idf = idf;
			this.players = players;
		}

		private final JTree tree;
		private final IWorkerTreeModel wtModel;
		private final IDRegistrar idf;
		private final Iterable<Player> players;

		private void handleMouseEvent(MouseEvent event) {
			if (event.isPopupTrigger() && event.getClickCount() == 1) {
				IFixture obj = Optional.ofNullable(tree
						.getClosestPathForLocation(event.getX(), event.getY()))
					.map(TreePath::getLastPathComponent).map(wtModel::getModelObject)
					.filter(IFixture.class::isInstance).map(IFixture.class::cast)
					.orElse(null);
				if (obj != null) {
					new FixtureEditMenu(obj, players, idf, wtModel)
						.show(event.getComponent(), event.getX(), event.getY());
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			handleMouseEvent(event);
		}

		@Override
		public void mousePressed(MouseEvent event) {
			handleMouseEvent(event);
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			handleMouseEvent(event);
		}
	}
}
