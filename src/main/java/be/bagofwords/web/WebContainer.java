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
import spark.route.RouteMatcherFactory;
import spark.route.SimpleRouteMatcher;
import spark.webserver.SparkServer;
import spark.webserver.SparkServerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WebContainer implements CloseableComponent, ApplicationListener<ContextStartedEvent> {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private StaticFolderConfiguration staticFolderConfiguration;


    private SimpleRouteMatcher routeMatcher;
    private SparkServerThread sparkServerThread;
    private int port;

    public WebContainer(int port) {
        initialize(port);
    }

    public WebContainer(String applicationName) {
        long hashCode = HashUtils.hashCode(applicationName);
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        int randomPort = (int) (1023 + (hashCode % (65535 - 1023)));
        initialize(randomPort);
    }

    private void initialize(int port) {
        this.routeMatcher = RouteMatcherFactory.get();
        this.port = port;
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
        registerControllers();
        String staticFolder = null;
        if (staticFolderConfiguration != null) {
            staticFolder = staticFolderConfiguration.getStaticFolder();
        }
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
                server.ignite("0.0.0.0", port, null, null, null, null, null, staticFolder, new CountDownLatch(1), 100, 1, 1000);
            } catch (Exception exp) {
                UI.writeError("Error while trying to start spark server on port " + port);
                server = null;
            }
        }


        //We don't call super.interrupt() because this occasionally makes the spark server throw
        //an interrupted exception and terminates the VM!
        @Override
        public void interrupt() {
            try {
                server.stop();
            } finally {

            }
        }
    }
}
