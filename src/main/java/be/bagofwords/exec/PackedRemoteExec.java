package be.bagofwords.exec;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by koen on 28/04/17.
 */
public class PackedRemoteExec {

    public final String executorClassName;
    public final byte[] executor;
    public final Map<String, byte[]> classDefinitions;

    public PackedRemoteExec(@JsonProperty("executorClassName") String executorClassName,
                            @JsonProperty("executor") byte[] executor,
                            @JsonProperty("classDefinitions") Map<String, byte[]> classDefinitions) {
        this.executorClassName = executorClassName;
        this.executor = executor;
        this.classDefinitions = classDefinitions;
    }
}
