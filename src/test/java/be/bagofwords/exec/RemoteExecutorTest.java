/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-5-3. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class RemoteExecutorTest {

    public static String testFile = "/tmp/dummyRunnableTestFile2.txt";

    @Test
    public void testSeparateClass() throws IOException {
        String message = "hello there";
        File testFile = new File(DummyRunnable.testFile);
        Assert.assertTrue(!testFile.exists() || testFile.delete());
        PackedRemoteExec packedRemoteExec = RemoteExecConfig.create(new DummyRunnable(message)).add(DependencyClass.class).pack();
        RemoteExecutor remoteExecutor = new RemoteExecutor();
        remoteExecutor.exec(packedRemoteExec);
        Assert.assertTrue(testFile.exists());
        Assert.assertEquals(message, FileUtils.readFileToString(testFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testInnerClass() throws IOException {
        String message = "bye for now";
        File testFile = new File(RemoteExecutorTest.testFile);
        Assert.assertTrue(!testFile.exists() || testFile.delete());
        PackedRemoteExec packedRemoteExec = RemoteExecConfig.create(new InnerDummyRunnable(message)).add(DependencyClass.class).pack();
        RemoteExecutor remoteExecutor = new RemoteExecutor();
        remoteExecutor.exec(packedRemoteExec);
        Assert.assertTrue(testFile.exists());
        Assert.assertEquals(message, FileUtils.readFileToString(testFile, StandardCharsets.UTF_8));
    }

    @RemoteClass
    public static class InnerDummyRunnable implements Runnable, Serializable {

        private final String message;

        public InnerDummyRunnable(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            DependencyClass dependency = new DependencyClass();
            dependency.doSomething();
            try {
                FileUtils.write(new File(testFile), message, "UTF-8");
            } catch (IOException exp) {
                throw new RuntimeException("Failed to write to test file", exp);
            }
        }
    }

}