package be.bagofwords.exec;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by koen on 28/04/17.
 */
public class PackedRemoteObject {

    public final String objectClassName;
    public final byte[] serializedObject;
    public final Map<String, String> classSources;

    public PackedRemoteObject(@JsonProperty("objectClassName") String objectClassName,
                              @JsonProperty("serializedObject") byte[] serializedObject,
                              @JsonProperty("classSources") Map<String, String> classSources) {
        this.objectClassName = objectClassName;
        this.serializedObject = serializedObject;
        this.classSources = classSources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PackedRemoteObject that = (PackedRemoteObject) o;

        if (!objectClassName.equals(that.objectClassName)) return false;
        if (!Arrays.equals(serializedObject, that.serializedObject)) return false;
        return classSources.equals(that.classSources);
    }

    @Override
    public synchronized int hashCode() {
        int hash = objectClassName.hashCode();
        hash = 31 * hash + Arrays.hashCode(serializedObject);
        hash = 31 * hash + classSources.hashCode();
        return hash;
    }
}
