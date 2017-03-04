import controller.map.misc {
    ICLIHelper,
    WindowCloser,
    IDFactoryFiller
}
import model.misc {
    IDriverModel
}
import model.workermgmt {
    IWorkerModel,
    WorkerModel,
    IWorkerTreeModel,
    WorkerTreeModelAlt,
    UnitMemberTransferable
}
import javax.swing {
    SwingUtilities,
    JComponent,
    KeyStroke,
    JFileChooser,
    JScrollPane,
    JLabel,
    JTree,
    ToolTipManager,
    SpinnerNumberModel,
    JPanel,
    JSpinner,
    JTextArea,
    JButton,
    TransferHandler,
    Icon,
    ImageIcon,
    JFrame,
    JMenuBar,
    BorderFactory,
    SwingConstants
}
import ceylon.interop.java {
    CeylonIterable,
    JavaList,
    createJavaIntArray,
    createJavaObjectArray,
    JavaIterable
}
import model.map.fixtures {
    UnitMember
}
import model.listeners {
    PlayerChangeListener,
    NewUnitSource,
    UnitMemberSelectionSource,
    UnitSelectionSource,
    UnitSelectionListener,
    UnitMemberListener
}
import java.awt {
    Dimension,
    Component,
    Frame,
    Graphics2D,
    Color,
    BorderLayout,
    GridLayout,
    Image,
    Graphics
}
import model.map {
    IMapNG,
    Player,
    DistanceComparator,
    Point,
    PointFactory,
    HasName,
    IFixture,
    HasImage,
    HasPortrait,
    HasMutableName,
    HasKind
}
import util {
    Pair
}
import java.awt.event {
    KeyEvent,
    ActionEvent,
    WindowEvent,
    WindowAdapter,
    MouseAdapter,
    MouseEvent,
    KeyAdapter,
    ActionListener
}
import model.map.fixtures.mobile {
    IUnit,
    ProxyUnit,
    IWorker,
    ProxyFor,
    Animal,
    Worker
}
import javax.swing.tree {
    DefaultTreeModel,
    DefaultMutableTreeNode,
    TreePath,
    DefaultTreeCellRenderer
}
import model.report {
    SimpleReportNode,
    IReportNode
}
import java.lang {
    Thread,
    IllegalStateException,
    JIterable=Iterable,
    ArrayIndexOutOfBoundsException,
    IllegalArgumentException,
    IntArray,
    ObjectArray
}
import model.map.fixtures.towns {
    Fortress
}
import model.viewer {
    IViewerModel,
    ViewerModel
}
import com.bric.window {
    WindowList,
    WindowMenu
}
import javax.swing.event {
        TreeSelectionListener,
    TreeSelectionEvent,
    TreeModelEvent,
    TreeModelListener
}
import ceylon.file {
    Resource,
    File,
    Nil,
    Writer,
    parsePath
}
import ceylon.collection {
    HashMap,
    ArrayList,
    MutableMap,
    MutableList
}
import model.map.fixtures.mobile.worker {
    IJob,
    ProxyWorker,
    WorkerStats,
    ISkill
}
import strategicprimer.viewer.about {
    aboutDialog
}
import strategicprimer.viewer.report {
    createAbbreviatedReportIR
}
import lovelace.util.jvm {
    listenedButton,
    FunctionalGroupLayout,
    centeredHorizontalBox,
    BorderedPanel,
    verticalSplit,
    horizontalSplit,
    createHotKey,
    FormattedLabel,
    platform,
    ActionWrapper
}
import java.awt.datatransfer {
    Transferable,
    UnsupportedFlavorException
}
import java.io {
    IOException,
    FileNotFoundException
}
import java.awt.image {
    BufferedImage
}
import ceylon.math.float {
    halfEven
}
import java.nio.file {
    NoSuchFileException
}
import ceylon.language.meta {
    type
}
import lovelace.util.common {
    todo
}
"A TreeModel implementation for a player's units and workers."
class WorkerTreeModel(variable Player player, IWorkerModel model)
        satisfies IWorkerTreeModel {
    MutableList<UnitMember> dismissedMembers = ArrayList<UnitMember>();
    MutableList<TreeModelListener> listeners = ArrayList<TreeModelListener>();
    shared actual Player root => player;

    shared actual Object getChild(Object parent, Integer index) {
        if (is Player parent) {
            if (exists child = CeylonIterable(model.getUnitKinds(parent))
                .getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else if (is String parent, model.getUnitKinds(player).contains(parent)) {
            if (exists child = CeylonIterable(model.getUnits(player, parent))
                .getFromFirst(index)) {
                return child;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else if (is IUnit parent) {
            if (exists child = CeylonIterable(parent).getFromFirst(index)) {
                return index;
            } else {
                throw ArrayIndexOutOfBoundsException(index);
            }
        } else {
            throw ArrayIndexOutOfBoundsException("Unrecognized parent");
        }
    }

    shared actual Integer getChildCount(Object parent) {
        if (is Player parent) {
            return model.getUnits(parent).size();
        } else if (is String parent, model.getUnitKinds(player).contains(parent)) {
            return model.getUnits(player, parent).size();
        } else if (is IUnit parent) {
            return CeylonIterable(parent).size;
        } else {
            throw IllegalArgumentException("Not a possible member of the tree");
        }
    }

    shared actual Boolean isLeaf(Object node) => !node is Player|IUnit|String;
    shared actual void valueForPathChanged(TreePath path, Object newValue) =>
            log.error("valueForPathChanged needs to be implemented");

    shared actual Integer getIndexOfChild(Object parent, Object child) {
        if (is Player parent, is IUnit child) {
            return model.getUnits(parent).indexOf(child);
        } else if (is IUnit parent,
            exists index->ignored = CeylonIterable(parent).locate(child.equals)) {
            return index;
        } else {
            return -1;
        }
    }

    shared actual void addTreeModelListener(TreeModelListener listener) =>
            listeners.add(listener);
    shared actual void removeTreeModelListener(TreeModelListener listener) =>
            listeners.remove(listener);

    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        Integer oldIndex = getIndexOfChild(old, member);
        TreeModelEvent removedEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray({ root, old })), createJavaIntArray({ oldIndex }),
                createJavaObjectArray({ member }));
        TreeModelEvent removedChangeEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray({ root, old })));
        for (listener in listeners) {
            listener.treeNodesRemoved(removedEvent);
            listener.treeStructureChanged(removedChangeEvent);
        }
        newOwner.addMember(member);
        TreeModelEvent insertedEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray<Object>({ root, newOwner })),
                createJavaIntArray({ getIndexOfChild(newOwner, member) }),
                createJavaObjectArray<Object>({ member }));
        TreeModelEvent insertedChangeEvent = TreeModelEvent(this,
            TreePath(createJavaObjectArray<Object>({ root, newOwner })));
        for (listener in listeners) {
            listener.treeNodesInserted(insertedEvent);
            listener.treeStructureChanged(insertedChangeEvent);
        }
    }
    shared actual void addUnit(IUnit unit) {
        model.addUnit(unit);
        TreePath path = TreePath(root);
        value indices = createJavaIntArray({model.getUnits(player).size()});
        value children = createJavaObjectArray({unit});
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
    }
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);
    shared actual void mapChanged() {
        player = model.map.currentPlayer;
        TreePath path = TreePath(root);
        TreeModelEvent event = TreeModelEvent(this, path);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual void playerChanged(Player? old, Player newPlayer) {
        player = newPlayer;
        TreeModelEvent event = TreeModelEvent(this, TreePath(root));
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual Object getModelObject(Object obj) => obj;
    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        unit.addMember(member);
        TreePath path = TreePath(createJavaObjectArray({root, unit}));
        IntArray indices = createJavaIntArray({getIndexOfChild(unit, member)});
        ObjectArray<Object> children = createJavaObjectArray<Object>({member});
        TreeModelEvent event = TreeModelEvent(this, path, indices,
            children);
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
    }
    shared actual void renameItem(HasMutableName item) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(createJavaObjectArray({root}));
            indices = createJavaIntArray({getIndexOfChild(root, item)});
            children = createJavaObjectArray<Object>({item});
        } else if (is UnitMember item,
                exists parent = CeylonIterable(model.getUnits(player))
                    .find((unit) => CeylonIterable(unit).contains(item))) {
            path = TreePath(createJavaObjectArray({root, parent}));
            indices = createJavaIntArray({getIndexOfChild(parent, item)});
            children = createJavaObjectArray<Object>({item});
        } else {
            // Probably the player; in any case, ignore.
            return;
        }
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual void moveItem(HasKind item) {
        TreePath path;
        IntArray indices;
        ObjectArray<Object> children;
        if (is IUnit item) {
            path = TreePath(createJavaObjectArray({root}));
            indices = createJavaIntArray({getIndexOfChild(root, item)});
            children = createJavaObjectArray<Object>({item});
        } else if (is UnitMember item,
            exists parent = CeylonIterable(model.getUnits(player))
                .find((unit) => CeylonIterable(unit).contains(item))) {
            path = TreePath(createJavaObjectArray({root, parent}));
            indices = createJavaIntArray({getIndexOfChild(parent, item)});
            children = createJavaObjectArray<Object>({item});
        } else {
            // Impossible at present, so ignore
            return;
        }
        TreeModelEvent event = TreeModelEvent(this, path, indices, children);
        for (listener in listeners) {
            listener.treeNodesChanged(event);
        }
    }
    shared actual void dismissUnitMember(UnitMember member) {
        for (unit in model.getUnits(root)) {
            if (exists index->item = CeylonIterable(unit).locate(member.equals)) {
                dismissedMembers.add(member);
                unit.removeMember(member);
                TreeModelEvent event = TreeModelEvent(this,
                    TreePath(createJavaObjectArray({root, unit})),
                    createJavaIntArray({index}), createJavaObjectArray({member}));
                for (listener in listeners) {
                    listener.treeNodesRemoved(event);
                }
            }
        }
    }
    shared actual JIterable<UnitMember> dismissed() => JavaIterable(dismissedMembers);
}
"A tree of a player's units."
JTree&UnitMemberSelectionSource&UnitSelectionSource workerTree(
        "The tree model"
        IWorkerTreeModel wtModel,
        "The players in the map"
        {Player*} players,
        "How to get the current turn"
        Integer() turnSource,
        """Whether we should visually warn if orders contain substrings indicating remaining
           work or if a unit named "unassigned" is nonempty"""
        Boolean orderCheck) {
    object retval extends JTree() satisfies UnitMemberSelectionSource&UnitSelectionSource {
        model = wtModel;
        rootVisible = false;
        dragEnabled = true;
        showsRootHandles = true;
        object workerTreeTransferHandler extends TransferHandler() {
            "Unit members can only be moved, not copied or linked."
            shared actual Integer getSourceActions(JComponent component) =>
                    TransferHandler.move;
            "Create a transferable representing the selected node(s)."
            shared actual UnitMemberTransferable? createTransferable(JComponent component) {
                value paths = selectionModel.selectionPaths;
                // TODO: use Tuples instead of Pairs
                MutableList<Pair<UnitMember, IUnit>> toTransfer =
                        ArrayList<Pair<UnitMember, IUnit>>();
                for (path in paths) {
                    if (exists last = path.lastPathComponent,
                            exists parentPath = path.parentPath,
                            exists parentObj = parentPath.lastPathComponent) {
                        if (is IUnit parent = wtModel.getModelObject(parentObj),
                                is UnitMember selection = wtModel.getModelObject(last)) {
                            toTransfer.add(Pair<UnitMember, IUnit>.\iof(selection, parent));
                        } else {
                            log.info("Selection included non-UnitMember");
                        }
                    }
                }
                if (toTransfer.empty) {
                    return null;
                } else {
                    return UnitMemberTransferable(JavaList(toTransfer));
                }
            }
            "Whether a drag here is possible."
            shared actual Boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(UnitMemberTransferable.flavor),
                        is JTree.DropLocation dropLocation = support.dropLocation,
                        exists path = dropLocation.path,
                        exists last = path.lastPathComponent,
                        is IUnit|UnitMember lastObj = wtModel.getModelObject(last)) {
                    return true;
                } else {
                    return false;
                }
            }
            "Handle a drop."
            shared actual Boolean importData(TransferSupport support) {
                if (canImport(support),
                        is JTree.DropLocation dropLocation = support.dropLocation,
                        exists path = dropLocation.path,
                        exists pathLast = path.lastPathComponent,
                        exists local = wtModel.getModelObject(pathLast)) {
                    Object tempTarget;
                    if (is UnitMember local) {
                        TreePath pathParent = path.parentPath;
                        tempTarget = wtModel.getModelObject(pathParent.lastPathComponent);
                    } else {
                        tempTarget = local;
                    }
                    if (is IUnit tempTarget) {
                        try {
                            Transferable trans = support.transferable;
                            assert (is JIterable<Pair<UnitMember, IUnit>> list =
                                trans.getTransferData(UnitMemberTransferable.flavor));
                            for (pair in CeylonIterable(list)) {
                                wtModel.moveMember(pair.first(), pair.second(), tempTarget);
                            }
                            return true;
                        } catch (UnsupportedFlavorException except) {
                            log.error("Impossible unsupported data flavor", except);
                            return false;
                        } catch (IOException except) {
                            log.error("I/O error in transfer after we checked", except);
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        transferHandler = workerTreeTransferHandler;
        object unitMemberCellRenderer extends DefaultTreeCellRenderer() {
            Icon createDefaultFixtureIcon() {
                Integer imageSize = 24;
                BufferedImage temp = BufferedImage(imageSize, imageSize,
                    BufferedImage.typeIntArgb);
                Graphics2D pen = temp.createGraphics();
                Color saveColor = pen.color;
                pen.color = Color.\iRED;
                Float margin = 0.15;
                Integer firstCorner = halfEven(imageSize * margin).integer + 1;
                Integer firstDimension = halfEven(imageSize * (1.0 - (margin * 2.0))).integer;
                Integer firstArcDimension = halfEven(imageSize * (margin / 2.0)).integer;
                pen.fillRoundRect(firstCorner, firstCorner, firstDimension,
                    firstDimension, firstArcDimension, firstArcDimension);
                pen.color = saveColor;
                Integer secondCorner = halfEven((imageSize / 2.0) - (imageSize * margin))
                    .integer + 1;
                Integer secondDimension = halfEven(imageSize * margin * 2.0).integer;
                Integer secondArcDimension = halfEven((imageSize * margin) / 2.0).integer;
                pen.fillRoundRect(secondCorner, secondCorner, secondDimension,
                    secondDimension, secondArcDimension, secondArcDimension);
                pen.dispose();
                return ImageIcon(temp);
            }
            Icon defaultFixtureIcon = createDefaultFixtureIcon();
            Icon? getIconForFile(String filename) {
                try {
                    return loadIcon(filename);
                }  catch (FileNotFoundException|NoSuchFileException except) {
                    log.error("Image file images/``filename`` not found`");
                    log.debug("with stack trace", except);
                    return null;
                } catch (IOException except) {
                    log.error("I/O error reading image", except);
                    return null;
                }
            }
            String jobCSL(IWorker worker) {
                {IJob*} iter = CeylonIterable(worker);
                StringBuilder builder = StringBuilder();
                if (exists first = iter.first) {
                    builder.append(" (```first.name`` ``first.level``");
                    for (job in iter.rest) {
                        builder.append(", ``job.name`` ``job.level``");
                    }
                    builder.append(")");
                    return builder.string;
                } else {
                    return "";
                }
            }
            Icon getIcon(HasImage obj) {
                String image = obj.image;
                if (!image.empty, exists icon = getIconForFile(image)) {
                    return icon;
                } else if (exists icon = getIconForFile(obj.defaultImage)) {
                    return icon;
                } else {
                    return defaultFixtureIcon;
                }
            }
            shared actual Component getTreeCellRendererComponent(JTree? tree,
                    Object? item, Boolean selected, Boolean expanded, Boolean leaf,
                    Integer row, Boolean hasFocus) {
                assert (exists tree, exists item);
                Component component = super.getTreeCellRendererComponent(tree, item,
                    selected, expanded, leaf, row, hasFocus);
                Object internal;
                if (is DefaultMutableTreeNode item) {
                    internal = item.userObject;
                } else {
                    internal = item;
                }
                if (is HasImage internal, is JLabel component) {
                    component.icon = getIcon(internal);
                }
                if (is IWorker internal, is JLabel component) {
                    if ("human" == internal.race) {
                        component.text = "<html><p>``internal
                            .name````jobCSL(internal)``</p></html>";
                    } else {
                        component.text = "<html><p>``internal.name``, a ``internal
                            .race````jobCSL(internal)``</p></html>";
                    }
                } else if (is IUnit internal, is DefaultTreeCellRenderer component) {
                    component.text = internal.name;
                    String orders = internal.getLatestOrders(turnSource()).lowercased;
                    if (orderCheck, orders.contains("fixme"),
                            !CeylonIterable(internal).empty) {
                        component.backgroundSelectionColor = Color.pink;
                        component.backgroundNonSelectionColor = Color.pink;
                    } else if (orderCheck, orders.contains("todo"),
                            !CeylonIterable(internal).empty) {
                        component.backgroundSelectionColor = Color.yellow;
                        component.backgroundNonSelectionColor = Color.yellow;
                    }
                } else if (orderCheck, is WorkerTreeModelAlt.KindNode item) {
                    variable Boolean shouldWarn = false;
                    for (child in CeylonIterable(item)) {
                        if (is WorkerTreeModelAlt.UnitNode child) {
                            if (exists unit = child.userObject, !CeylonIterable(unit).empty) {
                                String orders = unit.getLatestOrders(turnSource()).lowercased;
                                if (orders.contains("fixme"),
                                        is DefaultTreeCellRenderer component) {
                                    component.backgroundSelectionColor = Color.pink;
                                    component.backgroundNonSelectionColor = Color.pink;
                                    shouldWarn = false;
                                    break;
                                } else if (orders.contains("todo")) {
                                    shouldWarn = true;
                                }
                            }
                        }
                    }
                    if (shouldWarn, is DefaultTreeCellRenderer component) {
                        component.backgroundSelectionColor = Color.yellow;
                        component.backgroundNonSelectionColor = Color.yellow;
                    }
                }
                return component;
            }
        }
        cellRenderer = unitMemberCellRenderer;
        shared actual String? getToolTipText(MouseEvent event) {
            if (getRowForLocation(event.x, event.y) == -1) {
                return null;
            }
            if (exists path = getPathForLocation(event.x, event.y),
                    exists pathLast = path.lastPathComponent) {
                if (is IWorker localNode = wtModel.getModelObject(pathLast)) {
                    if (exists stats = localNode.stats) {
                        StringBuilder temp = StringBuilder();
                        temp.append("<html><p>");
                        for ([desc, func] in {["Str", WorkerStats.strength],
                                ["Dex", WorkerStats.dexterity],
                                ["Con", WorkerStats.constitution],
                                ["Int", WorkerStats.intelligence],
                                ["Wis", WorkerStats.wisdom],
                                ["Cha", WorkerStats.charisma]}) {
                            temp.append(desc);
                            temp.append(" ");
                            temp.append(WorkerStats.getModifierString(func(stats)));
                            if ("Cha" != desc) {
                                temp.append(", ");
                            }
                        }
                        temp.append("</p></html>");
                        return temp.string;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        MutableList<UnitSelectionListener> selectionListeners =
                ArrayList<UnitSelectionListener>();
        MutableList<UnitMemberListener> memberListeners =
                ArrayList<UnitMemberListener>();
        shared actual void addUnitMemberListener(UnitMemberListener listener) =>
                memberListeners.add(listener);
        shared actual void addUnitSelectionListener(UnitSelectionListener listener)
                => selectionListeners.add(listener);
        shared actual void removeUnitMemberListener(UnitMemberListener listener) =>
                memberListeners.remove(listener);
        shared actual void removeUnitSelectionListener(UnitSelectionListener listener)
                => selectionListeners.remove(listener);
        object tsl satisfies TreeSelectionListener {
            shared actual void valueChanged(TreeSelectionEvent event) {
                if (exists path = event.newLeadSelectionPath,
                        exists pathLast = path.lastPathComponent) {
                    Object? sel = wtModel.getModelObject(pathLast);
                    if (is UnitMember? sel) {
                        for (listener in memberListeners) {
                            listener.memberSelected(null, sel);
                        }
                    }
                    if (is IUnit sel) {
                        for (listener in selectionListeners) {
                            listener.selectUnit(sel);
                        }
                        for (listener in memberListeners) {
                            listener.memberSelected(null, ProxyWorker(sel));
                        }
                    } else if (!sel exists) {
                        for (listener in selectionListeners) {
                            listener.selectUnit(null);
                        }
                    }
                }
            }
        }
        addTreeSelectionListener(tsl);
        variable Integer i = 0;
        while (i < rowCount) {
            expandRow(i);
            i++;
        }
    }
    object tml satisfies TreeModelListener {
        shared actual void treeStructureChanged(TreeModelEvent event) {
            if (exists path = event.treePath, exists parent = path.parentPath) {
                retval.expandPath(parent);
            }
            variable Integer i = 0;
            while (i < retval.rowCount) {
                retval.expandRow(i);
                i++;
            }
            retval.updateUI();
        }
        shared actual void treeNodesRemoved(TreeModelEvent event) => retval.updateUI();
        shared actual void treeNodesInserted(TreeModelEvent event) {
            if (exists path = event.treePath) {
                retval.expandPath(path);
                if (exists parent = path.parentPath) {
                    retval.expandPath(parent);
                }
            }
            retval.updateUI();
        }
        shared actual void treeNodesChanged(TreeModelEvent event) {
            if (exists path = event.treePath, exists parent = path.parentPath) {
                retval.expandPath(parent);
            }
            retval.updateUI();
        }
    }
    wtModel.addTreeModelListener(tml);
    ToolTipManager.sharedInstance().registerComponent(retval);
    object treeMouseListener extends MouseAdapter() {
        void handleMouseEvent(MouseEvent event) {
            if (event.popupTrigger, event.clickCount == 1,
                    exists path = retval.getClosestPathForLocation(event.x, event.y),
                    exists pathEnd = path.lastPathComponent,
                    is IFixture obj = wtModel.getModelObject(pathEnd)) {
                fixtureEditMenu(obj, players, wtModel).show(event.component, event.x,
                    event.y);
            }
        }
        shared actual void mouseClicked(MouseEvent event) => handleMouseEvent(event);
        shared actual void mousePressed(MouseEvent event) => handleMouseEvent(event);
        shared actual void mouseReleased(MouseEvent event) => handleMouseEvent(event);
    }
    retval.addMouseListener(treeMouseListener);
    return retval;
}
"An interface to help simplify form management"
todo("Do we really need this in Ceylon?")
interface Applyable {
    """Method to call when an "Apply" button is pressed."""
    shared formal void apply();
}
"An interface to, together with [[Applyable]], simplify form management."
interface Revertible {
    """Method to call when a "Revert" button is pressed."""
    shared formal void revert();
}
"A panel for the user to enter a unit's orders or read a unit's results."
JPanel&Applyable&Revertible&TreeSelectionListener&PlayerChangeListener ordersPanel(
        Integer currentTurn, variable Player currentPlayer,
        Iterable<IUnit>(Player, String) playerUnits,
        String(IUnit, Integer) ordersSupplier,
        Anything(IUnit, Integer, String)? ordersConsumer) {
    JTextArea area = JTextArea();
    Integer minimumTurn = (currentTurn<0) then currentTurn else - 1;
    Integer maximumTurn = (currentTurn>100) then currentTurn else 100;
    SpinnerNumberModel spinnerModel = SpinnerNumberModel(currentTurn, minimumTurn,
        maximumTurn, 1);
    object retval extends BorderedPanel()
            satisfies Applyable&Revertible&TreeSelectionListener&PlayerChangeListener {
        variable Anything selection = null;
        "If a unit is selected, change its orders to what the user wrote."
        shared actual void apply() {
            if (is IUnit sel = selection) {
                if (exists ordersConsumer) {
                    ordersConsumer(sel, spinnerModel.number.intValue(), area.text);
                }
                parent.parent.repaint();
            }
        }
        "Change the text in the area to either the current orders, if a unit is selected,
          or the empty string, if one is not."
        shared actual void revert() {
            if (is IUnit sel = selection) {
                area.text = ordersSupplier(sel, spinnerModel.number.intValue());
            } else {
                area.text = "";
            }
        }
        "Handle a changed value in the tree."
        shared actual void valueChanged(TreeSelectionEvent event) {
            if (exists selectedPath = event.newLeadSelectionPath) {
                value sel = selectedPath.lastPathComponent;
                Object temp;
                if (is DefaultMutableTreeNode sel) {
                    temp = sel.userObject;
                } else {
                    temp = sel;
                }
                if (is String temp) {
                    ProxyUnit proxyUnit = ProxyUnit(temp);
                    for (unit in playerUnits(currentPlayer, temp)) {
                        proxyUnit.addProxied(unit);
                    }
                    selection = proxyUnit;
                } else {
                    selection = temp;
                }
                revert();
            }
        }
        shared actual void playerChanged(Player? old, Player newPlayer) {
            currentPlayer = newPlayer;
        }
    }
    if (exists ordersConsumer) {
        JButton applyButton = listenedButton("Apply",
            (ActionEvent event) => retval.apply());
        JButton revertButton = listenedButton("Revert",
            (ActionEvent event) => retval.revert());
        platform.makeButtonsSegmented(applyButton, revertButton);
        JPanel buttonPanel = (platform.systemIsMac) then
                centeredHorizontalBox(applyButton, revertButton)
                else BorderedPanel.horizontalPanel(applyButton, null, revertButton);
        String prefix = platform.shortcutDescription;
        retval.pageStart = BorderedPanel.horizontalPanel(
            JLabel("Orders for current selection, if a unit: (``prefix``D)"), null,
            BorderedPanel.horizontalPanel(null, JLabel("Turn "),
                JSpinner(spinnerModel)));
        retval.pageEnd = buttonPanel;
    } else {
        retval.pageStart = BorderedPanel.horizontalPanel(
            JLabel("Results for current selection, if a unit"), null,
            BorderedPanel.horizontalPanel(null, JLabel("Turn "),
                JSpinner(spinnerModel)));
    }
    retval.center = JScrollPane(area);
    area.lineWrap = true;
    area.wrapStyleWord = true;
    spinnerModel.addChangeListener((event) => retval.revert());
    object modifiedEnterListener extends KeyAdapter() {
        shared actual void keyPressed(KeyEvent event) {
            if (event.keyCode == KeyEvent.vkEnter, platform.hotKeyPressed(event)) {
                retval.apply();
            }
        }
    }
    area.addKeyListener(modifiedEnterListener);
    Integer keyMask = platform.shortcutMask;
    createHotKey(retval, "openOrders", ActionWrapper((event) {
        Boolean newlyGainingFocus = !area.focusOwner;
        area.requestFocusInWindow();
        if (newlyGainingFocus) {
            area.selectAll();
        }
    }), JComponent.whenInFocusedWindow, KeyStroke.getKeyStroke(KeyEvent.vkD, keyMask));
    return retval;
}
"A class to write a proto-strategy to file."
class StrategyExporter(IWorkerModel model, SPOptions options) satisfies PlayerChangeListener {
    variable Player currentPlayer = model.map.currentPlayer;
    shared actual void playerChanged(Player? old, Player newPlayer) =>
            currentPlayer = newPlayer;
    void writeMember(Writer writer, UnitMember? member) {
        if (is IWorker member) {
            writer.write(member.name);
            Iterable<IJob> iter = CeylonIterable(member);
            if (exists first = iter.first) {
                writer.write(" (``first.name`` ``first.level``");
                for (job in iter.rest) {
                    writer.write(", ``job.name`` ``job.level``");
                }
                writer.write(")");
            }
        } else if (exists member) {
            writer.write(member.string);
        }
    }
    shared void writeStrategy(Resource path, {UnitMember*} dismissed) {
        File file;
        if (is Nil path) {
            file = path.createFile();
        } else if (is File path) {
            file = path;
        } else {
            throw IllegalStateException("Can't write to a directory or link");
        }
        try (writer = file.Overwriter()) {
            String playerName = currentPlayer.name;
            Integer turn = model.map.currentTurn;
            {IUnit*} units = CeylonIterable(model.getUnits(currentPlayer))
                .sequence();
            MutableMap<String, MutableList<IUnit>> unitsByKind =
                    HashMap<String, MutableList<IUnit>>();
            for (unit in units) {
                if (!unit.iterator().hasNext(),
                    "false" == options.getArgument("--print-empty")) {
                    continue;
                }
                if (exists list = unitsByKind.get(unit.kind)) {
                    list.add(unit);
                } else {
                    MutableList<IUnit> list = ArrayList<IUnit>();
                    list.add(unit);
                    unitsByKind.put(unit.kind, list);
                }
            }
            MutableMap<IUnit, String> orders = HashMap<IUnit, String>();
            for (kind->list in unitsByKind) {
                for (unit in list) {
                    String unitOrders = unit.getLatestOrders(turn);
                    if (unitOrders == unit.getOrders(turn)) {
                        orders.put(unit, unitOrders);
                    } else {
                        orders.put(unit, "(From turn #``unit
                            .getOrdersTurn(unitOrders)``) ``unitOrders``");
                    }
                }
            }
            writer.writeLine("[``playerName``");
            writer.writeLine("Turn ``turn``]");
            writer.writeLine();
            writer.writeLine();
            writer.writeLine("Inventions: TODO: any?");
            writer.writeLine();
            if (!dismissed.empty) {
                writer.write("Dismissed workers etc.: ``dismissed
                    .first else ""``");
                for (member in dismissed.rest) {
                    writer.write(", ");
                    if (is HasName member) {
                        writer.write(member.name);
                    } else {
                        writer.write(member.string);
                    }
                }
                writer.writeLine();
                writer.writeLine();
            }
            writer.write("Workers:");
            for (kind->list in unitsByKind) {
                writer.writeLine("* ``kind``:");
                for (unit in list) {
                    Iterable<UnitMember> iter = CeylonIterable(unit);
                    writer.write("  - ``unit.name``");
                    if (!iter.empty) {
                        writer.write(" [");
                        writeMember(writer, iter.first);
                        for (member in iter.rest) {
                            writer.write(", ");
                            writeMember(writer, member);
                        }
                        writer.write("]");
                    }
                    writer.writeLine(":");
                    writer.writeLine();
                    if (exists unitOrders = orders.get(unit), !unitOrders.empty) {
                        writer.writeLine(unitOrders);
                    } else {
                        writer.writeLine("TODO");
                    }
                    writer.writeLine();
                    writer.writeLine();
                }
            }
        }
    }
}
"A set of menus for the worker GUI (and other related apps)."
JMenuBar workerMenu(
        "The broker that handles menu items, or arranges for them to be handled"
        Anything(ActionEvent) handler,
        """The window this is to be attached to, whic should close on "Close"."""
        JFrame parentFrame,
        "The current driver model."
        IDriverModel model) {
    SPMenu retval = SPMenu();
    retval.add(retval.createFileMenu(handler, model));
    retval.addDisabled(retval.createMapMenu(handler, model));
    retval.add(retval.createViewMenu(handler, model));
    retval.add(WindowMenu(parentFrame));
    return retval;
}
"An interface for classes listening for directives to expand or collapse trees."
interface TreeExpansionOrderListener {
    "Expand a tree entirely."
    shared formal void expandAll();
    "Collapse a tree entirely."
    shared formal void collapseAll();
    "Expand a tree to a certain level."
    shared formal void expandSome(
        "How many levels from the root, counting the root, to expand."
        Integer levels);
}
"""A class to handle "expand all," "collapse all," etc."""
class TreeExpansionHandler(JTree tree) satisfies TreeExpansionOrderListener {
    "Expand all rows of the tree."
    shared actual void expandAll() {
        variable Integer i = 0;
        while (i < tree.rowCount) {
            tree.expandRow(i);
            i++;
        }
    }
    "Collapse all rows of the tree."
    shared actual void collapseAll() {
        variable Integer i = tree.rowCount - 1;
        while (i >= 0) {
            if (i < tree.rowCount) {
                tree.collapseRow(i);
            }
            i--;
        }
    }
    "Expand some rows of the tree."
    shared actual void expandSome(
            "How many levels from the root, inclusive, to expand."
            Integer levels) {
        variable Integer i = 0;
        while (i < tree.rowCount) {
            if (exists path = tree.getPathForRow(i), path.pathCount <= levels) {
                tree.expandRow(i);
            }
            i++;
        }
    }
}
"A panel to show the details of the currently selected unit-member."
JPanel&UnitMemberListener memberDetailPanel(JPanel resultsPanel) {
    JPanel statPanel = JPanel();
    FunctionalGroupLayout statLayout = FunctionalGroupLayout(statPanel);
    statPanel.layout = statLayout;
    statPanel.border = BorderFactory.createEmptyBorder();
    statLayout.autoCreateGaps = true;
    statLayout.autoCreateContainerGaps = true;
    class StatLabel(Integer(WorkerStats ) stat) extends JLabel("+NaN") {
        shared void recache(WorkerStats? stats) {
            if (exists stats) {
                text = WorkerStats.getModifierString(stat(stats));
            } else {
                text = "";
            }
        }
    }
    StatLabel strLabel = StatLabel(WorkerStats.strength);
    StatLabel dexLabel = StatLabel(WorkerStats.dexterity);
    StatLabel conLabel = StatLabel(WorkerStats.constitution);
    StatLabel intLabel = StatLabel(WorkerStats.intelligence);
    StatLabel wisLabel = StatLabel(WorkerStats.wisdom);
    StatLabel chaLabel = StatLabel(WorkerStats.charisma);
    StatLabel[6] statLabels = [strLabel, dexLabel, conLabel, intLabel, wisLabel, chaLabel];
    JLabel caption(String string) {
        return JLabel("<html><b>``string``:</b></html>");
    }
    JLabel typeCaption = caption("Member Type");
    JLabel typeLabel = JLabel("member type");
    JLabel nameCaption = caption("Name");
    JLabel nameLabel = JLabel("member name");
    JLabel kindCaption = caption("Race or Kind");
    JLabel kindLabel = JLabel("member kind");
    JLabel strCaption = caption("Str");
    JLabel dexCaption = caption("Dex");
    JLabel conCaption = caption("Con");
    JLabel intCaption = caption("Int");
    JLabel wisCaption = caption("Wis");
    JLabel chaCaption = caption("Cha");
    JLabel jobsCaption = JLabel("Job Levels");
    JPanel jobsPanel = JPanel(GridLayout(0, 1));
    statLayout.setVerticalGroup(statLayout.createSequentialGroupOf(
        statLayout.createParallelGroupOf(typeCaption, typeLabel),
        statLayout.createParallelGroupOf(nameCaption, nameLabel),
        statLayout.createParallelGroupOf(kindCaption, kindLabel),
        statLayout.createParallelGroupOf(strCaption, strLabel, intCaption, intLabel),
        statLayout.createParallelGroupOf(dexCaption, dexLabel, wisCaption, wisLabel),
        statLayout.createParallelGroupOf(conCaption, conLabel, chaCaption, chaLabel),
        statLayout.createParallelGroupOf(jobsCaption, jobsPanel)));
    statLayout.setHorizontalGroup(statLayout.createParallelGroupOf(
        statLayout.createSequentialGroupOf(
            statLayout.createParallelGroupOf(typeCaption, nameCaption, kindCaption,
                statLayout.createSequentialGroupOf(strCaption, strLabel),
                statLayout.createSequentialGroupOf(dexCaption, dexLabel),
                statLayout.createSequentialGroupOf(conCaption, conLabel), jobsCaption),
            statLayout.createParallelGroupOf(typeLabel, nameLabel, kindLabel,
                statLayout.createSequentialGroupOf(intCaption, intLabel),
                statLayout.createSequentialGroupOf(wisCaption, wisLabel),
                statLayout.createSequentialGroupOf(chaCaption, chaLabel), jobsPanel))));
    statLayout.linkSize(SwingConstants.horizontal, typeCaption, nameCaption, kindCaption,
        jobsCaption);
    statLayout.linkSize(SwingConstants.horizontal, typeLabel, nameLabel, kindLabel,
        jobsPanel);
    statLayout.linkSize(strCaption, dexCaption, conCaption, intCaption, wisCaption,
        chaCaption);
    statLayout.linkSize(*statLabels);
    statLayout.linkSize(SwingConstants.vertical, typeCaption, typeLabel);
    statLayout.linkSize(SwingConstants.vertical, nameCaption, nameLabel);
    statLayout.linkSize(SwingConstants.vertical, kindCaption, kindLabel);
    object portraitComponent extends JComponent() {
        shared variable Image? portrait = null;
        shared actual void paintComponent(Graphics pen) {
            super.paintComponent(pen);
            if (exists local = portrait) {
                pen.drawImage(local, 0, 0, width, height, this);
            }
        }
    }
    JComponent split = verticalSplit(0.5, 0.5, horizontalSplit(0.6, 0.6, statPanel,
        portraitComponent), resultsPanel);
    split.border = BorderFactory.createEmptyBorder();
    variable UnitMember? current = null;
    void recache() {
        UnitMember? local = current;
        if (is Worker local) { // TODO: IWorker interface?
            typeLabel.text = "Worker";
            nameLabel.text = local.name;
            kindLabel.text = local.kind;
            WorkerStats? stats = local.stats;
            for (label in statLabels) {
                label.recache(stats);
            }
            jobsPanel.removeAll();
            for (job in local) {
                if (!job.empty) {
                    JLabel label = JLabel("``job.name`` ``job.level``");
                    {ISkill*} skills = CeylonIterable(job);
                    if (exists firstSkill = skills.first) {
                        StringBuilder skillsBuilder = StringBuilder();
                        skillsBuilder.append("Skills: ``firstSkill.name`` ``firstSkill
                            .level``");
                        for (skill in skills.rest) {
                            skillsBuilder.append(", ``skill.name`` ``skill.level``");
                        }
                        label.toolTipText = skillsBuilder.string;
                    }
                    jobsPanel.add(label);
                }
            }
        } else if (is Animal local) {
            typeLabel.text = "Animal";
            nameLabel.text = "";
            kindLabel.text = local.kind;
            for (label in statLabels) {
                label.recache(null);
            }
            jobsPanel.removeAll();
        } else if (exists local) {
            typeLabel.text = "Unknown";
            nameLabel.text = "";
            kindLabel.text = type(local).declaration.name;
            for (label in statLabels) {
                label.recache(null);
            }
            jobsPanel.removeAll();
        } else {
            typeLabel.text = "";
            nameLabel.text = "";
            kindLabel.text = "";
            for (label in statLabels) {
                label.recache(null);
            }
            jobsPanel.removeAll();
        }
        portraitComponent.portrait = null;
        if (is HasPortrait local) {
            String portraitName = local.portrait;
            if (!portraitName.empty) {
                try {
                    portraitComponent.portrait = loadImage(portraitName);
                } catch (IOException except) {
                    log.warn("Failed to load portrait", except);
                }
            }
        }
        portraitComponent.repaint();
    }
    object retval extends JPanel(BorderLayout()) satisfies UnitMemberListener {
        shared actual void memberSelected(UnitMember? old, UnitMember? selected) {
            if (is ProxyFor<out Anything> selected) {
                assert (is ProxyFor<out UnitMember> selected);
                if (selected.parallel) {
                    if (exists first = CeylonIterable(selected.proxied).first) {
                        memberSelected(old, first);
                        return;
                    }
                } else {
                    memberSelected(old, null);
                    return;
                }
            }
            if (exists selected) {
                if (exists temp = current, selected == temp) {
                } else {
                    current = selected;
                    recache();
                }
            } else if (current exists) {
                current = null;
                recache();
            }
        }
    }
    retval.add(JLabel("<html><h2>Unit Member Details:</h2></html>"),
        BorderLayout.pageStart);
    retval.add(split);
    recache();
    return retval;
}
"A window to let the player manage units."
SPFrame&PlayerChangeListener workerMgmtFrame(SPOptions options,
        IWorkerModel model, MenuBroker menuHandler) {
    Point findHQ() {
        variable Point retval = PointFactory.invalidPoint;
        for (location in model.map.locations()) {
            for (fixture in model.map.getOtherFixtures(location)) {
                if (is Fortress fixture, fixture.owner == model.map.currentPlayer) {
                    if ("HQ" == fixture.name) {
                        return location;
                    } else if (location.valid, !retval.valid) {
                        retval = location;
                    }
                }
            }
        }
        return retval;
    }
    IViewerModel getViewerModel() {
        for (frame in WindowList.getFrames(false, true, true)) {
            if (is IViewerFrame frame, frame.model.mapFile == model.mapFile) {
                frame.toFront();
                if (frame.extendedState == Frame.iconified) {
                    frame.extendedState = Frame.normal;
                }
                return frame.model;
            }
        } else {
            SPFrame&IViewerFrame frame = viewerFrame(ViewerModel(model.map, model.mapFile),
                menuHandler.actionPerformed);
            SwingUtilities.invokeLater(() => frame.setVisible(true));
            return frame.model;
        }
    }
    JTree createReportTree(DefaultTreeModel reportModel) {
        JTree report = JTree(reportModel);
        report.rootVisible = true;
        assert (is DefaultMutableTreeNode root = reportModel.root);
        report.expandPath(TreePath(root.path));
        DistanceComparator calculator = DistanceComparator(findHQ());
        object renderer extends DefaultTreeCellRenderer() {
            shared actual Component getTreeCellRendererComponent(JTree tree, Object val,
                    Boolean selected, Boolean expanded, Boolean leaf, Integer row,
                    Boolean hasFocus) {
                assert (is JComponent retval =
                        super.getTreeCellRendererComponent(tree, val, selected, expanded,
                            leaf, row, hasFocus));
                if (is IReportNode val) {
                    Point point = val.point;
                    if (point.valid) {
                        retval.toolTipText = calculator.distanceString(point);
                    } else {
                        retval.toolTipText = null;
                    }
                }
                return retval;
            }
        }
        report.cellRenderer = renderer;
        ToolTipManager.sharedInstance().registerComponent(report);
        object reportMouseHandler extends MouseAdapter() {
            shared actual void mousePressed(MouseEvent event) {
                if (exists selPath = report.getPathForLocation(event.x, event.y)) {
                    value node = selPath.lastPathComponent;
                    if (platform.hotKeyPressed(event), is IReportNode node) {
                        Point point = node.point;
                        if (point.valid) {
                            IViewerModel viewerModel = getViewerModel();
                            SwingUtilities.invokeLater(() =>
                                viewerModel.setSelection(point));
                        }
                    }
                }
            }
        }
        report.addMouseListener(reportMouseHandler);
        return report;
    }
    object retval extends SPFrame("Worker Management", model.mapFile.orElse(null),
                Dimension(640, 480))
            satisfies PlayerChangeListener {
        IMapNG mainMap = model.map;
        SPDialog&NewUnitSource&PlayerChangeListener newUnitFrame =
                newUnitDialog(mainMap.currentPlayer,
                    IDFactoryFiller.createFactory(mainMap));
        IWorkerTreeModel treeModel = WorkerTreeModelAlt(mainMap.currentPlayer, model);
        JTree tree = workerTree(treeModel, CeylonIterable(mainMap.players()),
            () => mainMap.currentTurn, true);
        newUnitFrame.addNewUnitListener(treeModel);
        Integer keyMask = platform.shortcutMask;
        createHotKey(tree, "openUnits", ActionWrapper(
            (ActionEvent event) => tree.requestFocusInWindow()),
            JComponent.whenInFocusedWindow,
            KeyStroke.getKeyStroke(KeyEvent.vkU, keyMask));
        FormattedLabel playerLabel = FormattedLabel("Units belonging to %s: (%sU)",
            mainMap.currentPlayer.name, platform.shortcutDescription);
        value ordersPanelObj = ordersPanel(mainMap.currentTurn, mainMap.currentPlayer,
            (Player player, String kind) => CeylonIterable(model.getUnits(player, kind)),
            (IUnit unit, Integer turn) => unit.getLatestOrders(turn),
            (IUnit unit, Integer turn, String orders) => unit.setOrders(turn, orders));
        tree.addTreeSelectionListener(ordersPanelObj);
        DefaultTreeModel reportModel = DefaultTreeModel(SimpleReportNode(
            "Please wait, loading report ..."));
        void reportGeneratorThread() {
            log.info("About to generate report");
            IReportNode report = createAbbreviatedReportIR(mainMap,
                mainMap.currentPlayer);
            log.info("Finished generating report");
            SwingUtilities.invokeLater(() => reportModel.setRoot(report));
        }
        Thread(reportGeneratorThread).start();
        value resultsPanel = ordersPanel(mainMap.currentTurn, mainMap.currentPlayer,
            (Player player, String kind) => CeylonIterable(model.getUnits(player, kind)),
            (IUnit unit, Integer turn) => unit.getResults(turn), null);
        tree.addTreeSelectionListener(resultsPanel);
        JPanel&UnitMemberListener mdp = memberDetailPanel(resultsPanel);
        StrategyExporter strategyExporter = StrategyExporter(model, options);
        BorderedPanel lowerLeft = BorderedPanel.verticalPanel(
            listenedButton("Add New Unit", (event) => newUnitFrame.setVisible(true)),
            ordersPanelObj, listenedButton("Export a proto-strategy",
                (ActionEvent event) => FileChooser.save(null, JFileChooser(".", null))
                    .call((file) => strategyExporter.writeStrategy(
                        parsePath(file.string).resource,
                        CeylonIterable(treeModel.dismissed())))));
        contentPane = horizontalSplit(0.5, 0.5, verticalSplit(2.0 / 3.0, 2.0 / 3.0,
                BorderedPanel.verticalPanel(playerLabel, JScrollPane(tree), null), lowerLeft),
            verticalSplit(0.6, 0.6, BorderedPanel.verticalPanel(
                    JLabel("The contents of the world you know about, for reference:"),
                    JScrollPane(createReportTree(reportModel)), null),
                mdp));
        TreeExpansionOrderListener expander = TreeExpansionHandler(tree);
        menuHandler.register((event) => expander.expandAll(), "expand all");
        menuHandler.register((event) => expander.collapseAll(), "collapse all");
        menuHandler.register((event) => expander.expandSome(2), "expand unit kinds");
        expander.expandAll();
        object closeListener extends WindowAdapter() {
            shared actual void windowClosed(WindowEvent event) => newUnitFrame.dispose();
        }
        addWindowListener(closeListener);
        model.addMapChangeListener(() => Thread(reportGeneratorThread).start());
        object reportUpdater satisfies PlayerChangeListener {
            shared actual void playerChanged(Player? old, Player newPlayer) =>
                    Thread(reportGeneratorThread).start();
        }
        {PlayerChangeListener+} pcListeners = { newUnitFrame, treeModel, ordersPanelObj,
            reportUpdater, resultsPanel };
        shared actual void playerChanged(Player? old, Player newPlayer) {
            for (listener in pcListeners) {
                listener.playerChanged(old, newPlayer);
            }
            playerLabel.setArgs(newPlayer.name, platform.shortcutDescription);
        }
        shared actual String windowName = "Worker Management";
    }
    retval.jMenuBar = workerMenu(menuHandler.actionPerformed, retval, model);
    retval.pack();
    return retval;
}
"A driver to start the worker management GUI."
object workerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Manage a player's workers in units";
        longDescription = "Organize the members of a player's units.";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            menuHandler.register(pcml, "change current player");
            menuHandler.register((event) => process.exit(0), "quit");
            SwingUtilities.invokeLater(() {
                value frame = workerMgmtFrame(options, model, menuHandler);
                pcml.addPlayerChangeListener(frame);
                menuHandler.register((event) => frame.playerChanged(
                        model.map.currentPlayer, model.map.currentPlayer),
                    "reload tree");
                menuHandler.register(WindowCloser(frame), "close");
                menuHandler.register((event) =>
                    aboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}
"A command-line program to export a proto-strategy for a player from orders in a map."
object strategyExportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.one;
        shortDescription = "Export a proto-strategy";
        longDescription = "Create a proto-strategy using orders stored in the map";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty", "--export=filename.txt" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            if (options.hasOption("--export")) {
                StrategyExporter(model, options).writeStrategy(parsePath(
                        options.getArgument("--export")).resource, {});
            } else {
                throw DriverFailedException(
                    IllegalStateException("--export option is required"),
                    "--export option is required");
            }
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}