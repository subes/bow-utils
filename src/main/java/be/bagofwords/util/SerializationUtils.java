package be.bagofwords.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SerializationUtils {

    public static final long LONG_NULL = Long.MAX_VALUE - 3;
    public static final double DOUBLE_NULL = Double.MAX_VALUE;
    public static final int INT_NULL = Integer.MAX_VALUE;
    public static final float FLOAT_NULL = Float.MAX_VALUE;
    public static final byte[] STRING_NULL;
    private static final String ENCODING = "UTF-8";

    private static final ObjectMapper prettyPrintObjectMapper = new ObjectMapper();
    private static final ObjectMapper defaultObjectMapper = new ObjectMapper();

    static {
        prettyPrintObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            STRING_NULL = "xyNUlLxy".getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeObject(Object object) {
        return serializeObject(object, false);
    }

    public static String serializeObject(Object object, boolean prettyPrint) {
        try {
            if (object instanceof Compactable) {
                ((Compactable) object).compact();
            }
            ObjectMapper objectMapper = prettyPrint ? prettyPrintObjectMapper : defaultObjectMapper;
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserializeObject(String object, Class<T> objectClass, Class... genericParams) {
        try {
            if (genericParams.length > 0) {
                JavaType type = defaultObjectMapper.getTypeFactory().constructParametricType(objectClass, genericParams);
                return defaultObjectMapper.readValue(object, type);
            } else {
                return defaultObjectMapper.readValue(object, objectClass);
            }
        } catch (IOException e) {
            String objectForMessage = object;
            if (!StringUtils.isEmpty(objectForMessage) && objectForMessage.length() > 200) {
                objectForMessage = objectForMessage.substring(0, 200) + "...";
            }
            throw new RuntimeException("Failed to read " + objectForMessage, e);
        }
    }

    public static String bytesToString(byte[] key) {
        return bytesToString(key, 0, key.length);
    }

    public static String bytesToString(byte[] key, int offset, int length) {
        try {
            return new String(key, offset, length, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] stringToBytes(String key) {
        try {
            return key.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OutOfMemoryError outOfMemoryError) {
            throw new RuntimeException("OOM while trying to convert " + key.substring(0, Math.min(100, key.length())) + " of length " + key.length(), outOfMemoryError);
        }
    }

    public static <T> T bytesToObject(byte[] bytes, Class<T> objectClass) {
        if (bytes == null) {
            return null;
        } else {
            if (objectClass == Long.class) {
                return (T) new Long(bytesToLong(bytes));
            } else if (objectClass == Double.class) {
                return (T) new Double(Double.longBitsToDouble(bytesToLong(bytes)));
            } else if (objectClass == Integer.class) {
                return (T) new Integer(bytesToInt(bytes));
            } else if (objectClass == Float.class) {
                return (T) new Float(Float.intBitsToFloat(bytesToInt(bytes)));
            } else if (objectClass == String.class) {
                return (T) bytesToString(bytes);
            } else {
                String objectAsString = bytesToString(bytes);
                return SerializationUtils.deserializeObject(objectAsString, objectClass);
            }
        }
    }

    public static <T> byte[] objectToBytesCheckForNull(T value, Class<T> objectClass) {
        if (objectClass == Long.class) {
            if (value == null) {
                return longToBytes(LONG_NULL);
            } else if (value.equals(LONG_NULL)) {
                throw new RuntimeException("Sorry " + value + " is a reserved value to indicate null.");
            } else {
                return longToBytes((Long) value);
            }
        } else if (objectClass == Double.class) {
            long valueAsLong;
            if (value == null) {
                valueAsLong = Double.doubleToLongBits(DOUBLE_NULL);
            } else if (value.equals(DOUBLE_NULL)) {
                throw new RuntimeException("Sorry " + value + " is a reserved value to indicate null");
            } else {
                valueAsLong = Double.doubleToLongBits((Double) value);
            }
            return longToBytes(valueAsLong);
        }
        if (objectClass == Integer.class) {
            if (value == null) {
                return intToBytes(INT_NULL);
            } else if (value.equals(INT_NULL)) {
                throw new RuntimeException("Sorry " + value + " is a reserved value to indicate null.");
            } else {
                return intToBytes((Integer) value);
            }
        } else if (objectClass == Float.class) {
            int valueAsInt;
            if (value == null) {
                valueAsInt = Float.floatToIntBits(FLOAT_NULL);
            } else if (value.equals(FLOAT_NULL)) {
                throw new RuntimeException("Sorry " + value + " is a reserved value to indicate null");
            } else {
                valueAsInt = Float.floatToIntBits((Float) value);
            }
            return intToBytes(valueAsInt);
        } else {
            if (value == null) {
                return STRING_NULL;
            }
            byte[] result;
            if (objectClass == String.class) {
                result = stringToBytes((String) value);
            } else if (ByteArraySerializable.class.isAssignableFrom(objectClass)) {
                result = ((ByteArraySerializable) value).toByteArray();
            } else {
                result = stringToBytes(SerializationUtils.serializeObject(value, false));
            }
            if (Arrays.equals(result, STRING_NULL)) {
                throw new RuntimeException("Sorry " + value + " is a reserved value to indicate null");
            } else {
                return result;
            }
        }
    }

    public static <T> T bytesToObjectCheckForNull(byte[] value, Class<T> objectClass) {
        return bytesToObjectCheckForNull(value, 0, value.length, objectClass);
    }

    public static <T> T bytesToObjectCheckForNull(byte[] value, int offset, int length, Class<T> objectClass) {
        if (objectClass == Long.class) {
            long response = bytesToLong(value, offset);
            if (response != LONG_NULL) {
                return (T) new Long(response);
            } else {
                return null;
            }
        } else if (objectClass == Double.class) {
            double response = Double.longBitsToDouble(bytesToLong(value, offset));
            if (response != DOUBLE_NULL) {
                return (T) new Double(response);
            } else {
                return null;
            }
        } else if (objectClass == Integer.class) {
            int response = bytesToInt(value, offset);
            if (response != INT_NULL) {
                return (T) new Integer(response);
            } else {
                return null;
            }
        } else if (objectClass == Float.class) {
            float response = Float.intBitsToFloat(bytesToInt(value, offset));
            if (response != FLOAT_NULL) {
                return (T) new Float(response);
            } else {
                return null;
            }
        } else {
            byte[] actualValue = Arrays.copyOfRange(value, offset, offset + length);
            if (Arrays.equals(STRING_NULL, actualValue)) {
                return null;
            } else {
                if (objectClass == String.class) {
                    return (T) bytesToString(actualValue);
                } else if (ByteArraySerializable.class.isAssignableFrom(objectClass)) {
                    try {
                        return objectClass.getConstructor(byte[].class).newInstance(actualValue);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not instantiate object of class " + objectClass + ". Does it have a constructor with as only argument an array of bytes?", e);
                    }
                } else {
                    return SerializationUtils.deserializeObject(bytesToString(actualValue), objectClass);
                }
            }
        }
    }

    public static <T> byte[] objectToBytes(T value, Class<T> objectClass) {
        if (objectClass == Long.class) {
            return longToBytes((Long) value);
        } else if (objectClass == Double.class) {
            return longToBytes(Double.doubleToLongBits((Double) value));
        } else if (objectClass == Integer.class) {
            return intToBytes((Integer) value);
        } else if (objectClass == Float.class) {
            return intToBytes(Float.floatToIntBits((Float) value));
        } else if (objectClass == String.class) {
            return stringToBytes((String) value);
        } else {
            return stringToBytes(SerializationUtils.serializeObject(value));
        }
    }

    public static <T> byte[] objectToCompressedBytes(T value, Class<T> objectClass) {
        try {
            return Snappy.compress(objectToBytes(value, objectClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T compressedBytesToObject(byte[] bytes, Class<T> objectClass) {
        try {
            return bytesToObject(Snappy.uncompress(bytes), objectClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        longToBytes(value, bytes, 0);
        return bytes;
    }

    public static void longToBytes(long value, byte[] bytes, int offset) {
        bytes[offset] = (byte) (value >>> 56);
        bytes[offset + 1] = (byte) (value >>> 48);
        bytes[offset + 2] = (byte) (value >>> 40);
        bytes[offset + 3] = (byte) (value >>> 32);
        bytes[offset + 4] = (byte) (value >>> 24);
        bytes[offset + 5] = (byte) (value >>> 16);
        bytes[offset + 6] = (byte) (value >>> 8);
        bytes[offset + 7] = (byte) (value);
    }


    public static long bytesToLong(byte[] bytes) {
        return bytesToLong(bytes, 0);
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        long result = (((long) bytes[offset] << 56) +
                ((long) (bytes[offset + 1] & 255) << 48) +
                ((long) (bytes[offset + 2] & 255) << 40) +
                ((long) (bytes[offset + 3] & 255) << 32) +
                ((long) (bytes[offset + 4] & 255) << 24) +
                ((bytes[offset + 5] & 255) << 16) +
                ((bytes[offset + 6] & 255) << 8) +
                (bytes[offset + 7] & 255));
        return result;
    }

    public static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((value >>> 24));
        bytes[1] = (byte) ((value >>> 16));
        bytes[2] = (byte) ((value >>> 8));
        bytes[3] = (byte) ((value));
        return bytes;
    }

    public static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, 0);
    }

    public static int bytesToInt(byte[] bytes, int offset) {
        int result = (bytes[offset] << 24) +
                ((bytes[offset + 1] & 255) << 16) +
                ((bytes[offset + 2] & 255) << 8) +
                (bytes[offset + 3] & 255);
        return result;
    }

    /**
     * Careful! Not compatible with above method to convert objects to byte arrays!
     */

    public static void writeObject(Object object, OutputStream outputStream) {
        try {
            if (object instanceof Compactable) {
                ((Compactable) object).compact();
            }
            defaultObjectMapper.writeValue(outputStream, object);
        } catch (IOException exp) {
            throw new RuntimeException("Failed to write object to outputstream", exp);
        }
    }

    /**
     * Careful! Not compatible with above method to convert objects to byte arrays!
     */

    public static <T> T readObject(Class<T> _class, InputStream inputStream) {
        try {
            return defaultObjectMapper.readValue(inputStream, _class);
        } catch (IOException exp) {
            throw new RuntimeException("Failed to read object from inputstream", exp);
        }
    }

    public static <T> int getWidth(Class<T> objectClass) {
        if (objectClass == Long.class || objectClass == Double.class) {
            return 8;
        } else if (objectClass == Integer.class || objectClass == Float.class) {
            return 4;
        } else {
            return -1;
        }
    }
}
