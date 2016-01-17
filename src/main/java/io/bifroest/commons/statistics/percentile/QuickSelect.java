package io.bifroest.commons.statistics.percentile;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuickSelect {
    private static Logger log = LogManager.getLogger();

    public static double select( double[] values, int n ) {
        log.trace( "values.length: {}, n: {}", values.length, n );
        return destructiveSelect( values.clone(), 0, values.length - 1, n );
    }

    public static double select( double[] values, int n, int onlyLookAtFirst ) {
        log.trace( "values.length: {}, n: {}, onlyLookAtFirst: {}", values.length, n, onlyLookAtFirst );
        return destructiveSelect( Arrays.copyOf( values, onlyLookAtFirst ), 0, onlyLookAtFirst - 1, n );
    }

    public static double destructiveSelect( double[] values, int n ) {
        log.trace( "values.length: {}, n: {}", values.length, n );
        return destructiveSelect( values, 0, values.length - 1, n );
    }

    private static double destructiveSelect( double[] values, int left, int right, int n ) {
        if ( ! ( n < values.length ) ) {
            throw new IndexOutOfBoundsException( "n is " + n + ", should be < " + values.length );
        }

        log.trace( "values.length: {}, left: {}, right: {}, n: {}", values.length, left, right, n );

        if ( left == right ) {
            return values[left];
        }

        int pivotIndex = findPivot( values, left, right );
        pivotIndex = partition( values, left, right, pivotIndex );

        log.trace( "pivotIndex: {}, pivotElement: {}", pivotIndex, values[pivotIndex]);

        if ( n == pivotIndex ) {
            log.trace( "Found!" );
            return values[n];
        } else if ( n < pivotIndex ) {
            log.trace( "Recursing left!" );
            return destructiveSelect( values, left, pivotIndex - 1, n );
        } else {
            log.trace( "Recursing right!" );
            return destructiveSelect( values, pivotIndex + 1, right, n );
        }
    }

    private static int findPivot( double[] values, int left, int right ) {
        // Median of three
        int middle = ( left + right ) / 2;

        log.trace( "values.length: {}, left: {}, middle: {}, right: {}", values.length, left, middle, right );
        log.trace( "values[left]: {}, values[middle]: {}, values[right]: {}", values[left], values[middle], values[right]);

        if ( values[left] > values[middle] ) {
            if ( values[left] < values[right] ) {
                log.trace("returning left");
                return left;
            } else { // value[left] is highest
                if ( values[middle] > values[right] ) {
                    log.trace("returning middle");
                    return middle;
                } else {
                    log.trace("returning right");
                    return right;
                }
            }
        } else { //value[middle] > value[left]
            if ( values[middle] < values[right] ) {
                log.trace("returning middle");
                return middle;
            } else { //value[middle] is highest
                if ( values[left] > values[right] ) {
                    log.trace("returning left");
                    return left;
                } else {
                    log.trace("returning right");
                    return right;
                }
            }
        }
    }

    private static int partition( double[] values, int left, int right, int pivotIndex ) {
        // Swap pivot to the right, keep pivot value in pivotValue
        double pivotValue = values[pivotIndex];
        values[pivotIndex] = values[right];
        values[right] = pivotValue;

        int storeIndex = left;
        for ( int i = left; i < right; i++) {
            // both left and right are inclusive here, so one might think this should be i <= right.
            // BUT: the pivot element is stored in values[right], and we don't want this to be included in the loop.

            if ( values[i] < pivotValue ) {
                double temp = values[storeIndex];
                values[storeIndex] = values[i];
                values[i] = temp;

                storeIndex++;
            }
        }

        // Move pivot to it's final place
        double temp = values[storeIndex];
        values[storeIndex] = values[right];
        values[right] = temp;

        return storeIndex;
    }
}
