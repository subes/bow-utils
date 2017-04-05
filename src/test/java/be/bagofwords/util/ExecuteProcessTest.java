package be.bagofwords.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by koen on 2/04/17.
 */
public class ExecuteProcessTest {

    @Test
    public void testExecProcessWithLotsOfOutput() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("ls", "-R");
        pb.directory(new File("/usr/lib"));
        ExecutionResult result = ExecuteProcess.exec(pb);
        Assert.assertTrue(result.isSuccess());
    }

}