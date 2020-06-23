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
    Quantity,
    ResourcePile
}

class FoodType {
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
    new delegate(Integer? keepsFor, Integer? keepsForIfCool, Integer? keepsForRefrig,
            Integer? keepsForFrozen, Decimal? fracSpoilingDaily, Decimal? minSpoilage,
            String str) {
        this.keepsFor = keepsFor;
        this.keepsForIfCool = keepsForIfCool;
        this.keepsForRefrigerated = keepsForRefrig;
        this.keepsForFrozen = keepsForFrozen;
        this.fractionSpoilingDaily = fracSpoilingDaily;
        this.minimumSpoilage = minSpoilage;
        string = str;
    }
    shared new milk extends delegate(2, 3, 4, null, decimalize(0.5), decimalize(8), "milk") {}
    shared new meat extends delegate(2, 3, 4, 8, decimalize(0.25), decimalize(2), "meat") {}
    shared new grain extends delegate(7, 10, null, null, decimalize(0.125), decimalize(1), "grain") {}
    shared new slowFruit extends delegate(6, 8, 12, null, decimalize(0.125), decimalize(1), "slow-spoiling fruit") {}
    shared new quickFruit extends delegate(3, 6, 8, null, decimalize(0.25), decimalize(1), "fast-spoiling fruit") {}
    // TODO: Add additional cases
    shared new other extends delegate(null, null, null, null, null, null, "other") {}

    shared Boolean? hasSpoiled(ResourcePile pile, Integer turn, ICLIHelper cli) {
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
