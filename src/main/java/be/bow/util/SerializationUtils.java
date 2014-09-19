package be.bow.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.JavaType;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SerializationUtils {

    private static final String encoding = "UTF-8";
    private final static ObjectMapper prettyPrintObjectMapper = new ObjectMapper();
    private final static ObjectMapper defaultObjectMapper = new ObjectMapper();

    static {
        prettyPrintObjectMapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
    }


    public static String objectToString(Object object) {
        return objectToString(object, false);
    }

    public static String objectToString(Object object, boolean prettyPrint) {
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

    public static <T> T stringToObject(String object, Class<T> objectClass, Class... genericParams) {
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
        try {
            return new String(key, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] stringToBytes(String key) {
        try {
            return key.getBytes(encoding);
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
                return SerializationUtils.stringToObject(objectAsString, objectClass);
            }
        }
    }

    public static <T> byte[] objectToBytes(T value) {
        if (value instanceof Long) {
            return longToBytes((Long) value);
        } else if (value instanceof Double) {
            return longToBytes(Double.doubleToLongBits((Double) value));
        } else if (value instanceof Integer) {
            return intToBytes((Integer) value);
        } else if (value instanceof Float) {
            return intToBytes(Float.floatToIntBits((Float) value));
        } else if (value instanceof String) {
            return stringToBytes((String) value);
        } else {
            return stringToBytes(SerializationUtils.objectToString(value));
        }
    }

    public static byte[] objectToCompressedBytes(Object object) {
        try {
            return Snappy.compress(objectToBytes(object));
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
        bytes[0] = (byte) (value >>> 56);
        bytes[1] = (byte) (value >>> 48);
        bytes[2] = (byte) (value >>> 40);
        bytes[3] = (byte) (value >>> 32);
        bytes[4] = (byte) (value >>> 24);
        bytes[5] = (byte) (value >>> 16);
        bytes[6] = (byte) (value >>> 8);
        bytes[7] = (byte) (value);
        return bytes;
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes.length < 8) {
            bytes = Arrays.copyOf(bytes, 8);
        }
        long result = (((long) bytes[0] << 56) +
                ((long) (bytes[1] & 255) << 48) +
                ((long) (bytes[2] & 255) << 40) +
                ((long) (bytes[3] & 255) << 32) +
                ((long) (bytes[4] & 255) << 24) +
                ((bytes[5] & 255) << 16) +
                ((bytes[6] & 255) << 8) +
                (bytes[7] & 255));
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
        int result = (bytes[0] << 24) +
                ((bytes[1] & 255) << 16) +
                ((bytes[2] & 255) << 8) +
                (bytes[3] & 255);
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
}
