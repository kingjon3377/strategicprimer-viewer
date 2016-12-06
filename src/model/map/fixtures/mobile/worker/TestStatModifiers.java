package model.map.fixtures.mobile.worker;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * A class to test that {@link WorkerStats#getModifier(int)} works properly.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class TestStatModifiers {
	/**
	 * Test case.
	 */
	@Test
	public void testModifiers() {
		for (int stat = 0,
					 modifier = -5; stat < 20; stat += 2, modifier++) {
			assertThat("Even stats have correct modifier", modifier,
					equalTo(WorkerStats.getModifier(stat)));
			assertThat("Odd stats have correct modifier", modifier,
					equalTo(WorkerStats.getModifier(stat + 1)));
		}
	}
}
