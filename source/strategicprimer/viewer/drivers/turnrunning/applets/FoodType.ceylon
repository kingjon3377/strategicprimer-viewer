import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import ceylon.decimal {
    Decimal
}

import lovelace.util.jvm {
    decimalize
}

import strategicprimer.model.common.map.fixtures {
    IResourcePile,
    Quantity
}

class FoodType of milk | meat | grain | slowFruit | quickFruit | other {
    shared static FoodType? askFoodType(ICLIHelper cli, String foodKind) {
        for (type in `FoodType`.caseValues) {
            switch (cli.inputBooleanInSeries("Is it ``type``?", foodKind + type.string))
            case (true) { return type; }
            case (false) { continue; }
            case (null) { return null; }
        }
        return null;
    }

    shared Integer? keepsFor;
    shared Integer? keepsForIfCool;
    shared Integer? keepsForRefrigerated;
    shared Integer? keepsForFrozen;
    shared Decimal? fractionSpoilingDaily;
    shared Decimal? minimumSpoilage;
    shared actual String string;

    shared new milk {
        keepsFor = 2;
        keepsForIfCool = 3;
        keepsForRefrigerated = 4;
        keepsForFrozen = null;
        fractionSpoilingDaily = decimalize(0.5);
        minimumSpoilage = decimalize(8);
        string = "milk";
    }
    shared new meat {
        keepsFor = 2;
        keepsForIfCool = 3;
        keepsForRefrigerated = 4;
        keepsForFrozen = 8;
        fractionSpoilingDaily = decimalize(0.25);
        minimumSpoilage = decimalize(2);
        string = "meat";
    }
    shared new grain {
        keepsFor = 7;
        keepsForIfCool = 10;
        keepsForRefrigerated = null;
        keepsForFrozen = null;
        fractionSpoilingDaily = decimalize(0.125);
        minimumSpoilage = decimalize(1);
        string = "grain";
    }
    shared new slowFruit {
        keepsFor = 6;
        keepsForIfCool = 8;
        keepsForRefrigerated = 12;
        keepsForFrozen = null;
        fractionSpoilingDaily = decimalize(0.125);
        minimumSpoilage = decimalize(1);
        string = "slow-spoiling fruit";
    }
    shared new quickFruit {
        keepsFor = 3;
        keepsForIfCool = 6;
        keepsForRefrigerated = 8;
        keepsForFrozen = null;
        fractionSpoilingDaily = decimalize(0.25);
        minimumSpoilage = decimalize(1);
        string = "fast-spoiling fruit";
    }
    // TODO: Add additional cases
    shared new other {
        keepsFor = null;
        keepsForIfCool = null;
        keepsForRefrigerated = null;
        keepsForFrozen = null;
        fractionSpoilingDaily = null;
        minimumSpoilage = null;
        string = "other";
    }

    shared Boolean? hasSpoiled(IResourcePile pile, Integer turn, ICLIHelper cli) {
        Integer age = turn - pile.created;
        if (turn < 0 || pile.created < 0) { // Either corrupt turn information or non-spoiling rations
            return false;
        } else if (pile.created >= turn) { // Created this turn or in the future
            return false;
        } else if (exists keepsFor, age < keepsFor) {
            return false;
        } else if (exists keepsForIfCool, age < keepsForIfCool) {
            switch (cli.inputBooleanInSeries("Was this kept cool?", pile.kind + string + "cool"))
            case (true) { return false; }
            case (false) {}
            case (null) { return null; }
        }
        if (exists keepsForRefrigerated, age < keepsForRefrigerated) {
            switch (cli.inputBooleanInSeries("Was this kept refrigerated?", pile.kind + string + "refrig"))
            case (true) { return false; }
            case (false) {}
            case (null) { return null; }
        }
        if (exists keepsForFrozen, age < keepsForFrozen) {
            switch (cli.inputBooleanInSeries("Was this kept frozen?", pile.kind + string + "frozen"))
            case (true) { return false; }
            case (false) {}
            case (null) { return null; }
        }
        if ([keepsFor, keepsForIfCool, keepsForRefrigerated, keepsForFrozen].coalesced.empty) {
            return cli.inputBooleanInSeries("Has this spoiled?", pile.kind + string + "other");
        } else {
            return true;
        }
    }

    shared Decimal? amountSpoiling(Quantity qty, ICLIHelper cli) {
        Decimal amt = decimalize(qty.number);
        Decimal? fractional;
        if (exists fractionSpoilingDaily) {
            fractional = fractionSpoilingDaily * amt;
        } else {
            fractional = null;
        }
        if (exists retval = max([fractional, minimumSpoilage].coalesced)) {
            return retval;
        } else {
            return cli.inputDecimal("How many pounds spoil?");
        }
    }
}
