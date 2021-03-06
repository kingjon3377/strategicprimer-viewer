import java.awt.image {
    BufferedImage
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import java.awt {
    Component,
    Graphics2D,
    Color
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    ProxyWorker,
    WorkerStats,
    IJob
}
import strategicprimer.model.common.map {
    HasImage,
    IFixture,
    HasMutableKind,
    Player
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
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    IUnit,
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
    DefaultTreeCellRenderer,
    TreeSelectionModel
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
    IOException
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
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import lovelace.util.common {
    as,
    MissingFileException
}
import java.lang {
    JString=String
}

"An enumeration of possible reasons for changing the highlighting of a unit."
class ShadingReason of todo|fixme|missing satisfies Comparable<ShadingReason> {
    Integer ordinal;
    shared Color color;
    "When orders are empty (in the current turn)."
    shared new missing {
        ordinal = 0;
        color = Color(184, 224, 249);
    }
    """When orders contain "TODO" or "XXX"."""
    shared new todo {
        ordinal = 1;
        color = Color.yellow;
    }
    """When orders contain "FIXME"."""
    shared new fixme {
        ordinal = 2;
        color = Color.pink;
    }

    shared actual Comparison compare(ShadingReason other) => ordinal <=> other.ordinal;
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
        IDRegistrar idf) {
    DefaultTreeCellRenderer defaultStorer = DefaultTreeCellRenderer();

    value statReferencesList = [["Str", WorkerStats.strength],
                                ["Dex", WorkerStats.dexterity],
                                ["Con", WorkerStats.constitution],
                                ["Int", WorkerStats.intelligence],
                                ["Wis", WorkerStats.wisdom],
                                ["Cha", WorkerStats.charisma]];

    class WorkerTreeTransferHandler(TreeSelectionModel selectionModel, Boolean(TreePath) isExpanded,
            Anything(TreePath) collapsePath) extends TransferHandler() {
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
                if (!unitsToTransfer.empty) {
                    log.warn("Selection included both units and unit members");
                }
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
                TreePath targetPath;
                if (is UnitMember local) {
                    targetPath = path.parentPath;
                    tempTarget = wtModel.getModelObject(targetPath.lastPathComponent);
                } else {
                    targetPath = path;
                    tempTarget = local;
                }
                Transferable trans = support.transferable;
                Boolean shouldBeExpanded = isExpanded(targetPath);
                try {
                    if (is IUnit tempTarget,
                            trans.isDataFlavorSupported(
                                UnitMemberTransferable.flavor)) {
                        assert (is [UnitMember, IUnit][] list =
                            trans.getTransferData(UnitMemberTransferable.flavor));
                        for ([member, unit] in list) {
                            wtModel.moveMember(member, unit, tempTarget);
                        }
                        if (!shouldBeExpanded) {
                            collapsePath(targetPath);
                        }
                        return true;
                    } else if (is String tempTarget,
                            trans.isDataFlavorSupported(UnitTransferable.flavor)) {
                        assert (is <IUnit&HasMutableKind>[] list =
                            trans.getTransferData(UnitTransferable.flavor));
                        for (unit in list) {
                            wtModel.changeKind(unit, tempTarget);
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
            }  catch (MissingFileException except) {
                log.error("Image file images/``filename`` not found`");
                log.debug("with stack trace", except);
                return null;
            } catch (IOException except) {
                log.error("I/O error reading image", except);
                return null;
            }
        }

        String jobString(IJob job) => "``job.name`` ``job.level``";

        String jobCSL(IWorker worker) {
            {String*} jobs = worker.filter(not(IJob.emptyJob)).map(jobString);
            if (jobs.empty) {
                return "";
            } else {
                return " (``", ".join(jobs)``)";
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

        "Returns [[ShadingReason.fixme]] if orders contain FIXME,
         [[ShadingReason.todo]] if orders contain TODO or XXX,
         [[ShadingReason.missing]] if orders are empty, and [[null]]
         otherwise."
        ShadingReason? shouldChangeBackground(String|JString|IUnit item) {
            switch (item)
            case (is IUnit) {
                if (item.empty) {
                    return null;
                }
                String orders = item.getLatestOrders(turnSource()).lowercased;
                if (orders.contains("fixme")) {
                    return ShadingReason.fixme;
                } else if (orders.contains("todo") || orders.contains("xxx")) {
                    return ShadingReason.todo;
                } else if (orders.empty || item.getOrders(turnSource()).empty) {
                    return ShadingReason.missing;
                } else {
                    return null;
                }
            }
            else {
                variable ShadingReason? retval = null;
                for (unit in wtModel.childrenOf(item).map(wtModel.getModelObject)
                        .narrow<IUnit>()) {
                    if (exists unitPaint = shouldChangeBackground(unit)) {
                        if (unitPaint == ShadingReason.fixme) {
                            return unitPaint;
                        } else if (exists temp = retval) {
                            retval = largest(temp, unitPaint);
                        } else {
                            retval = unitPaint;
                        }
                    }
                }
                return retval;
            }
        }

        shared actual Component getTreeCellRendererComponent(JTree? tree,
                Object? item, Boolean selected, Boolean expanded, Boolean leaf,
                Integer row, Boolean hasFocus) {
            assert (exists tree, exists item);
            Component component = super.getTreeCellRendererComponent(tree, item,
                selected, expanded, leaf, row, hasFocus);
            Object internal = as<DefaultMutableTreeNode>(item)?.userObject else item;
            if (is HasImage internal, is JLabel component) {
                component.icon = getIcon(internal);
            }
            variable ShadingReason? background = null;
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
//                    animalPlurals[internal.kind]``"; // TODO: syntax sugar once compiler bug fixed
                    animalPlurals.get(internal.kind)``";
                } else {
                    component.text = "``age``-turn-old ``internal.kind``";
                }
            } else if (is Animal internal, internal.population > 1,
                    is JLabel component) {
//                component.text = "``internal.population`` ``animalPlurals[internal.kind]``"; // TODO: syntax sugar once compiler bug fixed
                component.text = "``internal.population`` ``animalPlurals.get(internal
                    .kind)``";
            } else if (is IUnit internal, is DefaultTreeCellRenderer component) {
                if (expanded || internal.empty) {
                    component.text = internal.name;
                } else {
                    component.text = "``internal.name`` (``internal.narrow<IWorker>()
                        .size`` workers)";
                }
                if (exists result = shouldChangeBackground(internal)) {
                    if (exists temp = background) {
                        background = largest(result, temp);
                    } else {
                        background = result;
                    }
                }
            } else if (orderCheck, is String|JString internal) {
                if (exists result = shouldChangeBackground(internal)) {
                    if (exists temp = background) {
                        background = largest(result, temp);
                    } else {
                        background = result;
                    }
                }
            }
            if (is DefaultTreeCellRenderer component) {
                if (exists temp = background) {
                    component.backgroundSelectionColor = temp.color;
                    component.backgroundNonSelectionColor = temp.color;
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
    object retval extends JTree()
            satisfies UnitMemberSelectionSource&UnitSelectionSource {
        model = wtModel;
        rootVisible = false;
        dragEnabled = true;
        showsRootHandles = true;
        dropMode = DropMode.on;

        transferHandler = WorkerTreeTransferHandler(selectionModel, (TreePath p) => isExpanded(p), collapsePath);

        cellRenderer = unitMemberCellRenderer;

        String statHelper(WorkerStats stats)([String, Integer(WorkerStats)] tuple) =>
            "``tuple.first`` ``WorkerStats.getModifierString(tuple.rest.first(stats))``";

        shared actual String? getToolTipText(MouseEvent event) {
            if (getRowForLocation(event.x, event.y) == -1) {
                return null;
            }
            if (exists pathLast =
                    getPathForLocation(event.x, event.y)?.lastPathComponent,
                    is IWorker localNode = wtModel.getModelObject(pathLast),
                    exists stats = localNode.stats) {
                return "<html><p>``", ".join(statReferencesList
                    .map(statHelper(stats)))``</p></html>";
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
                    log.debug("workerTree selection is a UnitMember, but not an IUnit");
                    for (listener in selectionListeners) {
                        listener.selectUnit(null);
                    }
                    for (listener in memberListeners) {
                        listener.memberSelected(null, sel);
                    }
                } else {
                    if (is String sel) {
                        log.debug(
                            "workerTree selection is a String, i.e. a unit-kind node");
                    } else if (!sel exists) {
                        log.debug("Selection in workerTree is null");
                    } else {
                        log.warn(
                            "Unexpected type of selection in workerTree: ``type(sel)``");
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

        shared actual void treeNodesRemoved(TreeModelEvent event) {
            value path = event.treePath.pathByAddingChild(event.children.array.first);
            log.trace("About to remove this path from selection: ``path``");
            retval.removeSelectionPath(path);
            retval.updateUI();
        }
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
                FixtureEditMenu(obj, players, idf, wtModel)
                    .show(event.component, event.x, event.y);
            }
        }
        shared actual void mouseClicked(MouseEvent event) => handleMouseEvent(event);
        shared actual void mousePressed(MouseEvent event) => handleMouseEvent(event);
        shared actual void mouseReleased(MouseEvent event) => handleMouseEvent(event);
    }
    retval.addMouseListener(treeMouseListener);
    return retval;
}
