/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-5-3. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

public class RemoteExecutorTest {

    @Test
    public void testSeparateClass() {
        File testFile = new File(DummyRunnable.testFile);
        String message = "hello there";
        Assert.assertTrue(!testFile.exists() || testFile.delete());
        PackedRemoteExec packedRemoteExec = RemoteExecConfig.create(new DummyRunnable(message)).add(DependencyClass.class).pack();
        RemoteExecutor remoteExecutor = new RemoteExecutor();
        remoteExecutor.exec(packedRemoteExec);
        Assert.assertTrue(testFile.exists());
    }

    @Test(expected = RuntimeException.class)
    public void testInnerClass() {
        PackedRemoteExec packedRemoteExec = RemoteExecConfig.create(new InnerDummyRunnable()).add(DependencyClass.class).pack();
        RemoteExecutor remoteExecutor = new RemoteExecutor();
        remoteExecutor.exec(packedRemoteExec);
    }

    public static class InnerDummyRunnable implements Runnable, Serializable {

        @Override
        public void run() {
            DependencyClass dependency = new DependencyClass();
            dependency.doSomething();
        }
    }

}