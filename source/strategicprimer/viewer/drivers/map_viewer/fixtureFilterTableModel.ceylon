import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassModel,
	ClassOrInterface
}

import java.lang {
    JString=String,
    JBoolean=Boolean,
    JClass=Class,
    Types,
	ArrayIndexOutOfBoundsException
}

import javax.swing.table {
    AbstractTableModel
}

import lovelace.util.common {
    Reorderable,
    Comparator,
	inverse
}

import strategicprimer.model.map {
    TileFixture
}
import strategicprimer.model.map.fixtures {
    TextFixture,
    Ground
}
import strategicprimer.model.map.fixtures.explorable {
    Cave,
    Battlefield,
    Portal,
    AdventureFixture
}

import strategicprimer.model.map.fixtures.mobile {
    Animal,
    Dragon,
    Centaur,
    Fairy,
    Giant,
    IUnit,
    Troll,
    Simurgh,
    Ogre,
    Minotaur,
    Griffin,
    Sphinx,
    Phoenix,
    Djinn,
	AnimalTracks
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Meadow,
    MineralVein,
    CacheFixture,
    Mine,
    StoneDeposit,
    Shrub
}
import strategicprimer.model.map.fixtures.terrain {
    Sandbar,
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    Village,
    AbstractTown,
    Fortress
}
import strategicprimer.drivers.common {
	FixtureMatcher,
	simpleMatcher
}
"A class to allow the Z-order of fixtures to be represented as a table." // TODO: Try again to get it to compile as a class rather than a factory with inner object
shared AbstractTableModel&Reorderable&ZOrderFilter&{FixtureMatcher*}&
        Comparator<TileFixture> fixtureFilterTableModel() {
    FixtureMatcher trivialMatcher(ClassOrInterface<TileFixture> type,
            String description = "``type.declaration.name``s") {
        return FixtureMatcher((TileFixture fixture) => type.typeOf(fixture), description);
    }
    {FixtureMatcher*} complements<out T>(Boolean(T) method,
            String firstDescription, String secondDescription)
            given T satisfies TileFixture {
        return [simpleMatcher<T>(method, firstDescription),
            simpleMatcher<T>(inverse(method), secondDescription)];
    }
    MutableList<FixtureMatcher> list = ArrayList<FixtureMatcher>();
    // Can't use our preferred initialization form because an Iterable can only be spread
    // as the *last* argument.
    for (arg in [
	        complements<IUnit>((unit) => !unit.owner.independent, "Units",
	            "Independent Units"),
	        trivialMatcher(`Fortress`, "Fortresses"),
	        // TODO: Towns should be broken up by kind or size, and maybe by status or owner
	        trivialMatcher(`AbstractTown`, "Cities, Towns, and Fortifications"),
	        // TODO: break up by owner beyond owned/independent
	        complements<Village>((village) => village.owner.independent, "Independent Villages", "Villages With Suzerain"),
	        trivialMatcher(`Mine`), trivialMatcher(`Troll`),
	        trivialMatcher(`Simurgh`), trivialMatcher(`Ogre`), trivialMatcher(`Minotaur`),
	        trivialMatcher(`Griffin`), trivialMatcher(`Sphinx`, "Sphinxes"),
	        trivialMatcher(`Phoenix`, "Phoenixes"), trivialMatcher(`Djinn`, "Djinni"),
	        trivialMatcher(`Centaur`), trivialMatcher(`Fairy`, "Fairies"),
	        trivialMatcher(`Giant`), trivialMatcher(`Dragon`), trivialMatcher(`Cave`),
	        trivialMatcher(`Battlefield`),
	        trivialMatcher(`Animal`), trivialMatcher(`AnimalTracks`),
	        trivialMatcher(`StoneDeposit`, "Stone Deposits"),
	        trivialMatcher(`MineralVein`, "Mineral Veins"),
	        complements<Grove>(Grove.orchard, "Orchards", "Groves"),
	        trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"), trivialMatcher(`Portal`),
	        trivialMatcher(`AdventureFixture`, "Adventures"),
	        trivialMatcher(`CacheFixture`, "Caches"), trivialMatcher(`Oasis`, "Oases"),
	        trivialMatcher(`Forest`), complements<Meadow>(Meadow.field, "Fields", "Meadows"),
	        trivialMatcher(`Shrub`), trivialMatcher(`Hill`), trivialMatcher(`Sandbar`),
	        complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
    ]) {
        if (is {FixtureMatcher*} arg) {
            list.addAll(arg);
        } else {
            list.add(arg);
        }
    }
    object retval extends AbstractTableModel() satisfies Reorderable&ZOrderFilter&
            {FixtureMatcher*}&Comparator<TileFixture> {
        shared actual Integer rowCount => list.size;
        shared actual Integer columnCount => 2;
        shared actual Object getValueAt(Integer rowIndex, Integer columnIndex) {
            if (exists matcher = list[rowIndex]) {
                switch (columnIndex)
                case (0) { return JBoolean.valueOf(matcher.displayed); }
                case (1) { return matcher.description; }
                else { throw ArrayIndexOutOfBoundsException(columnIndex); }
            } else {
                throw ArrayIndexOutOfBoundsException(rowIndex);
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
            case (0) { return Types.classForType<JBoolean>(); }
            case (1) { return Types.classForType<JString>(); }
            else { return Types.classForType<Object>(); }
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
            list.add(trivialMatcher(cls, fixture.plural));
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
    }
    return retval;
}
