import strategicprimer.model.map {
    IMapNG,
    IFixture
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import strategicprimer.model.xmlio {
    Warning
}
object dbMemoizer {
    MutableMap<[IMapNG, Integer], IFixture> cache =
            HashMap<[IMapNG, Integer], IFixture>();
    shared IFixture findById(IMapNG map, Integer id, MapContentsReader context,
            Warning warner) {
        if (exists retval = cache[[map, id]]) {
            return retval;
        } else {
            assert (exists retval =
                        context.findByIdImpl(map.fixtures.map(Entry.item), id));
            cache[[map, id]] = retval;
            return retval;
        }
    }
}