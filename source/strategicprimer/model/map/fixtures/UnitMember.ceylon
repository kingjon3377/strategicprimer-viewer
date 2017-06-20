import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    Subsettable,
    IFixture
}
"A (marker) interface for things that can be part of a unit.

 We extend [[strategicprimer.model.map::Subsettable]] to make Unit's subset calculation
 show differences in workers, but without hard-coding
 [[strategicprimer.model.map.fixtures.mobile::Worker]] in the Unit implementation. Most
 implementations of this will essentially delegate [[isSubset]] to [[equals]]."
todo("Change that now we have reified generics?", "Members?")
shared interface UnitMember satisfies IFixture&Subsettable<IFixture> {
	"Specialization."
	shared actual formal UnitMember copy(Boolean zero);
}
