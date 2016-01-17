package io.bifroest.commons.decorating_factories;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.ext.XLogger;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.logging.LogService;

public abstract class DecoratorFactory<E extends Environment, T> {
    private static final XLogger log = LogService.getXLogger(DecoratorFactory.class);

    public T create( E environment, JSONObject config ) {
        log.entry( config );
        if ( config.has( "inner" ) ) {
            if ( config.get( "inner" ) instanceof JSONArray ) {
                log.debug( "need to recurse (multiple inners) " + config.getString( "type" ) );
                List<T> inners = new ArrayList<>();
                for( int i = 0; i < config.getJSONArray( "inner" ).length(); i++ ) {
                    inners.add( create( environment, config.getJSONArray( "inner" ).getJSONObject( i ) ) );
                }
                MultipleWrappingFactory<E, T> factory = findMultipleWrappingFactory( config.getString( "type" ) );
                try {
                    return log.exit( factory.wrap( environment, inners, config ) );
                } catch ( Exception e ) {
                    try {
                        for( T inner : inners ) {
                            if ( inner instanceof Closeable ) {
                                ((Closeable)inner).close();
                            }
                        }
                    } catch ( Exception e2 ) {
                        log.warn( "Things broke even more!", e2 );
                    }
                    throw e;
                }
            } else {
                log.debug( "need to recurse " + config.getString( "type" ) );
                T inner = create( environment, config.getJSONObject( "inner" ) );
                WrappingFactory<E, T> factory = findWrappingFactory( config.getString( "type" ) );
                try {
                    return log.exit( factory.wrap( environment, inner, config ) );
                } catch ( Exception e ) {
                    try {
                        if ( inner instanceof Closeable ) {
                            ((Closeable)inner).close();
                        }
                    } catch ( Exception e2 ) {
                        log.warn( "Things broke even more!", e2 );
                    }
                    throw e;
                }
            }
        } else {
            log.debug( "using basic factory" );
            BasicFactory<E, T> basicFactory = findBasicFactory( config.getString( "type" ) );
            return log.exit( basicFactory.create( environment, config ) );
        }
    }

    public void addRequirements( Collection<String> requiredSystems, JSONObject config ) {
        log.entry( config );
        if ( config.has( "inner" ) ) {
            if ( config.get( "inner" ) instanceof JSONArray ) {
                log.debug( "need to recurse (multiple inners) " + config.getString( "type" ) );
                for( int i = 0; i < config.getJSONArray( "inner" ).length(); i++ ) {
                    addRequirements( requiredSystems, config.getJSONArray( "inner" ).getJSONObject( i ) );
                }
                MultipleWrappingFactory<E, T> factory = findMultipleWrappingFactory( config.getString( "type" ) );
                factory.addRequiredSystems( requiredSystems, config );
            } else {
                log.debug( "need to recurse " + config.getString( "type" ) );
                addRequirements( requiredSystems, config.getJSONObject( "inner" ) );
                WrappingFactory<E, T> factory = findWrappingFactory( config.getString( "type" ) );
                factory.addRequiredSystems( requiredSystems, config );
            }
        } else {
            log.debug( "using basic factory" );
            BasicFactory<E, T> basicFactory = findBasicFactory( config.getString( "type" ) );
            basicFactory.addRequiredSystems( requiredSystems, config );
        }
        log.trace( "Resulting required systems: {}",  requiredSystems.toString() ) ;
        log.exit();
    }

    private MultipleWrappingFactory<E, T> findMultipleWrappingFactory( String type ) {
        log.entry( type );
        for( MultipleWrappingFactory<E, T> wrappingFactory : getMultipleWrappingFactories() ) {
            log.trace( "considering: {}", wrappingFactory );
            log.trace( "type.equalsIgnoreCase( wrappingFactory.handledType() ) = {}", type.equalsIgnoreCase( wrappingFactory.handledType() ) );
            if ( type.equalsIgnoreCase( wrappingFactory.handledType() ) ) {
                return log.exit( wrappingFactory );
            }
        }
        throw log.throwing( new IllegalArgumentException( "Cannot find factory for type " + type + " with inner class and current environment" ) );
    }

    private WrappingFactory<E, T> findWrappingFactory( String type ) {
        log.entry( type );
        for( WrappingFactory<E, T> wrappingFactory : getWrappingFactories() ) {
            log.trace( "considering: {}", wrappingFactory );
            log.trace( "type.equalsIgnoreCase( wrappingFactory.handledType() ) = {}", type.equalsIgnoreCase( wrappingFactory.handledType() ) );
            if ( type.equalsIgnoreCase( wrappingFactory.handledType() ) ) {
                return log.exit( wrappingFactory );
            }
        }
        throw log.throwing( new IllegalArgumentException( "Cannot find factory for type " + type + " with inner class and current environment" ) );
    }

    private BasicFactory<E, T> findBasicFactory( String type ) {
        log.entry( type );
        for( BasicFactory<E, T> basicFactory : getBasicFactories() ) {
            log.trace( "considering: {}", basicFactory );
            log.trace( "type.equalsIgnoreCase( basicFactory.handledType() ) = {}", type.equalsIgnoreCase( basicFactory.handledType() ) );
            if ( type.equalsIgnoreCase( basicFactory.handledType() ) ) {
                return log.exit( basicFactory );
            }
        }
        throw log.throwing( new IllegalArgumentException( "Cannot find factory for type " + type + " with current environment" ) );
    }

    protected abstract Collection<MultipleWrappingFactory<E, T>> getMultipleWrappingFactories();
    protected abstract Collection<WrappingFactory<E, T>> getWrappingFactories();
    protected abstract Collection<BasicFactory<E, T>> getBasicFactories();
}
