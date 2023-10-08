package drivers.worker_mgmt;

import java.util.Comparator;

import lovelace.util.LovelaceLogger;
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
import java.util.function.Predicate;
import java.util.function.Consumer;

import org.javatuples.Pair;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A tree of a player's units.
 */
public final class WorkerTree extends JTree implements UnitMemberSelectionSource, UnitSelectionSource {
    private static final long serialVersionUID = 1L;
    // TODO: Move into the inner class that uses this
    private static final DefaultTreeCellRenderer DEFAULT_STORER = new DefaultTreeCellRenderer();
    private final List<Pair<String, ToIntFunction<WorkerStats>>> statReferencesList =
            List.of(Pair.with("Str", WorkerStats::getStrength), Pair.with("Dex", WorkerStats::getDexterity), Pair.with("Con", WorkerStats::getConstitution), Pair.with("Int", WorkerStats::getIntelligence), Pair.with("Wis", WorkerStats::getWisdom), Pair.with("Cha", WorkerStats::getCharisma));

    private final IWorkerTreeModel wtModel;

    /**
     * @param wtModel The tree model
     * @param players The players in the map
     * @param turnSource How to get the current turn
     * @param orderCheck Whether we should visually warn if orders contain
     * substrings indicating remaining work or if a unit named "unassigned"
     * is nonempty TODO: enum instead of boolean
     * @param idf The factory to use to generate ID numbers.
     */
    public WorkerTree(final IWorkerTreeModel wtModel, final Iterable<Player> players, final IntSupplier turnSource,
                      final boolean orderCheck, final IDRegistrar idf) {
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
            final WorkerStats stats) {
        return pair -> String.format("%s %s", pair.getValue0(),
                WorkerStats.getModifierString(pair.getValue1().applyAsInt(stats)));
    }

    @Override
    public @Nullable String getToolTipText(final MouseEvent event) {
        if (getRowForLocation(event.getX(), event.getY()) == -1) {
            return null;
        }
        final WorkerStats stats = Optional.ofNullable(getPathForLocation(event.getX(), event.getY()))
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
    public void addUnitMemberListener(final UnitMemberListener listener) {
        memberListeners.add(listener);
    }

    @Override
    public void addUnitSelectionListener(final UnitSelectionListener listener) {
        selectionListeners.add(listener);
    }

    @Override
    public void removeUnitMemberListener(final UnitMemberListener listener) {
        memberListeners.remove(listener);
    }

    @Override
    public void removeUnitSelectionListener(final UnitSelectionListener listener) {
        selectionListeners.remove(listener);
    }

    private void treeSelectionChanged(final TreeSelectionEvent event) {
        final Object sel = Optional.ofNullable(event.getNewLeadSelectionPath())
                .map(TreePath::getLastPathComponent).map(wtModel::getModelObject).orElse(null);
        if (sel instanceof IUnit u) {
            LovelaceLogger.debug("Selection in workerTree is an IUnit");
            for (final UnitSelectionListener listener : selectionListeners) {
                listener.selectUnit(u);
            }
            final IWorker proxy = new ProxyWorker(u);
            for (final UnitMemberListener listener : memberListeners) {
                listener.memberSelected(null, proxy);
            }
        } else if (sel instanceof UnitMember um) {
            LovelaceLogger.debug("workerTree selection is a UnitMember, but not an IUnit");
            for (final UnitSelectionListener listener : selectionListeners) {
                listener.selectUnit(null);
            }
            for (final UnitMemberListener listener : memberListeners) {
                listener.memberSelected(null, um);
            }
        } else {
            if (sel instanceof String) {
                LovelaceLogger.debug("workerTree selection is a String, i.e. a unit-kind node");
            } else if (sel == null) {
                LovelaceLogger.debug("Selection in workerTree is null");
            } else {
                LovelaceLogger.warning("Unexpected type of selection in workerTree: %s",
                        sel.getClass());
            }
            for (final UnitSelectionListener listener : selectionListeners) {
                listener.selectUnit(null);
            }
            for (final UnitMemberListener listener : memberListeners) {
                listener.memberSelected(null, null);
            }
        }
    }

    // TODO: Move this class to its own file?
    private static class WorkerTreeTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;
        private final IWorkerTreeModel wtModel;
        private final Predicate<TreePath> isExpanded;
        private final TreeSelectionModel selectionModel;
        private final Consumer<TreePath> collapsePath;

        public WorkerTreeTransferHandler(final IWorkerTreeModel wtModel,
                                         final TreeSelectionModel selectionModel, final Predicate<TreePath> isExpanded,
                                         final Consumer<TreePath> collapsePath) {
            this.wtModel = wtModel;
            this.isExpanded = isExpanded;
            this.selectionModel = selectionModel;
            this.collapsePath = collapsePath;
        }

        /**
         * Unit members can only be moved, not copied or linked.
         */
        @Override
        public int getSourceActions(final JComponent component) {
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
        public @Nullable Transferable createTransferable(final JComponent component) {
            final TreePath[] paths = selectionModel.getSelectionPaths();
            final List<Pair<UnitMember, IUnit>> membersToTransfer = new ArrayList<>();
            final List<IUnit> unitsToTransfer = new ArrayList<>();

            for (final TreePath path : paths) {
                final Object last = path.getLastPathComponent();
                final Object parentObj = path.getParentPath().getLastPathComponent();
                if (last != null && parentObj != null) {
                    final Object parent = wtModel.getModelObject(parentObj);
                    final Object selection = wtModel.getModelObject(last);
                    if (parent instanceof IUnit unit && selection instanceof UnitMember member) {
                        membersToTransfer.add(Pair.with(member, unit));
                    } else if (selection instanceof IUnit unit &&
                            selection instanceof HasMutableKind) {
                        unitsToTransfer.add(unit);
                    } else {
                        LovelaceLogger.info("Selection included non-UnitMember: %s", selection.getClass());
                    }
                }
            }

            if (membersToTransfer.isEmpty()) {
                if (unitsToTransfer.isEmpty()) {
                    return null;
                } else {
                    // TODO: Make UnitTransferable take Iterable in its constructor
                    return new UnitTransferable(unitsToTransfer.toArray(IUnit[]::new));
                }
            } else {
                if (!unitsToTransfer.isEmpty()) { // TODO: combine with containing else?
                    LovelaceLogger.warning("Selection included both units and unit members");
                }
                return new UnitMemberTransferable(membersToTransfer.toArray(Pair[]::new));
            }
        }

        /**
         * Whether a drag here is possible.
         */
        @Override
        public boolean canImport(final TransferSupport support) {
            if (support.isDataFlavorSupported(UnitMemberTransferable.FLAVOR) &&
                    support.getDropLocation() instanceof JTree.DropLocation dl && dl.getPath() != null) {
                final Object last = dl.getPath().getLastPathComponent();
                final Object lastObj = Optional.ofNullable(last).map(wtModel::getModelObject)
                        .orElse(null);
                if (lastObj instanceof IUnit || lastObj instanceof UnitMember) {
                    return true;
                }
            }
            if (support.isDataFlavorSupported(UnitTransferable.FLAVOR) &&
                    support.getDropLocation() instanceof JTree.DropLocation dl && dl.getPath() != null) {
                final Object last = dl.getPath().getLastPathComponent();
                return Optional.ofNullable(last).map(wtModel::getModelObject).orElse(null) instanceof String;
            }
            return false;
        }

        /**
         * Handle a drop.
         */
        @Override
        public boolean importData(final TransferSupport support) {
            if (canImport(support) &&
                    support.getDropLocation() instanceof JTree.DropLocation dl && dl.getPath() != null) {
                final TreePath path = dl.getPath();
                final Object pathLast = path.getLastPathComponent();
                if (pathLast == null) {
                    return false;
                }
                final Object local = wtModel.getModelObject(pathLast);
                final Object tempTarget;
                final TreePath targetPath;
                if (local instanceof UnitMember) {
                    targetPath = path.getParentPath();
                    tempTarget = wtModel.getModelObject(targetPath
                            .getLastPathComponent());
                } else { // local instanceof IUnit, if canImport() worked
                    targetPath = path;
                    tempTarget = local;
                }
                final Transferable trans = support.getTransferable();
                final boolean shouldBeExpanded = isExpanded.test(targetPath);
                try {
                    if (tempTarget instanceof IUnit unit &&
                            trans.isDataFlavorSupported(
                                    UnitMemberTransferable.FLAVOR)) {
                        final List<Pair<UnitMember, IUnit>> list =
                                (List<Pair<UnitMember, IUnit>>)
                                        trans.getTransferData(
                                                UnitMemberTransferable.FLAVOR);
                        for (final Pair<UnitMember, IUnit> pair : list) {
                            wtModel.moveMember(pair.getValue0(),
                                    pair.getValue1(), unit);
                        }
                        if (!shouldBeExpanded) {
                            collapsePath.accept(targetPath);
                        }
                        return true;
                    } else if (tempTarget instanceof String str &&
                            trans.isDataFlavorSupported(
                                    UnitTransferable.FLAVOR)) {
                        final List<IUnit> list = (List<IUnit>)
                                trans.getTransferData(UnitTransferable.FLAVOR);
                        for (final IUnit unit : list) {
                            wtModel.changeKind(unit, str);
                        }
                        return true;
                    } else {
                        return false;
                    }
                } catch (final UnsupportedFlavorException except) {
                    LovelaceLogger.error(except, "Impossible unsupported data flavor");
                    return false;
                } catch (final IOException except) {
                    LovelaceLogger.error(except, "I/O error in transfer after we checked");
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    // TODO: Move this class to its own file?
    private static class UnitMemberCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

        public UnitMemberCellRenderer(final IWorkerTreeModel wtModel, final IntSupplier turnSource,
                                      final boolean orderCheck) {
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
            final int imageSize = 24;
            final BufferedImage temp = new BufferedImage(imageSize, imageSize,
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics2D pen = temp.createGraphics();
            final Color saveColor = pen.getColor();
            pen.setColor(Color.RED);
            final double margin = 0.15;
            final int firstCorner = ((int) (imageSize * margin)) + 1;
            final int firstDimension = (int) (imageSize * (1.0 - (margin * 2.0)));
            final int firstArcDimension = (int) (imageSize * (margin / 2.0));
            pen.fillRoundRect(firstCorner, firstCorner, firstDimension,
                    firstDimension, firstArcDimension, firstArcDimension);
            pen.setColor(saveColor);
            final int secondCorner = (int) ((imageSize / 2.0) - (imageSize * margin)) + 1;
            final int secondDimension = (int) (imageSize * margin * 2.0);
            final int secondArcDimension = (int) ((imageSize * margin) / 2.0);
            pen.fillRoundRect(secondCorner, secondCorner, secondDimension,
                    secondDimension, secondArcDimension, secondArcDimension);
            pen.dispose();
            return new ImageIcon(temp);
        }

        private static final Icon DEFAULT_FIXTURE_ICON = createDefaultFixtureIcon();

        private static @Nullable Icon getIconForFile(final String filename) {
            try {
                return ImageLoader.loadIcon(filename);
            } catch (final FileNotFoundException | NoSuchFileException except) {
                LovelaceLogger.error("Image file images/%s not found", filename);
                LovelaceLogger.debug(except, "with stack trace");
                return null;
            } catch (final IOException except) {
                LovelaceLogger.error(except, "I/O error reading image");
                return null;
            }
        }

        private static String jobString(final IJob job) {
            return String.format("%s %d", job.getName(), job.getLevel());
        }

        private static String jobCSL(final IWorker worker) {
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
        private static Icon getIcon(final HasImage obj) {
            final String image = obj.getImage();
            if (!image.isEmpty()) {
                final Icon icon = getIconForFile(image);
                if (icon != null) {
                    return icon;
                }
            }
            return Optional.ofNullable(getIconForFile(obj.getDefaultImage()))
                    .orElse(DEFAULT_FIXTURE_ICON);
        }

        private enum BackgroundState {
            NONE(null), MISSING(new Color(184, 224, 249)), TODO(Color.yellow), FIXME(Color.pink);

            BackgroundState(final @Nullable Color color) {
                this.color = color;
            }

            private final @Nullable Color color;

            public @Nullable Color getColor() {
                return color;
            }

            public static BackgroundState larger(final BackgroundState one, final BackgroundState two) {
                if (one.compareTo(two) < 0) {
                    return two;
                } else {
                    return one;
                }
            }
        }

        /**
         * Returns {@link BackgroundState#FIXME} if orders contain FIXME,
         * {@link BackgroundState##WARN} if orders contain TODO or XXX, {@link BackgroundState#MISSING} if orders are
         * empty, and {@link BackgroundState#NONE} otherwise (or if the unit has no members,
         * as such a unit is probably an administrative holdover that
         * <em>shouldn't</em> have orders, and if it does the orders will be ignored).
         */
        private BackgroundState shouldChangeBackground(final IUnit item) {
            if (item.isEmpty()) {
                return BackgroundState.NONE;
            }
            final String orders = item.getLatestOrders(turnSource.getAsInt()).toLowerCase();
            if (orders.contains("fixme")) {
                return BackgroundState.FIXME;
            } else if (orders.contains("todo") || orders.contains("xxx")) {
                return BackgroundState.TODO;
            } else if (orders.isBlank() || item.getOrders(turnSource.getAsInt()).isBlank()) {
                return BackgroundState.MISSING;
            } else {
                return BackgroundState.NONE;
            }
        }

        /**
         * Returns true if orders (for any unit with this "kind")
         * contain FIXME, false, if orders contain TODO or XXX, and
         * null otherwise (except that units with no members are ignored).
         */
        private BackgroundState shouldChangeBackground(final String item) {
            return StreamSupport.stream(wtModel.childrenOf(item).spliterator(), false)
                    .map(wtModel::getModelObject).filter(IUnit.class::isInstance)
                    .map(IUnit.class::cast).map(this::shouldChangeBackground).max(Comparator.naturalOrder())
                    .orElse(BackgroundState.NONE);
        }

        // In Ceylon 'tree' and 'object' had to be marked nullable, but I'm not going
        // to bother in Java; that *should* never happen, and in Ceylon the first statement
        // of the method was to assert both are non-null.
        @Override
        public Component getTreeCellRendererComponent(final JTree tree, final Object item, final boolean selected,
                                                      final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
            final Component component = super.getTreeCellRendererComponent(tree, item,
                    selected, expanded, leaf, row, hasFocus);
            final Object internal = Optional.of(item).filter(DefaultMutableTreeNode.class::isInstance)
                    .map(DefaultMutableTreeNode.class::cast)
                    .map(DefaultMutableTreeNode::getUserObject).orElse(item);
            if (internal instanceof HasImage hi && component instanceof JLabel label) {
                label.setIcon(getIcon(hi));
            }
            BackgroundState background = BackgroundState.NONE;
            if (internal instanceof final IWorker worker && component instanceof JLabel label) {
                if ("human".equals(worker.getRace())) {
                    label.setText(String.format("<html><p>%s%s</p></html>",
                            worker.getName(), jobCSL(worker)));
                } else {
                    label.setText(String.format(
                            "<html><p>%s, a %s%s</p></html>", worker.getName(),
                            worker.getRace(), jobCSL(worker)));
                }
            } else if (internal instanceof final Animal animal && component instanceof JLabel label) {
                final Map<String, Integer> maturityAges = MaturityModel.getMaturityAges();
                if (animal.getBorn() >= 0 && MaturityModel.getCurrentTurn() >= 0 &&
                        maturityAges.containsKey(animal.getKind()) &&
                        MaturityModel.getCurrentTurn() - animal.getBorn() <
                                maturityAges.get(animal.getKind())) {
                    final int age = MaturityModel.getCurrentTurn() - animal.getBorn();
                    if (animal.getPopulation() > 1) {
                        label.setText(String.format(
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
            } else if (internal instanceof final IUnit unit &&
                    component instanceof DefaultTreeCellRenderer dtcr) {
                if (expanded || unit.isEmpty()) {
                    dtcr.setText(unit.getName());
                } else {
                    dtcr.setText(String.format(
                            "%s (%d workers)", unit.getName(),
                            unit.stream().filter(IWorker.class::isInstance).count()));
                }
                final BackgroundState result = shouldChangeBackground(unit);
                background = BackgroundState.larger(background, result);
            } else if (orderCheck && internal instanceof String kind) {
                final BackgroundState result = shouldChangeBackground(kind);
                background = BackgroundState.larger(background, result);
            }
            if (component instanceof final DefaultTreeCellRenderer comp) {
                final @Nullable Color backgroundColor = background.getColor();
                if (backgroundColor == null) {
                    comp.setBackgroundSelectionColor(DEFAULT_STORER
                            .getBackgroundSelectionColor());
                    comp.setBackgroundNonSelectionColor(DEFAULT_STORER
                            .getBackgroundNonSelectionColor());
                    comp.setTextSelectionColor(Color.white);
                    comp.setTextNonSelectionColor(Color.black);
                } else {
                    comp.setBackgroundSelectionColor(backgroundColor);
                    comp.setBackgroundNonSelectionColor(backgroundColor);
                    comp.setTextSelectionColor(Color.black);
                    comp.setTextNonSelectionColor(Color.black);
                }
            }
            return component;
        }
    }

    private record TreeModelHandler(JTree tree) implements TreeModelListener {
        // TODO: Combine with some other class? Probably the tree itself.

        @Override
        public void treeStructureChanged(final TreeModelEvent event) {
            final TreePath path = event.getTreePath();
            final TreePath parent = Optional.ofNullable(path)
                    .map(TreePath::getParentPath).orElse(null);
            if (parent != null) {
                tree.expandPath(parent);
            } else if (path != null) {
                tree.expandPath(path);
            }
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
            tree.updateUI();
        }

        @Override
        public void treeNodesRemoved(final TreeModelEvent event) {
            final TreePath path = event.getTreePath().pathByAddingChild(event.getChildren()[0]);
            LovelaceLogger.trace("About to remove this path from selection: %s", path);
            tree.removeSelectionPath(path);
            tree.updateUI();
        }

        @Override
        public void treeNodesInserted(final TreeModelEvent event) {
            final TreePath path = event.getTreePath();
            if (path != null) {
                tree.expandPath(path);
                final TreePath parent = path.getParentPath();
                if (parent != null) {
                    tree.expandPath(parent);
                }
            }
            tree.updateUI();
        }

        @Override
        public void treeNodesChanged(final TreeModelEvent event) {
            Optional.ofNullable(event.getTreePath()).map(TreePath::getParentPath)
                    .ifPresent(tree::expandPath);
            tree.updateUI();
        }
    }

    private static class TreeMouseListener extends MouseAdapter {
        public TreeMouseListener(final JTree tree, final Iterable<Player> players, final IWorkerTreeModel wtModel,
                                 final IDRegistrar idf) {
            this.tree = tree;
            this.wtModel = wtModel;
            this.idf = idf;
            this.players = players;
        }

        private final JTree tree;
        private final IWorkerTreeModel wtModel;
        private final IDRegistrar idf;
        private final Iterable<Player> players;

        private void handleMouseEvent(final MouseEvent event) {
            if (event.isPopupTrigger() && event.getClickCount() == 1) {
                Optional.ofNullable(tree
                                .getClosestPathForLocation(event.getX(), event.getY()))
                        .map(TreePath::getLastPathComponent).map(wtModel::getModelObject)
                        .filter(IFixture.class::isInstance).map(IFixture.class::cast)
                        .ifPresent(obj -> new FixtureEditMenu(obj, players, idf, wtModel)
                                .show(event.getComponent(), event.getX(), event.getY()));
            }
        }

        @Override
        public void mouseClicked(final MouseEvent event) {
            handleMouseEvent(event);
        }

        @Override
        public void mousePressed(final MouseEvent event) {
            handleMouseEvent(event);
        }

        @Override
        public void mouseReleased(final MouseEvent event) {
            handleMouseEvent(event);
        }
    }
}
