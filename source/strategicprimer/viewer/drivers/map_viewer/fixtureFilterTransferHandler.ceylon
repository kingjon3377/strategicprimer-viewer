import java.awt {
    Component
}
import java.awt.datatransfer {
    DataFlavor,
    UnsupportedFlavorException,
    Transferable
}
import java.io {
    IOException
}
import java.lang {
    IllegalStateException
}

import javax.swing {
    JTable,
    SwingList=JList,
    TransferHandler,
    JComponent
}

import lovelace.util.common {
    Reorderable
}
import lovelace.util.jvm {
    IntTransferable
}
import strategicprimer.drivers.common {
	FixtureMatcher
}
"A transfer-handler to let the user drag items in the list to control Z-order."
object fixtureFilterTransferHandler extends TransferHandler() {
    DataFlavor flavor = DataFlavor(`FixtureMatcher`, "FixtureMatcher");
    "A drag/drop operation is supported iff it is a supported flavor and it is or
     can be coerced to be a move operation."
    shared actual Boolean canImport(TransferSupport support) {
        if (support.drop, support.isDataFlavorSupported(flavor),
            TransferHandler.move.and(support.sourceDropActions) ==
            TransferHandler.move) {
            support.dropAction = TransferHandler.move;
            return true;
        } else {
            return false;
        }
    }
    "Create a wrapper to transfer contents of the given component, which must be a
     [[SwingList]] or a [[JTable]]."
    shared actual Transferable createTransferable(JComponent component) {
        if (is SwingList<out Anything> component) {
            return IntTransferable(flavor, component.selectedIndex);
        } else if (is JTable component) {
            return IntTransferable(flavor, component.selectedRow);
        } else {
            throw IllegalStateException(
                "Tried to create transferable from non-list");
        }
    }
    "This listener only allows move operations."
    shared actual Integer getSourceActions(JComponent component) => TransferHandler.move;
    "Handle a drop."
    shared actual Boolean importData(TransferSupport support) {
        if (!support.drop) {
            return false;
        }
        Component component = support.component;
        DropLocation dropLocation = support.dropLocation;
        Transferable transfer = support.transferable;
        Integer payload;
        try {
            assert (is Integer temp = transfer.getTransferData(flavor));
            payload = temp;
        } catch (UnsupportedFlavorException|IOException except) {
            log.debug("Transfer failure", except);
            return false;
        }
        if (is SwingList<out Anything> component,
            is Reorderable model = component.model,
            is SwingList<out Anything>.DropLocation dropLocation) {
            Integer index = dropLocation.index;
            model.reorder(payload, index);
            return true;
        } else if (is JTable component, is Reorderable model = component.model,
            is JTable.DropLocation dropLocation) {
            Integer index = dropLocation.row;
            Integer selection = component.selectedRow;
            model.reorder(payload, index);
            if (selection == payload) {
                component.setRowSelectionInterval(index, index);
            } else if (selection > index, selection < payload) {
                component.setRowSelectionInterval(selection - 1, selection - 1);
            }
            return true;
        } else {
            return false;
        }
    }
}
