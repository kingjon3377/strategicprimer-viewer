import java.awt.datatransfer {
    DataFlavor,
    UnsupportedFlavorException,
    Transferable
}
import java.lang {
    ObjectArray
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    TileFixture
}
"A class to transfer a TileFixture by drag-and-drop."
class FixtureTransferable satisfies Transferable {
    shared static DataFlavor flavor = DataFlavor(`TileFixture`, "TileFixture");
    TileFixture payload;
    shared new (TileFixture data) { payload = data; }
    shared actual ObjectArray<DataFlavor> transferDataFlavors =>
            ObjectArray.with({flavor});
    shared actual Boolean isDataFlavorSupported(DataFlavor candidate) =>
            flavor == candidate;
    shared actual TileFixture getTransferData(DataFlavor candidate) {
        if (flavor == candidate) {
            return payload;
        } else {
            throw UnsupportedFlavorException(candidate);
        }
    }
    shared actual String string =>
            "FixtureTransferable transferring ``payload.shortDescription``";
    shared actual Boolean equals(Object that) {
        if (is FixtureTransferable that) {
            return payload == that.payload;
        } else {
            return false;
        }
    }
    shared actual Integer hash => payload.hash;
}
"A class to transfer a list of TileFixtures by drag-and-drop."
todo("Generalize, rename to CurriedTransferable, move to lovelace.util.jvm")
class CurriedFixtureTransferable satisfies Transferable {
    shared static DataFlavor flavor =
            DataFlavor(`CurriedFixtureTransferable`, "CurriedTransferable");
    Transferable[] payload;
    shared new (TileFixture* list) {
        payload = list.map(`FixtureTransferable`).sequence();
    }
    shared actual ObjectArray<DataFlavor> transferDataFlavors =>
            ObjectArray.with({flavor});
    shared actual Boolean isDataFlavorSupported(DataFlavor candidate) =>
            flavor == candidate;
    shared actual {Transferable*} getTransferData(DataFlavor candidate) {
        if (isDataFlavorSupported(candidate)) {
            return payload;
        } else {
            throw UnsupportedFlavorException(candidate);
        }
    }
    shared actual String string =>
            "CurriedFixtureTransferable with payload containing ``payload
                .size`` elements";
}
