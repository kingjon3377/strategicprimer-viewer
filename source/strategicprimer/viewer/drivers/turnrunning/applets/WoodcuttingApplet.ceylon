import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Quantity
}
import ceylon.decimal {
    Decimal
}
import lovelace.util.jvm {
    decimalize
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.common.map {
    Point,
    HasExtent,
    TileFixture
}
service(`interface TurnAppletFactory`)
shared class WoodcuttingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) =>
        WoodcuttingApplet(model, cli, idf);
}

class WoodcuttingApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared actual [String+] commands = ["woodcutting"];
    shared actual String description => "cut down trees for wood or to clear land";

    void reduceExtent(Point point, HasExtent<out HasExtent<out Anything>&TileFixture>&TileFixture fixture,
            Decimal acres) {
        if (acres.positive) {
            if (acres >= decimalize(fixture.acres)) {
                model.map.removeFixture(point, fixture);
                for (map->[file, _] in model.subordinateMaps) {
                    if (exists found = map.fixtures.get(point)
                            .find(shuffle(curry(fixture.isSubset))(noop))) {
                        map.removeFixture(point, found);
                    }
                }
            } else {
                model.map.removeFixture(point, fixture);
                value addend = fixture.reduced(acres);
                model.map.addFixture(point, addend);
                for (map->[file , _]in model.subordinateMaps) {
                    if (exists found = map.fixtures.get(point)
                            .find(shuffle(curry(fixture.isSubset))(noop))) {
                        map.removeFixture(point, found);
                    }
                    map.addFixture(point, addend.copy(true));
                }
            }
        } else {
            addToSubMaps(point, fixture, true);
        }
    }

    shared actual String? run() {
        StringBuilder builder = StringBuilder();
        // TODO: support other forms of woodcutting: logs, long beams, land-clearing, etc.
        value loc = confirmPoint("Where are they cutting wood?");
        if (is Null loc) {
            return null;
        }
        Integer workers;
        if (exists temp = cli.inputNumber("How many workers cutting?"), temp.positive) {
            workers = temp;
        } else {
            return null;
        }
        Integer baseHours;
        if (exists temp = cli.inputNumber("How many hours into a tree were they before?"), !temp.negative) {
            baseHours = temp;
        } else {
            return null;
        }
        Integer totalHours;
        if (exists temp = cli.inputNumber("How many hours does each worker work?"), temp.positive) {
            totalHours = temp * workers + baseHours;
        } else {
            return null;
        }
        variable Integer treeCount = totalHours / 100;
        cli.print("With unskilled workers, that would be ``treeCount`` trees");
        if (100.divides(totalHours)) {
            cli.println(".");
        } else {
            cli.println(" and ``totalHours % 100`` into the next.");
        }
        switch (cli.inputBoolean("Is that correct?"))
        case (true) {
            builder.append("The ``workers`` workers cut down and process ``treeCount`` trees");
            if (!100.divides(totalHours)) {
                cli.println(" and get ``totalHours % 100`` into the next");
            }
        }
        case (false) {
            if (exists str = cli.inputMultilineString("Description of trees cut:")) {
                builder.append(str);
            } else {
                return null;
            }
            if (exists count = cli.inputNumber("Number of trees cut and processed:"), count.positive) {
                treeCount = count;
            } else {
                return null;
            }
        }
        case (null) {
            return null;
        }
        variable Integer footage = treeCount * 300;
        switch (cli.inputBoolean("Is ``footage`` cubic feet correct?"))
        case (true) {
            builder.append(", producing ``footage`` cubic feet of wood");
        } case (false) {
            if (exists str = cli.inputMultilineString("Description of production:")) {
                builder.append(str);
            } else {
                return null;
            }
            if (exists count = cli.inputNumber("Square feet production-ready wood:")) {
                footage = count;
            } else {
                return null;
            }
        }
        case (null) {
            return null;
        }
        if (footage.positive) {
            assert (exists unit = model.selectedUnit);
            addResourceToMaps(ResourcePile(idf.createID(), "wood", "production-ready wood", Quantity(footage, "cubic feet")), unit.owner);
        }
        if (treeCount > 7, exists forest = chooseFromList(model.map.fixtures.get(loc).narrow<Forest>().sequence(),
            "Forests on tile:", "No forests on tile", "Forest being cleared:", false), forest.acres.positive) {
            variable Decimal acres = smallest(decimalize(treeCount * 10 / 72) / decimalize(100), decimalize(forest.acres));
            switch (cli.inputBoolean("Is ``acres`` (of ``forest.acres``) cleared correct?"))
            case (true) {
                builder.append(", clearing ``acres`` acres (~ ``(acres * decimalize(43560)).integer``) of land.`");
            }
            case (false) {
                if (exists str = cli.inputMultilineString("Descriptoin of cleared land:")) {
                    builder.append(str);
                } else {
                    return null;
                }
                if (exists temp = cli.inputDecimal("Acres cleared:")) {
                    acres = temp;
                } else {
                    return null;
                }
            }
            case (null) {
                return null;
            }
            reduceExtent(loc, forest, acres);
        }
        return builder.string;
    }
}
