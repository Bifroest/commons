
package io.bifroest.commons.serialize.json;

import io.bifroest.commons.serialize.json.Serializers;
import io.bifroest.commons.serialize.json.JSONSerializable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class SerializersTest {

    @Test
    public void testToJSONArray() {
        List<JSONObject> inputs = Arrays.asList(
                new JSONObject().put( "foo", "foo" ),
                new JSONObject().put( "bar", "bar" ),
                new JSONObject().put( "baz", "baz" )
        );

        JSONArray result = inputs.stream().collect( Serializers.toJSONArray() );

        assertThat( result.length(), is( equalTo( inputs.size() ) ) );
        for ( int i = 0; i < inputs.size(); i++ ) {
            assertThat( result.get( i ), is( equalTo( inputs.get( i ) ) ) );
        }
    }

    @Test
    public void testToSerializedJSONArray() {
        List<JSONObject> serializations = Arrays.asList(
                new JSONObject().put( "foo", "foo" ),
                new JSONObject().put( "bar", "bar" ),
                new JSONObject().put( "baz", "baz" )
        );

        List<JSONSerializable> inputs = serializations.stream()
                                            .map( SerializersTest::serializerWithOutput )
                                            .collect( Collectors.toList() );

        JSONArray serializedObjects = inputs.stream().collect( Serializers.toSerializedJSONArray() );

        assertThat( serializedObjects.length(), is( equalTo( serializations.size() ) ) );
        for ( int i = 0; i < inputs.size(); i++ ) {
            assertThat( serializedObjects.get( i ), is( equalTo( serializations.get( i ) ) ) );
        }
    }

    @Test
    public void testCollectionToJSONArray() {
        List<JSONObject> serializations = Arrays.asList(
                new JSONObject().put( "foo", "foo" ),
                new JSONObject().put( "bar", "bar" ),
                new JSONObject().put( "baz", "baz" )
        );

        List<JSONSerializable> inputs = serializations.stream()
                                            .map( SerializersTest::serializerWithOutput )
                                            .collect( Collectors.toList() );

        JSONArray result = Serializers.serialize( inputs );

        assertThat( result.length(), is( equalTo( serializations.size() ) ) );
        for ( int i = 0; i < inputs.size(); i++ ) {
            assertThat( result.get( i ), is( equalTo( serializations.get( i ) ) ) );
        }
    }

    private static JSONSerializable serializerWithOutput( JSONObject output ) {
        JSONSerializable input = mock(JSONSerializable.class );
        when( input.toJSON() ).thenReturn( output );
        return input;
    }
}