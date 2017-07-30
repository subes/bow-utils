package be.bagofwords.exec;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
