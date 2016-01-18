package io.bifroest.commons.model;

import java.util.Objects;

public final class Interval {

    private final long start;
    private final long end;

    public Interval( long from, long to ) {
        this.start = Math.min( from, to );
        this.end = Math.max( from, to );
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public boolean contains( long value ) {
        return ( value >= start ) && ( value < end );
    }

    public boolean intersects( Interval interval ) {
        if ( interval.start() == interval.end() ) {
            throw new IllegalArgumentException("Interval.intersects is broken for intervals with start == end and may falsly return true");
        }
        return this.contains(interval.start) || this.contains(interval.end - 1);
    }

    @Override
    public int hashCode() {
        return Objects.hash( start, end );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        } else if ( obj == null ) {
            return false;
        } else if ( !( obj instanceof Interval ) ) {
            return false;
        }
        Interval interval = (Interval) obj;
        return ( start == interval.start ) && ( end == interval.end );
    }

    @Override
    public String toString() {
        return "[" + start + ";" + end + "]";
    }

}
