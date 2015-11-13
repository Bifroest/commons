package com.goodgame.profiling.commons.statistics.percentile;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

public class QuickSelectTest {
    private double[] values = new double[]{ 45, 77, 78, 75, 63, 83, 15, 71, 90 };

    @Test
    public void testOneElement() {
        assertEquals( 17, QuickSelect.select( new double[]{ 17 }, 0 ), 0);
    }

    @Test
    public void testFirst() {
        assertEquals( 15, QuickSelect.select( values, 0 ), 0 );
    }

    @Test
    public void testSecond() {
        assertEquals( 45, QuickSelect.select( values, 1 ), 0 );
    }

    @Test
    public void testMiddle() {
        assertEquals( 75, QuickSelect.select( values, 4 ), 0 );
    }

    @Test
    public void testLastButOne() {
        assertEquals( 83, QuickSelect.select( values, 7 ), 0 );
    }

    @Test
    public void testLast() {
        assertEquals( 90, QuickSelect.select( values, 8 ), 0 );
    }

    @Ignore
    @Test
    public void testRandom() {
        Random r = new Random();

        for ( int i = 0; i < 10000; i++ ) {
            double[] values = new double[1000];
            for ( int j = 0; j < 1000; j++ ) {
                values[j] = r.nextDouble();
                QuickSelect.select( values, r.nextInt( j + 1 ), j + 1 );
                System.out.println( "============================================================" );
            }
        }
    }
}
