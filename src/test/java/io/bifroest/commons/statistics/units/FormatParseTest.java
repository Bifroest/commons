package io.bifroest.commons.statistics.units;

import io.bifroest.commons.statistics.units.TIME_UNIT;
import io.bifroest.commons.statistics.units.SI_PREFIX;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.bifroest.commons.statistics.units.format.TimeFormatter;
import io.bifroest.commons.statistics.units.format.UnitFormatter;
import io.bifroest.commons.statistics.units.parse.TimeUnitParser;
import io.bifroest.commons.statistics.units.parse.UnitParser;

public class FormatParseTest {

	private static final int NUM_TESTS = 100;

	private Random random;

	@Before
	public void initRandom() {
		random = new Random();
	}

	@Ignore @Test
	public void randomizedFormatAndParse() {
		for ( int i = 0; i < NUM_TESTS; i++ ) {

			SI_PREFIX prefix = SI_PREFIX.values()[random.nextInt( SI_PREFIX.values().length )];
			TIME_UNIT unit = TIME_UNIT.values()[random.nextInt( TIME_UNIT.values().length )];
			UnitFormatter formatter = new TimeFormatter( prefix, unit );
			UnitParser parser = new TimeUnitParser( prefix, unit );

			double value = random.nextDouble();
			String formatted = formatter.format( value );
			double result = parser.parse( formatted ).doubleValue();
			System.out.println();
			System.out.println( value + " -> " + formatted + " -> " + result );
			System.out.println( prefix + " " + unit);
			assertEquals( value, result, 0.1 );

		}
	}

}
