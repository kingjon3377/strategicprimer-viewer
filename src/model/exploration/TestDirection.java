package model.exploration;

import java.io.File;
import model.exploration.IExplorationModel.Direction;
import model.map.MapDimensions;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import org.junit.Test;

import static model.map.PointFactory.point;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that the movement code gets its most basic functionality, finding the right
 * adjacent tile, right.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class TestDirection {
	/**
	 * The object under test. Because the method in question is required by the
	 * interface, it can't be static.
	 */
	private final IExplorationModel model =
			new ExplorationModel(new SPMapNG(new MapDimensions(5, 5, 2),
													new PlayerCollection(), 0),
										new File(""));
	@Test
	public void testEast() {
		assertThat("East of (0, 0) is (0, 1)",
				model.getDestination(point(0, 0), Direction.East),
				equalTo(point(0, 1)));
		assertThat("East of (1, 1) is (1, 2)",
				model.getDestination(point(1, 1), Direction.East),
				equalTo(point(1, 2)));
		assertThat("East of (3, 4) in a 5x5 map is (3, 0)",
				model.getDestination(point(3, 4), Direction.East),
				equalTo(point(3, 0)));
		assertThat("East of (4, 3) is (4, 4)",
				model.getDestination(point(4, 3), Direction.East),
				equalTo(point(4, 4)));
	}
	@Test
	public void testNorth() {
		assertThat("North of (0, 0) in a 5x5 map is (4, 0)",
				model.getDestination(point(0, 0), Direction.North),
				equalTo(point(4, 0)));
		assertThat("North of (1, 1) is (0, 1)",
				model.getDestination(point(1, 1), Direction.North),
				equalTo(point(0, 1)));
		assertThat("North of (3, 4) is (2, 4)",
				model.getDestination(point(3, 4), Direction.North),
				equalTo(point(2, 4)));
		assertThat("North of (4, 3) is (3, 3)",
				model.getDestination(point(4, 3), Direction.North),
				equalTo(point(3, 3)));
	}
	@Test
	public void testSouth() {
		assertThat("South of (0, 0) is (1, 0)",
				model.getDestination(point(0, 0), Direction.South),
				equalTo(point(1, 0)));
		assertThat("South of (1, 1) is (2, 1)",
				model.getDestination(point(1, 1), Direction.South),
				equalTo(point(2, 1)));
		assertThat("South of (3, 4) is (4, 4)",
				model.getDestination(point(3, 4), Direction.South),
				equalTo(point(4, 4)));
		assertThat("South of (4, 3) in a 5x5 map is (0, 3)",
				model.getDestination(point(4, 3), Direction.South),
				equalTo(point(0, 3)));
	}
	@Test
	public void testWest() {
		assertThat("West of (0, 0) in a 5x5 map is (0, 4)",
				model.getDestination(point(0, 0), Direction.West),
				equalTo(point(0, 4)));
		assertThat("West of (1, 1) is (1, 0)",
				model.getDestination(point(1, 1), Direction.West),
				equalTo(point(1, 0)));
		assertThat("West of (3, 4) is (3, 3)",
				model.getDestination(point(3, 4), Direction.West),
				equalTo(point(3, 3)));
		assertThat("West of (4, 3) is (4, 2)",
				model.getDestination(point(4, 3), Direction.West),
				equalTo(point(4, 2)));
	}
}
