import java.awt.datatransfer {
    Transferable,
    DataFlavor,
    UnsupportedFlavorException
}
import java.lang {
    ObjectArray
}

"A [[Transferable]] implementation transferring a single [[Integer]]."
shared class IntTransferable(
        "The [[flavor|DataFlavor]] we should advertise support for."
        DataFlavor flavor, 
        "The [[Integer]] to transfer."
        Integer payload) satisfies Transferable {
    "A single-element array containing [[the provided flavor|flavor]]."
    shared actual ObjectArray<DataFlavor> transferDataFlavors =>
            ObjectArray.with(Singleton(flavor));
    "Only [[the provided flavor|flavor]] is supported."
    shared actual Boolean isDataFlavorSupported(DataFlavor possibility) =>
            possibility == flavor;
    "If [[wantedFlavor]] is equal to [[flavor]], return [[payload]];
     otherwise throws an [[UnsupportedFlavorException]]."
    shared actual Object getTransferData(DataFlavor wantedFlavor) {
        if (wantedFlavor == flavor) {
            return payload;
        } else {
            throw UnsupportedFlavorException(wantedFlavor);
        }
    }
    "A simple diagnostic string."
    shared actual String string = "IntTransferable carrying ``payload``";
    "Use the [[payload]] as the hash value."
    shared actual Integer hash => payload;
    "An object is equal iff it is an IntTransferable with the same [[flavor]]
     and [[payload]]."
    shared actual Boolean equals(Object that) {
        if (is IntTransferable that, that.payload == payload, that.flavor == flavor) {
            return true;
        } else {
            return false;
        }
    }
}
