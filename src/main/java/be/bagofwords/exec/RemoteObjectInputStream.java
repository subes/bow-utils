package be.bagofwords.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Created by koen on 28/04/17.
 */
public class RemoteObjectInputStream extends ObjectInputStream {

    private ClassLoader loader;

    public RemoteObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.loader = loader;
    }

    /**
     * Use the given ClassLoader rather than using the system class
     */
    protected Class resolveClass(ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException exp) {
            return super.resolveClass(classDesc);
        }
    }

}
