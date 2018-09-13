import java.awt.image {
    BufferedImage
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import java.awt {
    Component,
    Graphics2D,
    Color
}
import strategicprimer.model.map.fixtures.mobile.worker {
    ProxyWorker,
    WorkerStats,
    IJob
}
import strategicprimer.model.map {
    HasImage,
    IFixture,
    Player,
    HasMutableKind
}
import javax.swing.event {
    TreeSelectionEvent,
    TreeModelEvent,
    TreeSelectionListener,
    TreeModelListener
}
import java.awt.datatransfer {
    Transferable,
    UnsupportedFlavorException
}
import javax.swing {
    JTree,
    TransferHandler,
    ToolTipManager,
    JComponent,
    JLabel,
    ImageIcon,
    Icon,
	DropMode
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    IWorker,
    Animal,
    maturityModel,
    animalPlurals
}
import strategicprimer.viewer.drivers.map_viewer {
    imageLoader,
    FixtureEditMenu
}
import javax.swing.tree {
    TreePath,
    DefaultMutableTreeNode,
    DefaultTreeCellRenderer
}
import java.awt.event {
    MouseAdapter,
    MouseEvent
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    NoSuchFileException
}
import ceylon.numeric.float {
    halfEven
}
import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import ceylon.language.meta {
    type
}
import strategicprimer.model.idreg {
    IDRegistrar
}
import lovelace.util.common {
	matchingValue
}
"A tree of a player's units."
shared JTree&UnitMemberSelectionSource&UnitSelectionSource workerTree(
        "The tree model"
        IWorkerTreeModel wtModel,
        "The players in the map"
        {Player*} players,
        "How to get the current turn"
        Integer() turnSource,
        """Whether we should visually warn if orders contain substrings indicating
           remaining work or if a unit named "unassigned" is nonempty"""
        Boolean orderCheck,
        "The factory to use to generate ID numbers."
        IDRegistrar idf,
        "A method to call if the user does something to modify the maps."
        Anything() mutationListener) {
    DefaultTreeCellRenderer defaultStorer = DefaultTreeCellRenderer();
    value statReferencesList = [["Str", WorkerStats.strength],
	                            ["Dex", WorkerStats.dexterity],
	                            ["Con", WorkerStats.constitution],
	                            ["Int", WorkerStats.intelligence],
	                            ["Wis", WorkerStats.wisdom],
	                            ["Cha", WorkerStats.charisma]];
    object retval extends JTree()
            satisfies UnitMemberSelectionSource&UnitSelectionSource {
        model = wtModel;
        rootVisible = false;
        dragEnabled = true;
        showsRootHandles = true;
        dropMode = DropMode.on;
        object workerTreeTransferHandler extends TransferHandler() {
            "Unit members can only be moved, not copied or linked."
            shared actual Integer getSourceActions(JComponent component) =>
                    TransferHandler.move;
            "Create a transferable representing the selected node(s)."
            shared actual UnitMemberTransferable|UnitTransferable? createTransferable(
                    JComponent component) {
                value paths = selectionModel.selectionPaths;
                MutableList<[UnitMember, IUnit]> membersToTransfer =
                        ArrayList<[UnitMember, IUnit]>();
                MutableList<IUnit&HasMutableKind> unitsToTransfer =
                        ArrayList<IUnit&HasMutableKind>();
                for (path in paths) {
                    if (exists last = path.lastPathComponent,
                            exists parentObj = path.parentPath?.lastPathComponent) {
                        if (is IUnit parent = wtModel.getModelObject(parentObj),
	                            is UnitMember selection = wtModel.getModelObject(last)) {
                            membersToTransfer.add([selection, parent]);
                        } else if (is IUnit&HasMutableKind selection =
                                wtModel.getModelObject(last)) {
                            unitsToTransfer.add(selection);
                        } else {
                            log.info("Selection included non-UnitMember: ``
                                type(wtModel.getModelObject(last))``");
                        }
                    }
                }
                if (membersToTransfer.empty) {
                    if (unitsToTransfer.empty) {
                        return null;
                    } else {
                        return UnitTransferable(*unitsToTransfer);
                    }
                } else {
                    return UnitMemberTransferable(*membersToTransfer);
                }
            }
            "Whether a drag here is possible."
            shared actual Boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(UnitMemberTransferable.flavor),
                        is JTree.DropLocation dropLocation = support.dropLocation,
                        exists last = dropLocation.path?.lastPathComponent,
                        is IUnit|UnitMember lastObj = wtModel.getModelObject(last)) {
                    return true;
                } else if (support.isDataFlavorSupported(UnitTransferable.flavor),
                        is JTree.DropLocation dropLocation = support.dropLocation,
                        exists last = dropLocation.path?.lastPathComponent,
                        is String lastObj = wtModel.getModelObject(last)) {
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
                        exists pathLast = path.lastPathComponent) {
                    Object tempTarget;
                    Object local = wtModel.getModelObject(pathLast);
                    if (is UnitMember local) {
                        TreePath pathParent = path.parentPath;
                        tempTarget = wtModel.getModelObject(pathParent.lastPathComponent);
                    } else {
                        tempTarget = local;
                    }
                    Transferable trans = support.transferable;
                    try {
                        if (is IUnit tempTarget,
                                trans.isDataFlavorSupported(UnitMemberTransferable.flavor)) {
                            assert (is [UnitMember, IUnit][] list =
                                    trans.getTransferData(UnitMemberTransferable.flavor));
                            for ([member, unit] in list) {
                                wtModel.moveMember(member, unit, tempTarget);
                            }
                            return true;
                        } else if (is String tempTarget,
                                trans.isDataFlavorSupported(UnitTransferable.flavor)) {
                            assert (is <IUnit&HasMutableKind>[] list =
                                    trans.getTransferData(UnitTransferable.flavor));
                            for (unit in list) {
                                String priorKind = unit.kind;
                                unit.kind = tempTarget;
                                wtModel.moveItem(unit, priorKind);
                            }
                            return true;
                        } else {
                            return false;
                        }
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
                Integer firstDimension = halfEven(imageSize * (1.0 - (margin * 2.0)))
                    .integer;
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
                    return imageLoader.loadIcon(filename);
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
                StringBuilder builder = StringBuilder();
                {IJob*} jobs = worker.filter(matchingValue(false, IJob.emptyJob));
                if (exists first = jobs.first) {
                    builder.append(" (``first.name`` ``first.level``");
                    for (job in jobs.rest) {
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
                variable Boolean shouldWarn = false;
                variable Boolean shouldError = false;
                if (is IWorker internal, is JLabel component) {
                    if ("human" == internal.race) {
                        component.text = "<html><p>``internal
                            .name````jobCSL(internal)``</p></html>";
                    } else {
                        component.text = "<html><p>``internal.name``, a ``internal
                            .race````jobCSL(internal)``</p></html>";
                    }
                } else if (is Animal internal, internal.born >= 0,
                        maturityModel.currentTurn >= 0,
                        exists maturityAge = maturityModel.maturityAges[internal.kind],
                        maturityModel.currentTurn - internal.born < maturityAge,
                        is JLabel component) {
                    Integer age = maturityModel.currentTurn - internal.born;
                    if (internal.population>1) {
                        component.text = "``internal.population`` ``age``-turn-old ``
//                                animalPlurals[internal.kind]``"; // TODO: syntax sugar once compiler bug fixed
                                animalPlurals.get(internal.kind)``";
                    } else {
                        component.text = "``age``-turn-old ``internal.kind``";
                    }
                } else if (is Animal internal, internal.population > 1,
                        is JLabel component) {
//                    component.text = "``internal.population`` ``animalPlurals[internal.kind]``"; // TODO: syntax sugar once compiler bug fixed
                    component.text = "``internal.population`` ``animalPlurals.get(internal
                        .kind)``";
                } else if (is IUnit internal, is DefaultTreeCellRenderer component) {
                    if (expanded || internal.empty) {
                        component.text = internal.name;
                    } else {
                        component.text =
                            "``internal.name`` (``internal.narrow<IWorker>().size`` workers)";
                    }
                    String orders = internal.getLatestOrders(turnSource()).lowercased;
                    if (orderCheck, orders.contains("fixme"), !internal.empty) {
                        shouldError = true;
                    } else if (orderCheck, orders.contains("todo"), !internal.empty) {
                        shouldWarn = true;
                    }
                } else if (orderCheck,
	                    is WorkerTreeModelAlt.WorkerTreeNode<String> item) {
                    for (child in item
                            .narrow<WorkerTreeModelAlt.WorkerTreeNode<IUnit>>()) {
                        IUnit unit = child.userObjectNarrowed;
                        if (!unit.empty) {
                            String orders = unit.getLatestOrders(turnSource())
                                .lowercased;
                            if (orders.contains("fixme")) {
                                shouldError = true;
                                shouldWarn = false;
                                break;
                            } else if (orders.contains("todo")) {
                                shouldWarn = true;
                            }
                        }
                    }
                }
                if (is DefaultTreeCellRenderer component) {
                    if (shouldError) {
                        component.backgroundSelectionColor = Color.pink;
                        component.backgroundNonSelectionColor = Color.pink;
                        component.textSelectionColor = Color.black;
                        component.textNonSelectionColor = Color.black;
                    } else if (shouldWarn) {
                        component.backgroundSelectionColor = Color.yellow;
                        component.backgroundNonSelectionColor = Color.yellow;
                        component.textSelectionColor = Color.black;
                        component.textNonSelectionColor = Color.black;
                    } else {
                        component.backgroundSelectionColor = defaultStorer
                            .backgroundSelectionColor;
                        component.backgroundNonSelectionColor = defaultStorer
                            .backgroundNonSelectionColor;
                        component.textSelectionColor = Color.white;
                        component.textNonSelectionColor = Color.black;
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
            if (exists pathLast =
                    getPathForLocation(event.x, event.y)?.lastPathComponent,
		            is IWorker localNode = wtModel.getModelObject(pathLast),
                    exists stats = localNode.stats) {
                return "<html><p>``", ".join(statReferencesList
                                .map(([desc, func]) => "``desc`` ``WorkerStats.getModifierString(func(stats))``"))``</p></html>";
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
                Anything pathLast = event.newLeadSelectionPath?.lastPathComponent;
                Anything sel;
                if (exists pathLast) {
                    sel = wtModel.getModelObject(pathLast);
                } else {
                    sel = null;
                }
                if (is IUnit sel) {
                    log.debug("Selection in workerTree is an IUnit");
                    for (listener in selectionListeners) {
                        listener.selectUnit(sel);
                    }
                    IWorker proxy = ProxyWorker.fromUnit(sel);
                    for (listener in memberListeners) {
                        listener.memberSelected(null, proxy);
                    }
                } else if (is UnitMember sel) {
                    log.debug("Selection in workerTree is a UnitMember, but not an IUnit");
                    for (listener in selectionListeners) {
                        listener.selectUnit(null);
                    }
                    for (listener in memberListeners) {
                        listener.memberSelected(null, sel);
                    }
                } else {
                    if (is String sel) {
                        log.debug("Selection in workerTree is a String, i.e. a unit-kind node");
                    } else if (!sel exists) {
                        log.debug("Selection in workerTree is null");
                    } else {
                        log.warn("Unexpected type of selection in workerTree: ``type(sel)``");
                    }
                    for (listener in selectionListeners) {
                        listener.selectUnit(null);
                    }
                    for (listener in memberListeners) {
                        listener.memberSelected(null, null);
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
            if (exists parent = event.treePath?.parentPath) {
                retval.expandPath(parent);
            } else if (exists path = event.treePath) {
                retval.expandPath(path);
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
            if (exists parent = event.treePath?.parentPath) {
                retval.expandPath(parent);
            }
            retval.updateUI();
        }
    }
    wtModel.addTreeModelListener(tml);
    assert (is TreeModelListener temp = retval.accessibleContext);
    wtModel.addTreeModelListener(temp);
    ToolTipManager.sharedInstance().registerComponent(retval);
    object treeMouseListener extends MouseAdapter() {
        void handleMouseEvent(MouseEvent event) {
            if (event.popupTrigger, event.clickCount == 1,
                    exists pathEnd = retval
                        .getClosestPathForLocation(event.x, event.y)?.lastPathComponent,
                    is IFixture obj = wtModel.getModelObject(pathEnd)) {
                FixtureEditMenu(obj, players, idf, mutationListener, wtModel).show(event.component, event.x,
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
