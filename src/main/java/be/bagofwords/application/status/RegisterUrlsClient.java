package be.bagofwords.application.status;

import be.bagofwords.application.ApplicationContext;
import be.bagofwords.application.BaseServer;
import be.bagofwords.util.SocketConnection;
import be.bagofwords.web.BaseController;
import be.bagofwords.web.WebContainer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 07/10/14.
 */
public class RegisterUrlsClient {

    private ApplicationContext applicationContext;

    public RegisterUrlsClient(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void startClient() {
        WebContainer webContainer = applicationContext.getBean(WebContainer.class);
        List<? extends BaseController> controllers = applicationContext.getBeans(BaseController.class);
        String applicationRoot = applicationContext.getConfig("application_root");
        for (BaseController controller : controllers) {
            registerPath(applicationRoot + ":" + webContainer.getPort() + controller.getPath());
        }
    }

    public void registerPath(String path) {
        SocketConnection connection = null;
        try {
            String databaseServerAddress = applicationContext.getConfig("database_server_address");
            int registerUrlServerPort = Integer.parseInt(applicationContext.getConfig("url_server_port"));
            connection = new SocketConnection(databaseServerAddress, registerUrlServerPort);
            connection.writeByte(RegisterUrlsServer.SEND_URL);
            connection.writeString(applicationContext.getApplicationName());
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
