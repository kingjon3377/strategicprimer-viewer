import strategicprimer.model.map {
    IFixture,
	HasPopulation
}
"An orchard (fruit trees) or grove (other trees) on the map."
shared class Grove(orchard, cultivated, kind, id, population = -1)
        satisfies HarvestableFixture&HasPopulation/*<Grove>*/ {
    "If true, this is a fruit orchard; if false, a non-fruit grove."
    shared Boolean orchard;
    "If true, this is a cultivated grove or orchard; if false, wild or abandoned."
    shared Boolean cultivated;
    "What kind of tree is in this orchard or grove."
    shared actual String kind;
    "An ID number to identify this orchard or grove."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "How many individual trees are in this grove or orchard."
    shared actual Integer population;
    shared actual Grove copy(Boolean zero) {
        Grove retval = Grove(orchard, cultivated, kind, id, (zero) then -1 else population);
        retval.image = image;
        return retval;
    }
    shared actual Grove reduced(Integer newPopulation) => Grove(orchard, cultivated, kind, id, newPopulation);
    shared actual String defaultImage = (orchard) then "orchard.png" else "tree.png";
    shared actual String shortDescription =>
            "``(cultivated) then "Cultivated" else "Wild"`` ``kind`` ``(orchard) then
                "orchard" else "grove"````(population < 0) then "" else " of ``population`` trees"``";
    shared actual String string = shortDescription;
    shared actual Boolean equals(Object obj) {
        if (is Grove obj) {
            return kind == obj.kind && orchard == obj.orchard &&
                cultivated == obj.cultivated && id == obj.id && population == obj.population;
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Grove fixture) {
            return kind == fixture.kind && orchard == fixture.orchard &&
                cultivated == fixture.cultivated && population == fixture.population;
        } else {
            return false;
        }
    }
    shared actual Boolean isSubset(IFixture other, Anything(String) report) {
		if (other.id != id) {
			report("Different IDs");
			return false;
		} else if (is Grove other) {
			variable Boolean retval = true;
			Anything(String) localReport;
			if (orchard) {
				localReport = (String str) => report("In orchard with ID #``id``:\t``str``");
			} else {
				localReport = (String str) => report("In grove with ID #``id``:\t``str``");
			}
			if (kind != other.kind) {
				localReport("Kinds differ");
				retval = false;
			}
			if (orchard != other.orchard) {
				localReport("Grove vs. orchard differs");
				retval = false;
			}
			if (cultivated != other.cultivated) {
				localReport("Cultivation status differs");
				retval = false;
			}
			if (population < other.population) {
				localReport("Has larger number of trees than we do");
				retval = false;
			}
			return retval;
		} else {
			report("Different types for ID #``id``");
			return false;
		}
    }
    shared actual String plural = "Groves and orchards";
    shared actual Integer dc = 18;
}
