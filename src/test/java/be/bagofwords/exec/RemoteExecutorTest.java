package be.bagofwords.exec;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by koen on 28/04/17.
 */
public class RemoteExecutorTest {

    @Test
    public void test() {
        File testFile = new File(DummyRunnable.testFile);
        String message = "hello there";
        Assert.assertTrue(!testFile.exists() || testFile.delete());
        PackedRemoteExec packedRemoteExec = RemoteExecConfig.create(new DummyRunnable(message)).add(DependencyClass.class).pack();
        RemoteExecutor remoteExecutor = new RemoteExecutor();
        remoteExecutor.exec(packedRemoteExec);
        Assert.assertTrue(testFile.exists());
    }

}