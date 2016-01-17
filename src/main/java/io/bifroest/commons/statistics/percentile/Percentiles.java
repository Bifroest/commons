package io.bifroest.commons.statistics.percentile;

import java.util.Arrays;


public class Percentiles {
    private final double[] values;

    private int next;
    private boolean completelyFilled;

    public Percentiles( int size ) {
        values = new double[size];

        next = 0;
        completelyFilled = false;
    }

    public void add( double value ) {
        values[next] = value;
        next = ( next + 1 ) % values.length;
        if ( next == 0 ) {
            completelyFilled = true;
        }
    }

    public double[] getPercentiles( double...percentile ) {
        double[] ret = new double[percentile.length];
        double[] myvalues = completelyFilled ? values.clone() : Arrays.copyOf( values, next );
        for ( int i = 0; i < ret.length; i++ ) {
            if ( ! completelyFilled && next == 0 ) {
                ret[i] = Double.NaN;
            } else if ( completelyFilled ) {
                ret[i] = QuickSelect.destructiveSelect( myvalues, (int)( (values.length-1) * percentile[i] ) );
            } else {
                ret[i] = QuickSelect.destructiveSelect( myvalues, (int)( (next-1) * percentile[i] ) );
            }
        }
        return ret;
    }
}
