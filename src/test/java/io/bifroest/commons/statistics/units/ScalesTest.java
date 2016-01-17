package io.bifroest.commons.statistics.units;

import io.bifroest.commons.statistics.units.TIME_UNIT;
import io.bifroest.commons.statistics.units.SI_PREFIX;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScalesTest {

	@Test
	public void testYotta() {
		assertTrue( Math.pow( 10, 24 ) == SI_PREFIX.YOTTA.getMultiplier() );
	}

	@Test
	public void testOne() {
		assertTrue( 1d == SI_PREFIX.ONE.getMultiplier() );
	}

	@Test
	public void testYocto() {
		assertTrue( Math.pow( 10, -24 ) == SI_PREFIX.YOCTO.getMultiplier() );
	}

	@Test
	public void testOnes() {
		assertTrue( 1d == TIME_UNIT.SECOND.getMultiplier() );
	}

	@Test
	public void testDay() {
		assertTrue( 1d * 60 * 60 * 24 == TIME_UNIT.DAY.getMultiplier() );
	}

}
