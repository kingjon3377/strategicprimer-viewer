import java.lang {
	ObjectArray
}
import java.awt.datatransfer {
	DataFlavor,
	UnsupportedFlavorException,
	Transferable
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit
}
import strategicprimer.model.map {
	HasMutableKind
}
"A class to transfer a Unit (to change its kind) by drag-and-drop."
class UnitTransferable satisfies Transferable {
	shared static DataFlavor flavor = DataFlavor(`{IUnit*}`, "List<IUnit>");
	<IUnit&HasMutableKind>[] payload;
	shared new (<IUnit&HasMutableKind>* data) { payload = data.sequence(); }
	shared actual ObjectArray<DataFlavor> transferDataFlavors =>
			ObjectArray.with(Singleton(flavor));
	shared actual Boolean isDataFlavorSupported(DataFlavor candidate) =>
			flavor == candidate;
	shared actual {<IUnit&HasMutableKind>*} getTransferData(DataFlavor candidate) {
		if (flavor == candidate) {
			return payload;
		} else {
			throw UnsupportedFlavorException(candidate);
		}
	}
	shared actual String string => "UnitTransferable conveying ``payload.size`` unit(s)`";
	shared actual Boolean equals(Object that) {
		if (is UnitTransferable that) {
			return payload == that.payload;
		} else {
			return false;
		}
	}
	shared actual Integer hash => payload.hash;
}