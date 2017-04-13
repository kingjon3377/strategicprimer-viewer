import ceylon.test {
    test,
    assertEquals
}

test
void testModifiers() {
    value stats = (0..20).by(2);
    value modifiers = -5..4;
    for ([stat, modifier] in zipPairs(stats, modifiers)) {
        assertEquals(WorkerStats.getModifier(stat), modifier,
            "Even stat has correct modifier");
        assertEquals(WorkerStats.getModifier(stat + 1), modifier,
            "Odd stat has correct modifier");
    }
}