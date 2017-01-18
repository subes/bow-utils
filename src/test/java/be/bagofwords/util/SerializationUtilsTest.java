package be.bagofwords.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/19/14.
 */
public class SerializationUtilsTest {

    @Test
    public void testSimplePrimitiveValues() {
        checkConversion(1, Integer.class);
        checkConversion(-1, Integer.class);
        checkConversion(1l, Long.class);
        checkConversion(-1l, Long.class);
        checkConversion(1f, Float.class);
        checkConversion(-1f, Float.class);
        checkConversion(1d, Double.class);
        checkConversion(-1d, Double.class);
    }

    @Test
    public void testNullValues() {
        checkConversion(null, Long.class);
        checkConversion(null, Double.class);
        checkConversion(null, Float.class);
        checkConversion(null, Integer.class);
        checkConversion(null, String.class);
        checkConversion("null", String.class);
        checkConversion(null, Date.class);
    }

    private void checkConversion(Object obj, Class objectClass) {
        Assert.assertEquals(obj, SerializationUtils.bytesToObjectCheckForNull(SerializationUtils.objectToBytesCheckForNull(obj, objectClass), objectClass));
    }
}
