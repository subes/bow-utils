package be.bagofwords.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by koen on 28/04/17.
 */
public class RemoteExecConfig {

    private final String executorClassName;
    private final Object executor;
    private Set<Class> requiredClasses = new HashSet<>();
    private ClassSourceReader sourceReader = new ResourcesClassSourceReader();

    public static RemoteExecConfig create(Object executor) {
        return new RemoteExecConfig(executor);
    }

    private RemoteExecConfig(Object executor) {
        this.executor = executor;
        Class<?> executorClass = executor.getClass();
        this.executorClassName = executorClass.getName();
        add(executorClass);
    }

    public RemoteExecConfig add(Class _class) {
        if (_class.getEnclosingClass() != null) {
            throw new RuntimeException("Can not add class " + _class + ": Using inner classes or lambda's as remote classes is currently not supported");
        }
        Annotation annotation = _class.getAnnotation(RemoteClass.class);
        if (annotation == null) {
            throw new RuntimeException("Can not add class " + _class + ": Classes that need to run remotely need to be annotated with @RemoteClass");
        }
        requiredClasses.add(_class);
        return this;
    }

    public RemoteExecConfig sourceReader(ClassSourceReader sourceReader) {
        this.sourceReader = sourceReader;
        return this;
    }

    public PackedRemoteExec pack() {
        Map<String, String> classSources = new HashMap<>();
        for (Class _class : requiredClasses) {
            classSources.put(_class.getCanonicalName(), sourceReader.readSource(_class));
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(executor);
            oos.close();
            byte[] serializedExecutor = bos.toByteArray();
            return new PackedRemoteExec(executorClassName, serializedExecutor, classSources);
        } catch (IOException exp) {
            throw new PackException("Failed to serialize object " + executor, exp);
        }
    }

    public String getExecutorClassName() {
        return executorClassName;
    }
}
