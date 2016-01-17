package io.bifroest.commons.util.json.merge;

import java.util.Iterator;

import org.json.JSONObject;
import org.slf4j.ext.XLogger;

import io.bifroest.commons.logging.LogService;

/**
 * Make a shallow merge, i.e. copy over top-level values. If two values with the
 * same key exist, the first one is used.
 */
public class SimpleUseFirstPolicy implements JSONMergePolicy {

    private static final XLogger log = LogService.getXLogger(SimpleUseFirstPolicy.class);

    @Override
    public JSONObject merge( JSONObject first, JSONObject second ) {
        log.entry( first, second );

        JSONObject target = new JSONObject();
        @SuppressWarnings( "rawtypes" )
        Iterator keys;
        String key;

        keys = first.keys();
        while ( keys.hasNext() ) {
            key = (String) keys.next();
            target.put( key, first.get( key ) );
        }

        keys = second.keys();
        while ( keys.hasNext() ) {
            key = (String) keys.next();
            if ( !target.has( key ) ) {
                target.put( key, second.get( key ) );
            }
        }
        log.exit( target );
        return target;
    }

}
