import model.map.fixtures {
    UnitMember
}
"A (marker) interface for centaurs, trolls, ogres, fairies, and the like."
shared interface Immortal satisfies MobileFixture&UnitMember {
	"Clone the object."
	shared actual formal Immortal copy(Boolean zero);
}