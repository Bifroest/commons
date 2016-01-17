package io.bifroest.commons.model;

import io.bifroest.commons.model.Metric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MetricTest {

    @Test
    public void test() {
        Metric m1 = new Metric("server.DOiegzAC.g.MgI7ssgY", 8326803759332840246l, 832.1259329233853);
        Metric m2 = new Metric("server.DOiegzAC.g.MgI7ssgY", 8326803759332840246l, 832.1259329233853);
        
        assertTrue(m1.hashCode() == m2.hashCode());
        assertTrue(m1.equals(m2));
    }

    @Test
    public void testThatItStaysEqualDuringSerialization() {
        Metric subject = new Metric( "foo.bar.baz", 12345, 42.7 );
        Metric subjectAfterSerialization = Metric.fromJSON( subject.toJSON() );
        assertEquals( subject, subjectAfterSerialization );
    }

}
