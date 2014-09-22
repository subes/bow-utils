package be.bagofwords.web;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import spark.route.RouteMatcher;
import spark.route.RouteMatcherFactory;
import spark.webserver.SparkServer;
import spark.webserver.SparkServerFactory;

import javax.annotation.PostConstruct;
import java.util.List;

public class WebContainer implements CloseableComponent {

    @Autowired
    private ApplicationContext applicationContext;

    private RouteMatcher routeMatcher;
    private SparkServerThread sparkServerThread;
    private int port;

    public WebContainer(int port) {
        this.routeMatcher = RouteMatcherFactory.get();
        this.port = port;
    }

    @PostConstruct
    public void initialize() {
        registerControllers();
        sparkServerThread = new SparkServerThread(port);
        sparkServerThread.start();
    }

    @Override
    public void close() {
        routeMatcher.clearRoutes();
        sparkServerThread.close();
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
        private SparkServer server;

        private SparkServerThread(int port) {
            super("SparkServerThread", true);
            this.port = port;
        }

        @Override
        protected void runInt() throws Exception {
            try {
                server = SparkServerFactory.create(false);
                server.ignite("0.0.0.0", port, null, null, null, null, null, null);
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
