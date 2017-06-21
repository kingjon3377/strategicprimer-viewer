import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    HasMutableImage,
    IFixture,
	HasKind
}
import strategicprimer.model.map.fixtures {
    UnitMember,
    FortressMember,
    Quantity
}
"A quantity of some kind of resource."
todo("More members?")
shared class ResourcePile(id, kind, contents, quantity)
		satisfies UnitMember&FortressMember&HasKind&HasMutableImage {
	"The ID # of the resource pile."
	shared actual Integer id;
	"What general kind of thing is in the resource pile."
	shared actual String kind;
	"What specific kind of thing is in the resource pile."
	shared variable String contents;
	"How much of that thing is in the pile, including units."
	shared variable Quantity quantity;
	"The filename of an image to use as an icon for this instance."
	shared actual variable String image = "";
	variable Integer createdTurn = -1;
	"The turn on which the resource was created."
	shared Integer created => createdTurn;
	assign created {
		if (created < 0) {
			createdTurn = -1;
		} else {
			createdTurn = created;
		}
	}
	shared actual String defaultImage = "resource.png";
	"If we ignore ID, a fixture is equal iff it is a ResourcePile with the same kind and
	 contents, of the same age, with equal quantity."
	shared actual Boolean equalsIgnoringID(IFixture fixture) {
		if (is ResourcePile fixture) {
			return fixture.kind == kind && fixture.contents == contents &&
				fixture.quantity == quantity && fixture.created == created;
		} else {
			return false;
		}
	}
	"A fixture is a subset iff it is a ResourcePile of the same kind, contents, and age,
	 with the same ID, and its quantity is a subset of ours."
	shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
		if (obj.id == id) {
			if (is ResourcePile obj) {
				variable Boolean retval = true;
				void localReport(String str) =>
						report("In Resource Pile, ID #``id``: ``str``");
				if (kind != obj.kind) {
					localReport("Kinds differ");
					retval = false;
				}
				if (!contents != obj.contents) {
					localReport("Contents differ");
					retval = false;
				}
				if (!quantity.isSubset(obj.quantity, localReport)) {
					retval = false;
				}
				if (created != obj.created, obj.created != -1) {
					localReport("Age differs");
					retval = false;
				}
				return retval;
			} else {
				report("Different fixture types given for ID #``id``");
				return false;
			}
		} else {
			report("IDs differ");
			return false;
		}
	}
	"Clone the object."
	shared actual ResourcePile copy(Boolean zero) {
		ResourcePile retval = ResourcePile(id, kind, contents, quantity);
		if (!zero) {
			retval.created = created;
		}
		return retval;
	}
	shared actual Integer hash => id;
	shared actual Boolean equals(Object obj) {
		if (is ResourcePile obj) {
			return id == obj.id && equalsIgnoringID(obj);
		} else {
			return false;
		}
	}
	shared actual String string {
		if (quantity.units.empty) {
			return "A pile of ``quantity`` ``contents`` (``kind``)``(created < 0) then
				"" else " from turn ``created``"``";
		} else {
			return "A pile of ``quantity`` of ``contents`` (``kind``)``(created < 0) then
				"" else " from turn ``created``"``";
		}
	}
}