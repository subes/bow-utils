package be.bagofwords.web;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.ui.UI;
import be.bagofwords.util.HashUtils;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import spark.route.RouteMatcher;
import spark.route.RouteMatcherFactory;
import spark.webserver.SparkServer;
import spark.webserver.SparkServerFactory;

import java.util.List;

public class WebContainer implements CloseableComponent, ApplicationListener<ContextStartedEvent> {

    @Autowired
    private ApplicationContext applicationContext;

    private RouteMatcher routeMatcher;
    private SparkServerThread sparkServerThread;
    private int port;
    private String staticFolder;

    public WebContainer(int port) {
        this(port, null);
    }

    public WebContainer(int port, String staticFolder) {
        initialize(port, staticFolder);
    }

    public WebContainer(String applicationName) {
        this(applicationName, null);
    }

    public WebContainer(String applicationName, String staticFolder) {
        long hashCode = HashUtils.hashCode(applicationName);
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        int randomPort = (int) (1023 + (hashCode % (65535 - 1023)));
        initialize(randomPort, staticFolder);
    }

    private void initialize(int port, String staticFolder) {
        this.routeMatcher = RouteMatcherFactory.get();
        this.port = port;
        this.staticFolder = staticFolder;
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
        registerControllers();
        sparkServerThread = new SparkServerThread(port, staticFolder);
        sparkServerThread.start();
    }

    @Override
    public void terminate() {
        routeMatcher.clearRoutes();
        sparkServerThread.terminateAndWaitForFinish();
    }

    private void registerControllers() {
        List<? extends BaseController> controllers = SpringUtils.getInstantiatedBeans(applicationContext, BaseController.class);
        UI.write("Found " + controllers.size() + " controllers");
        for (BaseController controller : controllers) {
            registerController(controller);
        }
    }

    public void registerController(BaseController controller) {
        routeMatcher.parseValidateAddRoute("GET '" + controller.getPath() + "'", controller.getAcceptType(), controller);
        routeMatcher.parseValidateAddRoute("POST '" + controller.getPath() + "'", controller.getAcceptType(), controller);
    }

    public int getPort() {
        return port;
    }

    private static class SparkServerThread extends SafeThread {

        private int port;
        private String staticFolder;
        private SparkServer server;

        private SparkServerThread(int port, String staticFolder) {
            super("SparkServerThread", true);
            this.port = port;
            this.staticFolder = staticFolder;
        }

        @Override
        protected void runInt() throws Exception {
            try {
                server = SparkServerFactory.create(staticFolder != null);
                server.ignite("0.0.0.0", port, null, null, null, null, null, staticFolder);
            } catch (Exception exp) {
                UI.writeError("Error while trying to start spark server on port " + port);
                server = null;
            }
        }

        @Override
        protected void doTerminate() {
            if (server != null) {
                server.stop();
            }
        }
    }
}
