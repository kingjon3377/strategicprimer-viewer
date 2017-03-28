import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.interop.java {
    javaClass
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassModel
}

import java.lang {
    JString=String,
    JBoolean=Boolean,
    JClass=Class,
    IllegalArgumentException
}

import javax.swing.table {
    AbstractTableModel
}

import lovelace.util.common {
    Reorderable,
    Comparator
}

import model.map {
    TileFixture
}
import model.map.fixtures {
    RiverFixture,
    Ground,
    TextFixture
}
import model.map.fixtures.explorable {
    Portal,
    Cave,
    Battlefield,
    AdventureFixture
}
import model.map.fixtures.mobile {
    Dragon,
    Centaur,
    Animal
}

import strategicprimer.viewer.model.map.fixtures.mobile {
    Fairy,
    Giant,
    SimpleImmortal,
    Unit,
    SimpleImmortalKind
}
import strategicprimer.viewer.model.map.fixtures.resources {
    Grove,
    Meadow,
    MineralVein,
    CacheFixture,
    Mine,
    StoneDeposit,
    Shrub
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Sandbar,
    Oasis,
    Hill,
    Forest
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Village,
    AbstractTown,
    Fortress
}
"A class to allow the Z-order of fixtures to be represented as a table."
shared AbstractTableModel&Reorderable&ZOrderFilter&Iterable<FixtureMatcher>&Comparator<TileFixture> fixtureFilterTableModel() {
    FixtureMatcher trivialMatcher(ClassModel<TileFixture> type,
            String description = "``type.declaration.name``s") {
        return FixtureMatcher((TileFixture fixture) => type.typeOf(fixture), description);
    }
    FixtureMatcher immortalMatcher(SimpleImmortalKind kind) {
        return FixtureMatcher((TileFixture fixture) {
            if (is SimpleImmortal fixture) {
                return fixture.immortalKind == kind;
            } else {
                return false;
            }
        }, kind.plural);
    }
    {FixtureMatcher*} complements<out T>(Boolean(T) method,
            String firstDescription, String secondDescription)
            given T satisfies TileFixture {
        return {simpleMatcher<T>(method, firstDescription),
            simpleMatcher<T>((T fixture) => !method(fixture),
                secondDescription)};
    }
    MutableList<FixtureMatcher> list = ArrayList<FixtureMatcher>();
    // Can't use our preferred initialization form because an Iterable can only be spread
    // as the *last* argument.
    for (arg in {
    // TODO: Maybe units should be broken up by owner?
        trivialMatcher(`Unit`), trivialMatcher(`Fortress`, "Fortresses"),
        // TODO: Towns should be broken up by kind or size, and maybe by status or owner
        trivialMatcher(`AbstractTown`, "Cities, Towns, and Fortifications"),
        // TODO: Village through Centaur were all 45, so their ordering happened by chance
        trivialMatcher(`Village`),
        immortalMatcher(SimpleImmortalKind.troll),
        immortalMatcher(SimpleImmortalKind.simurgh),
        immortalMatcher(SimpleImmortalKind.ogre),
        immortalMatcher(SimpleImmortalKind.minotaur),
        trivialMatcher(`Mine`),
        immortalMatcher(SimpleImmortalKind.griffin),
        immortalMatcher(SimpleImmortalKind.sphinx),
        immortalMatcher(SimpleImmortalKind.phoenix),
        immortalMatcher(SimpleImmortalKind.djinn),
        trivialMatcher(`Centaur`),
        // TODO: StoneDeposit through Animal were all 40; they too should be reviewed
        trivialMatcher(`StoneDeposit`, "Stone Deposits"),
        trivialMatcher(`MineralVein`, "Mineral Veins"),
        trivialMatcher(`Fairy`, "Fairies"), trivialMatcher(`Giant`),
        trivialMatcher(`Dragon`), trivialMatcher(`Cave`), trivialMatcher(`Battlefield`),
        complements<Animal>((Animal animal) => !animal.traces, "Animals", "Animal tracks"),
        complements<Grove>(Grove.orchard, "Orchards", "Groves"),
        // TODO: Rivers are usually handled specially, so should this really be included?
        trivialMatcher(`RiverFixture`, "Rivers"),
        // TODO: TextFixture thru AdventureFixture were all 25, and should be considered
        trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
        trivialMatcher(`Portal`), trivialMatcher(`Oasis`, "Oases"),
        trivialMatcher(`AdventureFixture`, "Adventures"),
        trivialMatcher(`CacheFixture`, "Caches"), trivialMatcher(`Forest`),
        // TODO: Shrub and Meadow were both 15; consider
        trivialMatcher(`Shrub`), complements<Meadow>(Meadow.field, "Fields", "Meadows"),
        // TODO: Sandbar and Hill were both 5; consider
        trivialMatcher(`Sandbar`), trivialMatcher(`Hill`),
        complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
    }) {
        if (is Iterable<FixtureMatcher> arg) {
            list.addAll(arg);
        } else {
            list.add(arg);
        }
    }
    object retval extends AbstractTableModel() satisfies Reorderable&ZOrderFilter&
            Iterable<FixtureMatcher>&Comparator<TileFixture> {
        shared actual Integer rowCount => list.size;
        shared actual Integer columnCount => 2;
        shared actual Object getValueAt(Integer rowIndex, Integer columnIndex) {
            if (exists matcher = list[rowIndex]) {
                switch (columnIndex)
                case (0) { return JBoolean(matcher.displayed); }
                case (1) { return matcher.description; }
                else { throw IllegalArgumentException("Only two columns"); }
            } else {
                throw IllegalArgumentException("Row out of bounds");
            }
        }
        shared actual String getColumnName(Integer column) {
            switch (column)
            case (0) { return "Visible"; }
            case (1) { return "Category"; }
            else { return super.getColumnName(column); }
        }
        shared actual JClass<out Object> getColumnClass(Integer columnIndex) {
            switch (columnIndex)
            case (0) { return javaClass<JBoolean>(); }
            case (1) { return javaClass<JString>(); }
            else { return javaClass<Object>(); }
        }
        shared actual Boolean isCellEditable(Integer rowIndex, Integer columnIndex) =>
                columnIndex == 0;
        shared actual void setValueAt(Object val, Integer rowIndex, Integer columnIndex) {
            if (columnIndex == 0, exists matcher = list[rowIndex]) {
                if (is Boolean val) {
                    matcher.displayed = val;
                    fireTableCellUpdated(rowIndex, 0);
                } else if (is JBoolean val) {
                    matcher.displayed = val.booleanValue();
                    fireTableCellUpdated(rowIndex, 0);
                }
            }
        }
        shared actual void reorder(Integer fromIndex, Integer toIndex) {
            if (fromIndex != toIndex) {
                list.move(fromIndex, toIndex);
                fireTableRowsDeleted(fromIndex, fromIndex);
                fireTableRowsInserted(toIndex, toIndex);
            }
        }
        shared actual Boolean shouldDisplay(TileFixture fixture) {
            for (matcher in list) {
                if (matcher.matches(fixture)) {
                    return matcher.displayed;
                }
            }
            ClassModel<TileFixture> cls = type(fixture);
            list.add(trivialMatcher(cls, fixture.plural()));
            Integer size = list.size;
            fireTableRowsInserted(size - 1, size - 1);
            return true;
        }
        shared actual Iterator<FixtureMatcher> iterator() => list.iterator();
        shared actual Comparison compare(TileFixture first, TileFixture second) {
            for (matcher in list) {
                if (!matcher.displayed) {
                    continue;
                }
                if (matcher.matches(first)) {
                    if (matcher.matches(second)) {
                        return equal;
                    } else {
                        return smaller;
                    }
                } else if (matcher.matches(second)) {
                    return larger;
                }
            }
            return equal;
        }
        shared actual Boolean equals(Object that) => (this of Identifiable).equals(that);
    }
    return retval;
}
