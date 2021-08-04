import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    ResourcePileImpl,
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

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

service(`interface TurnAppletFactory`)
shared class WoodcuttingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        WoodcuttingApplet(model, cli, idf);
}

class WoodcuttingApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared actual [String+] commands = ["woodcutting"];
    shared actual String description => "cut down trees for wood or to clear land";

    void reduceExtent(Point point, HasExtent<out HasExtent<out Anything>&TileFixture>&TileFixture fixture,
            Decimal acres) => model.reduceExtent(point, fixture, true, acres);

    shared actual String? run() {
        StringBuilder builder = StringBuilder();
        // FIXME: support other forms of woodcutting: logs, long beams, land-clearing, etc.
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
            // FIXME: Use model.addResource() rather than creating pile here ourselves
            if (!model.addExistingResource(ResourcePileImpl(idf.createID(), "wood", "production-ready wood",
                    Quantity(footage, "cubic feet")), unit.owner)) {
                cli.println("Failed to find a fortress to add to in any map");
            }
        }
        if (treeCount > 7, exists forest = chooseFromList(model.map.fixtures.get(loc).narrow<Forest>().sequence(),
                "Forests on tile:", "No forests on tile", "Forest being cleared:", false), forest.acres.positive) {
            variable Decimal acres = smallest(decimalize(treeCount * 10 / 72) / decimalize(100), decimalize(forest.acres));
            switch (cli.inputBoolean("Is ``acres`` (of ``forest.acres``) cleared correct?"))
            case (true) {
                builder.append(", clearing ``acres`` acres (~ ``(acres * decimalize(43560)).integer`` sq ft) of land.`");
            }
            case (false) {
                if (exists str = cli.inputMultilineString("Description of cleared land:")) {
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
