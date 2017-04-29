package be.bagofwords.exec;

/**
 * Created by koen on 12/03/17.
 */

import java.io.InputStream;

public class RemoteExecClassLoader extends ClassLoader {

    private final PackedRemoteExec packedRemoteExec;

    public RemoteExecClassLoader(PackedRemoteExec packedRemoteExec, ClassLoader parentClassLoader) {
        super(parentClassLoader);
        this.packedRemoteExec = packedRemoteExec;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        throw new RuntimeException("Loading resources is currently not supported");
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        if (packedRemoteExec.classDefinitions.containsKey(name)) {
            System.out.println("Loading class '" + name + "' from packaged remote");
            byte[] classAsBytes = packedRemoteExec.classDefinitions.get(name);
            Class c = defineClass(name, classAsBytes, 0, classAsBytes.length);
            resolveClass(c);
            return c;
        } else {
            return super.loadClass(name);
        }
    }
}