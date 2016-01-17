
package io.bifroest.commons.serialize.json;

import java.util.Collection;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Serializers {

    private Serializers() {
        // Utility class -- prevent instantiation
    }

    public static JSONArray serialize( Collection<JSONSerializable> input ) {
        return input.stream().collect( toSerializedJSONArray() );
    }

    public static Collector<JSONObject, JSONArray, JSONArray> toJSONArray() {
        return Collector.of(
                JSONArray::new,
                JSONArray::put,
                (left, right) -> {
                    for ( int i = 0; i < right.length(); i++ ) {
                        left.put( right.get( i ) );
                    }
                    return left;
                }
        );
    }

    public static Collector<JSONSerializable, JSONArray, JSONArray> toSerializedJSONArray() {
        return Collector.of(
                JSONArray::new,
                (array, element) -> array.put( element.toJSON() ),
                (left, right) -> {
                    for ( int i = 0; i < right.length(); i++ ) {
                        left.put( right.get( i ) );
                    }
                    return left;
                }
        );
    }
}
