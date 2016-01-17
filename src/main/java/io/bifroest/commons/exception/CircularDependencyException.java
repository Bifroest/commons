package io.bifroest.commons.exception;

import java.util.List;
import java.util.Map;

public class CircularDependencyException extends Exception {
    private Map<String, List<String>> systemDependencies;

    public CircularDependencyException( String message, Map<String, List<String>> systemDependencies ) {
        super(message);
        this.systemDependencies = systemDependencies;
    }

    // I'd really like to put this into the super(message)-call in the constructor.
    // Unfortunately, super() has to be the very first thing called.
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder( super.getMessage() + "\n");
        sb.append( "digraph { ");
        for ( String dependingSystem : systemDependencies.keySet() ) {
            for ( String requiredSystem : systemDependencies.get( dependingSystem ) ) {
                sb.append( "\"" )
                  .append( dependingSystem )
                  .append( "\" -> \"" )
                  .append( requiredSystem )
                  .append( "\"; " );
            }
        }
        sb.append( " } ");
        return sb.toString();
    }
}