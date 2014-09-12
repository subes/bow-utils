package be.bow.util;

public class ReflectionUtils {

    public static <T> T createObject(Class<T> _class) {
        try {
            return _class.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create object from class " + _class + ". Does this class have an empty constructor?", e);
        }
    }

}
