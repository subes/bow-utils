package be.bagofwords.exec;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by koen on 5/05/17.
 */
public class ResourcesClassSourceReader implements ClassSourceReader {
    @Override
    public String readSource(Class _class) {
        String resourcePath = "remote-exec/" + _class.getName().replace('.', '/') + ".java.remote";
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new RuntimeException("Could not find resource " + resourcePath + ". Did the remote-exec annotation processor run correctly?");
        }
        try {
            String source = IOUtils.toString(resourceStream, StandardCharsets.UTF_8);
            IOUtils.closeQuietly(resourceStream);
            return source;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read contents of " + resourcePath);
        }
    }
}
