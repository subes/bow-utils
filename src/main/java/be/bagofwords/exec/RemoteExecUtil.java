/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-5-3. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;

import be.bagofwords.exec.ExecutePackedRemoteExec;
import be.bagofwords.exec.PackedRemoteExec;
import be.bagofwords.exec.RemoteExecObjectInputStream;

import java.io.ByteArrayInputStream;

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
