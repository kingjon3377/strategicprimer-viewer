"""A simplified model of terrain, dividing tiles into "ocean", "forested", and
   "unforested"."""
class SimpleTerrain of unforested | forested | ocean {
    "Plains, desert, and mountains"
    shared new unforested { }
    "Temperate forest, boreal forest, and steppe"
    shared new forested { }
    "Ocean."
    shared new ocean { }
}
