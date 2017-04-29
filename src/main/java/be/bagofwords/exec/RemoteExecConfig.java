package be.bagofwords.exec;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
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

    public static RemoteExecConfig create(Object executor) {
        return new RemoteExecConfig(executor);
    }

    private RemoteExecConfig(Object executor) {
        this.executor = executor;
        Class<?> executorClass = executor.getClass();
        this.executorClassName = executorClass.getName();
        this.requiredClasses.add(executorClass);
    }

    public RemoteExecConfig add(Class _class) {
        requiredClasses.add(_class);
        return this;
    }

    public PackedRemoteExec pack() {
        Map<String, byte[]> classDefinitions = new HashMap<>();
        for (Class _class : requiredClasses) {
            classDefinitions.put(_class.getCanonicalName(), classToBytes(_class));
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(executor);
            oos.close();
            byte[] serializedExecutor = bos.toByteArray();
            return new PackedRemoteExec(executorClassName, serializedExecutor, classDefinitions);
        } catch (IOException exp) {
            throw new PackException("Failed to serialize object " + executor, exp);
        }
    }

    public byte[] classToBytes(Class _class) {
        String name = _class.getName();
        String classAsPath = name.replace('.', '/') + ".class";
        InputStream is = getClass().getClassLoader().getResourceAsStream(classAsPath);
        if (is == null) {
            throw new PackException("Could not find class " + classAsPath);
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(is, bos);
            bos.close();
            return bos.toByteArray();
        } catch (IOException exp) {
            throw new PackException("Failed to serialize class " + name);
        }
    }
}
