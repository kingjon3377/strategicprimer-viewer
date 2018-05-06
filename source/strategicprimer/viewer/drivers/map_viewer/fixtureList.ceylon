import java.lang {
    ObjectArray
}
import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    Player,
    TileFixture,
    Point
}
import ceylon.interop.java {
    CeylonList
}
import java.awt.datatransfer {
    DataFlavor,
    UnsupportedFlavorException,
    Transferable
}
import java.awt.event {
    MouseAdapter,
    ActionEvent,
    KeyEvent,
    MouseEvent
}
import lovelace.util.jvm {
    createHotKey
}
import java.awt {
    Component
}
import java.io {
    IOException
}
import java.awt.dnd {
    DropTargetAdapter,
    DragSource,
    DropTargetEvent,
    DropTargetDropEvent,
    DnDConstants,
    DropTarget,
    DragGestureListener,
    DragGestureEvent,
    DropTargetDragEvent
}
import javax.swing {
    KeyStroke,
    SwingList=JList,
    ListSelectionModel,
    JComponent
}
import strategicprimer.drivers.common {
    SelectionChangeListener
}
import strategicprimer.model.idreg {
    IDRegistrar
}
"A visual list-based representation of the contents of a tile."
shared SwingList<TileFixture>&DragGestureListener&SelectionChangeListener fixtureList(
        JComponent parentComponent, FixtureListModel listModel, IDRegistrar idf,
        {Player*} players) {
    object retval extends SwingList<TileFixture>(listModel)
            satisfies DragGestureListener&SelectionChangeListener {
        cellRenderer = FixtureCellRenderer();
        selectionMode = ListSelectionModel.multipleIntervalSelection;
        shared actual void dragGestureRecognized(DragGestureEvent event) {
            List<TileFixture> selection = CeylonList(selectedValuesList);
            if (exists first = selection.first) {
                Transferable payload;
                value rest = selection.rest;
                if (rest.empty) {
                    payload = FixtureTransferable(first);
                } else {
                    payload = CurriedFixtureTransferable(*selection);
                }
                event.startDrag(null, payload);
            }
        }
        shared actual Boolean equals(Object that) {
            if (is SwingList<out Anything> that) {
                return model == that.model;
            } else {
                return false;
            }
        }
        shared actual Integer hash => listModel.hash;
        shared actual void selectedPointChanged(Point? old, Point newPoint) =>
                listModel.selectedPointChanged(old, newPoint);
        object fixtureMouseListener extends MouseAdapter() {
            void handleMouseEvent(MouseEvent event) {
                if (event.popupTrigger, event.clickCount == 1) {
                    Integer index = locationToIndex(event.point);
                    if ((0:listModel.size).contains(index)) {
                        fixtureEditMenu(listModel.getElementAt(index), players, idf)
                            .show(event.component, event.x, event.y);
                    }
                }
            }
            shared actual void mouseClicked(MouseEvent event) => handleMouseEvent(event);
            shared actual void mousePressed(MouseEvent event) => handleMouseEvent(event);
            shared actual void mouseReleased(MouseEvent event) => handleMouseEvent(event);
        }
        addMouseListener(fixtureMouseListener);
    }
    DragSource.defaultDragSource.createDefaultDragGestureRecognizer(retval,
        DnDConstants.actionCopy, retval);
    object dropListener extends DropTargetAdapter() {
        todo("Figure out how to skip all this (return true) on non-local drags")
        Boolean isXfrFromOutside(DropTargetEvent dtde) {
            if (is Component source = dtde.source,
	                parentComponent.isAncestorOf(source)) {
                return false;
            } else {
                return true;
            }
        }
        void handleDrag(DropTargetDragEvent dtde) {
            if (dtde.dropAction.and(DnDConstants.actionCopy) != 0,
                (dtde.currentDataFlavorsAsList.contains(FixtureTransferable.flavor) ||
                dtde.currentDataFlavorsAsList.contains(
                    CurriedFixtureTransferable.flavor)), isXfrFromOutside(dtde)) {
                dtde.acceptDrag(dtde.dropAction);
            } else {
                dtde.rejectDrag();
            }
        }
        shared actual void dragEnter(DropTargetDragEvent dtde) => handleDrag(dtde);
        shared actual void dragOver(DropTargetDragEvent dtde) => handleDrag(dtde);
        shared actual void dropActionChanged(DropTargetDragEvent dtde) =>
                handleDrag(dtde);
        void handleDrop(Transferable trans) {
            ObjectArray<DataFlavor>? flavors = trans.transferDataFlavors;
            if (exists flavors) {
                for (flavor in flavors) {
                    if (flavor == FixtureTransferable.flavor) {
                        if (is TileFixture transferData = trans.getTransferData(flavor)) {
                            listModel.addFixture(transferData);
                        }
                    } else if (flavor == CurriedFixtureTransferable.flavor) {
                        assert (is Transferable[] curried =
                                trans.getTransferData(flavor));
                        curried.each(handleDrop);
                    } else {
                        throw UnsupportedFlavorException(
                            trans.transferDataFlavors.array.first);
                    }
                }
            } else {
                throw UnsupportedFlavorException(DataFlavor(`DataFlavor`, "null"));
            }
        }
        shared actual void drop(DropTargetDropEvent dtde) {
            if (isXfrFromOutside(dtde)) {
                for (flavor in dtde.currentDataFlavorsAsList) {
                    if ([FixtureTransferable.flavor,
	                        CurriedFixtureTransferable.flavor].contains(flavor)) {
                        try {
                            dtde.acceptDrop(dtde.dropAction);
                            if (exists trans = dtde.transferable) {
                                handleDrop(trans);
                            }
                            return;
                        } catch (UnsupportedFlavorException except) {
                            log.error("Unsupported flavor when it said it was supported",
                                except);
                        } catch (IOException except) {
                            log.error("I/O error getting the data", except);
                        }
                    }
                }
                dtde.rejectDrop();
            }
        }
    }
    retval.dropTarget = DropTarget(retval, dropListener);
    createHotKey(retval, "delete",
        (ActionEvent event) => listModel.removeAll(*retval.selectedValuesList),
        JComponent.whenAncestorOfFocusedComponent,
        KeyStroke.getKeyStroke(KeyEvent.vkDelete, 0),
        KeyStroke.getKeyStroke(KeyEvent.vkBackSpace, 0));
    return retval;
}
