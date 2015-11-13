package com.goodgame.profiling.commons.statistics.percentile;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;

public class PercentileEvent {
    private final String type;
    private final double value;

    public PercentileEvent( String type, double value ) {
        this.type = type;
        this.value = value;
    }

    public static void fire( String type, double value ) {
        EventBusManager.fire( new PercentileEvent( type, value ) );
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
        long temp;
        temp = Double.doubleToLongBits( value );
        result = prime * result + (int)( temp ^ ( temp >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        PercentileEvent other = (PercentileEvent)obj;
        if ( type == null ) {
            if ( other.type != null )
                return false;
        } else if ( !type.equals( other.type ) )
            return false;
        if ( Double.doubleToLongBits( value ) != Double.doubleToLongBits( other.value ) )
            return false;
        return true;
    }
}
