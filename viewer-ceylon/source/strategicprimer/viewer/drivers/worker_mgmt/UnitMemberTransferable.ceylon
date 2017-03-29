import java.lang {
    ObjectArray
}
import ceylon.interop.java {
    createJavaObjectArray
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
import java.awt.datatransfer {
    DataFlavor,
    UnsupportedFlavorException,
    Transferable
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}
"A class to transfer a UnitMember via drag-and-drop."
class UnitMemberTransferable satisfies Transferable {
    shared static DataFlavor flavor =
            DataFlavor(`Iterable<[UnitMember, IUnit]>`, "List<UnitMember>");
    [UnitMember, IUnit][] payload;
    shared new ([UnitMember, IUnit]* data) { payload = data.sequence(); }
    shared actual ObjectArray<DataFlavor> transferDataFlavors =>
            createJavaObjectArray({flavor});
    shared actual Boolean isDataFlavorSupported(DataFlavor candidate) =>
            flavor == candidate;
    shared actual {[UnitMember, IUnit]*} getTransferData(DataFlavor candidate) {
        if (flavor == candidate) {
            return payload;
        } else {
            throw UnsupportedFlavorException(candidate);
        }
    }
    shared actual String string =>
            "UnitMemberTransferable conveying ``payload.size`` member(s)";
    shared actual Boolean equals(Object that) {
        if (is UnitMemberTransferable that) {
            return payload == that.payload;
        } else {
            return false;
        }
    }
    shared actual Integer hash => payload.hash;
}
