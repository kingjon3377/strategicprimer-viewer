import java.awt.datatransfer {
    Transferable,
    DataFlavor,
    UnsupportedFlavorException
}
import java.lang {
    ObjectArray
}
"A [[Transferable]] implementation transferring a single Integer."
shared class IntTransferable(DataFlavor flavor, Integer payload) satisfies Transferable {
    shared actual ObjectArray<DataFlavor> transferDataFlavors =>
            ObjectArray.with(Singleton(flavor));
    shared actual Boolean isDataFlavorSupported(DataFlavor possibility) =>
            possibility == flavor;
    shared actual Object getTransferData(DataFlavor wantedFlavor) {
        if (wantedFlavor == flavor) {
            return payload;
        } else {
            throw UnsupportedFlavorException(wantedFlavor);
        }
    }
    shared actual String string = "IntTransferable carrying ``payload``";
    shared actual Integer hash => payload;
    shared actual Boolean equals(Object that) {
        if (is IntTransferable that, that.payload == payload, that.flavor == flavor) {
            return true;
        } else {
            return false;
        }
    }
}
