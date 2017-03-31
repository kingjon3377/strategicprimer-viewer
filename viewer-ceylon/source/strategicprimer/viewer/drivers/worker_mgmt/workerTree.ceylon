import java.awt.image {
    BufferedImage
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
import java.awt {
    Component,
    Graphics2D,
    Color
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    ProxyWorker,
    WorkerStats
}
import model.map {
    HasImage,
    Player
}
import strategicprimer.viewer.model.map {
    IFixture
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
    Icon
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit,
    IWorker
}
import strategicprimer.viewer.drivers.advancement {
    UnitSelectionSource,
    UnitSelectionListener
}
import strategicprimer.viewer.drivers.map_viewer {
    loadIcon,
    fixtureEditMenu
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
import ceylon.math.float {
    halfEven
}
"A tree of a player's units."
shared JTree&UnitMemberSelectionSource&UnitSelectionSource workerTree(
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
                MutableList<[UnitMember, IUnit]> toTransfer =
                        ArrayList<[UnitMember, IUnit]>();
                for (path in paths) {
                    if (exists last = path.lastPathComponent,
                        exists parentPath = path.parentPath,
                        exists parentObj = parentPath.lastPathComponent) {
                        if (is IUnit parent = wtModel.getModelObject(parentObj),
                            is UnitMember selection = wtModel.getModelObject(last)) {
                            toTransfer.add([selection, parent]);
                        } else {
                            log.info("Selection included non-UnitMember");
                        }
                    }
                }
                if (toTransfer.empty) {
                    return null;
                } else {
                    return UnitMemberTransferable(*toTransfer);
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
                    exists pathLast = path.lastPathComponent) {
                    Object tempTarget;
                    Object local = wtModel.getModelObject(pathLast);
                    if (is UnitMember local) {
                        TreePath pathParent = path.parentPath;
                        tempTarget = wtModel.getModelObject(pathParent.lastPathComponent);
                    } else {
                        tempTarget = local;
                    }
                    if (is IUnit tempTarget) {
                        try {
                            Transferable trans = support.transferable;
                            assert (is  [UnitMember, IUnit][] list =
                                    trans.getTransferData(UnitMemberTransferable.flavor));
                            for (pair in list) {
                                wtModel.moveMember(pair.first, pair.rest.first, tempTarget);
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
                StringBuilder builder = StringBuilder();
                if (exists first = worker.first) {
                    builder.append(" (```first.name`` ``first.level``");
                    for (job in worker.rest) {
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
                    if (orderCheck, orders.contains("fixme"), !internal.empty) {
                        component.backgroundSelectionColor = Color.pink;
                        component.backgroundNonSelectionColor = Color.pink;
                    } else if (orderCheck, orders.contains("todo"), !internal.empty) {
                        component.backgroundSelectionColor = Color.yellow;
                        component.backgroundNonSelectionColor = Color.yellow;
                    }
                } else if (orderCheck,
                    is WorkerTreeModelAlt.WorkerTreeNode<String> item) {
                    variable Boolean shouldWarn = false;
                    for (child in item) {
                        if (is WorkerTreeModelAlt.WorkerTreeNode<IUnit> child) {
                            IUnit unit = child.userObjectNarrowed;
                            if (!unit.empty) {
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
                            listener.memberSelected(null, ProxyWorker.fromUnit(sel));
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
