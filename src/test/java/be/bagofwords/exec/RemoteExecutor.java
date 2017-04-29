package be.bagofwords.exec;

/**
 * Created by koen on 28/04/17.
 */
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
