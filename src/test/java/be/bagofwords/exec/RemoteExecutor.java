/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-5-3. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;


public class RemoteExecutor {

    public void exec(PackedRemoteExec packedRemoteExec) {
        try {
            Runnable runner = (Runnable) RemoteExecUtil.loadRemoteRunner(packedRemoteExec);
            runner.run();
        } catch (Exception exp) {
            throw new ExecutePackedRemoteExec("Failed to execute " + packedRemoteExec.executorClassName, exp);
        }
    }

}
