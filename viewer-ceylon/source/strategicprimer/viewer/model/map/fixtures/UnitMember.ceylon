import lovelace.util.common {
    todo
}
import model.map {
    SubsettableFixture
}
"A (marker) interface for things that can be part of a unit.

 We extend [[Subsettable]] to make Unit's subset calculation show differences in workers,
 but without hard-coding [[Worker]] in the Unit implementation. Most implementations of
 this will essentially delegate [[isSubset]] to [[equals]]."
todo("Change that now we have reified generics?", "Members?")
shared interface UnitMember satisfies SubsettableFixture {
	"Specialization."
	shared actual formal UnitMember copy(Boolean zero);
}
