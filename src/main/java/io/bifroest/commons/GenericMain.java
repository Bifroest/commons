package io.bifroest.commons;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import io.bifroest.commons.boot.BootLoaderNG;
import io.bifroest.commons.boot.InitD;
import io.bifroest.commons.logging.LogService;
import io.bifroest.commons.environment.AbstractCommonEnvironment;

import java.nio.file.Paths;
import java.nio.file.Path;

import java.lang.reflect.Constructor;

public class GenericMain {
    private static final Logger log = LogService.getLogger(GenericMain.class);

    private GenericMain() {
        // Utility class
    }

    public static void main( String[] args ) throws Exception {
        AbstractCommonEnvironment e = createEnvironment( Paths.get( args[0] ), args[1] );
        if ( e == null ) return;

        BootLoaderNG<AbstractCommonEnvironment> bootloader = new BootLoaderNG<AbstractCommonEnvironment>( e );
        e.setInitD( bootloader );
        JSONObject bootConfig = e.getConfiguration().getJSONObject( "bootloader" );
        JSONArray requiredSystems = bootConfig.getJSONArray( "enabled-systems" );
        for ( int i = 0; i < requiredSystems.length(); i++ ) {
            bootloader.enableSubsystem( requiredSystems.getString( i ) );
        }
        bootloader.boot();
    }

    private static AbstractCommonEnvironment createEnvironment( Path configFolder, String environmentClassName ) throws Exception {
        Class<?> environmentClass = Class.forName( environmentClassName );
        if ( !AbstractCommonEnvironment.class.isAssignableFrom( environmentClass )) {
            log.error( environmentClass.getName() + " cannot be assigned to AbstractCommonEnvironment" );
            return null;
        }
        Constructor<?> constructor = null;
        constructor = environmentClass.getConstructor( Path.class, InitD.class );
        if ( constructor != null ) {
            Object result = constructor.newInstance( configFolder, null );
            return (AbstractCommonEnvironment) result;
        }
        constructor = environmentClass.getConstructor( Path.class );
        if ( constructor != null ) {
            Object result = constructor.newInstance( configFolder );
            return (AbstractCommonEnvironment) result;
        }

        log.error( "Cannot find constructor (Path, InitD) or constructor (Path) of " + environmentClass.getName() );
        return null;
    }
}
