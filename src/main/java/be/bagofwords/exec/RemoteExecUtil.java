package be.bagofwords.exec;

import java.io.ByteArrayInputStream;

/**
 * Created by koen on 28/04/17.
 */
public class RemoteExecUtil {

    public static Object loadRemoteRunner(PackedRemoteExec packedRemoteExec) {
        try {
            RemoteExecClassLoader classLoader = new RemoteExecClassLoader(packedRemoteExec, RemoteExecUtil.class.getClassLoader());
            ByteArrayInputStream bis = new ByteArrayInputStream(packedRemoteExec.executor);
            RemoteExecObjectInputStream ois = new RemoteExecObjectInputStream(bis, classLoader);
            Object executor = ois.readObject();
            ois.close();
            return executor;
        } catch (Exception exp) {
            throw new ExecutePackedRemoteExec("Failed to execute " + packedRemoteExec, exp);
        }
    }

}
