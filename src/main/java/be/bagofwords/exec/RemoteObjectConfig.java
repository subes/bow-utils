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
public class RemoteObjectConfig {

    private final String objectClassName;
    private final Object object;
    private Set<Class> requiredClasses = new HashSet<>();
    private ClassSourceReader sourceReader = new ResourcesClassSourceReader();

    public static RemoteObjectConfig create(Object object) {
        return new RemoteObjectConfig(object);
    }

    private RemoteObjectConfig(Object object) {
        this.object = object;
        Class<?> objectClass = object.getClass();
        this.objectClassName = objectClass.getName();
        add(objectClass);
    }

    public RemoteObjectConfig add(Class _class) {
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

    public RemoteObjectConfig sourceReader(ClassSourceReader sourceReader) {
        this.sourceReader = sourceReader;
        return this;
    }

    public PackedRemoteObject pack() {
        Map<String, String> classSources = new HashMap<>();
        for (Class _class : requiredClasses) {
            classSources.put(_class.getCanonicalName(), sourceReader.readSource(_class));
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
            byte[] serializedExecutor = bos.toByteArray();
            return new PackedRemoteObject(objectClassName, serializedExecutor, classSources);
        } catch (IOException exp) {
            throw new PackException("Failed to serialize object " + object, exp);
        }
    }

    public String getObjectClassName() {
        return objectClassName;
    }
}
