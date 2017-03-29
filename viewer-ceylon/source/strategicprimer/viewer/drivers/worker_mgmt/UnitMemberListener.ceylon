import lovelace.util.common {
    todo
}
import java.util {
    EventListener
}
import model.map.fixtures {
    UnitMember
}
"An interface for objects that want to know when a new unit member (usually a worker) is
 selected."
todo("combine with similar interfaces?")
shared interface UnitMemberListener satisfies EventListener {
	"Handle a change in the selected member."
	shared formal void memberSelected(UnitMember? previousSelection,
			UnitMember? selected);
}