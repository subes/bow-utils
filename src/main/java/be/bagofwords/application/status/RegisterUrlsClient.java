package be.bagofwords.application.status;

import be.bagofwords.application.ApplicationContextFactory;
import be.bagofwords.application.BaseServer;
import be.bagofwords.util.SpringUtils;
import be.bagofwords.util.WrappedSocketConnection;
import be.bagofwords.web.BaseController;
import be.bagofwords.web.WebContainer;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;

import java.io.IOException;
import java.util.List;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 07/10/14.
 */
public class RegisterUrlsClient implements ApplicationListener<ContextStartedEvent> {

    @Autowired
    private RemoteRegisterUrlsServerProperties properties;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ApplicationContextFactory applicationContextFactory;
    @Autowired
    private WebContainer webContainer;

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
        List<? extends BaseController> controllers = SpringUtils.getInstantiatedBeans(applicationContext, BaseController.class);
        for (BaseController controller : controllers) {
            registerPath(properties.getApplicationUrlRoot() + ":" + webContainer.getPort() + controller.getPath());
        }
    }

    public void registerPath(String path) {
        WrappedSocketConnection connection = null;
        try {
            connection = new WrappedSocketConnection(properties.getDatabaseServerAddress(), properties.getRegisterUrlServerPort());
            connection.writeByte(RegisterUrlsServer.SEND_URL);
            connection.writeString(applicationContextFactory.getApplicationName());
            connection.writeString(path);
            connection.flush();
            long result = connection.readLong();
            if (result != BaseServer.LONG_OK) {
                throw new RuntimeException("Unexpected response " + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(connection);
        }
    }

}
