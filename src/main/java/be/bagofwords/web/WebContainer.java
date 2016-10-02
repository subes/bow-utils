package be.bagofwords.web;

import be.bagofwords.application.ApplicationContext;
import be.bagofwords.application.CloseableComponent;
import be.bagofwords.ui.UI;
import be.bagofwords.util.HashUtils;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.StringUtils;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WebContainer implements CloseableComponent {

    private ApplicationContext applicationContext;
    private Routes routes;
    private SparkServerThread sparkServerThread;
    private int port;

    public WebContainer(int port, ApplicationContext context) {
        initialize(port);
        this.applicationContext = context;
    }

    public WebContainer(String applicationName) {
        long hashCode = HashUtils.hashCode(applicationName);
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        int randomPort = (int) (1023 + (hashCode % (65535 - 1023)));
        initialize(randomPort);
        UI.write("Initialized web container on port " + randomPort);
    }

    private void initialize(int port) {
        this.routes = Routes.create();
        this.port = port;
    }

    public void startContainer() {
        registerControllers();
        String staticFolder = applicationContext.getConfig("static_folder", "");
        if (StringUtils.isEmpty(staticFolder)) {
            staticFolder = null;
        }
        sparkServerThread = new SparkServerThread(port, staticFolder, routes);
        sparkServerThread.start();
    }

    @Override
    public void terminate() {
        routes.clear();
        sparkServerThread.terminateAndWaitForFinish();
    }

    private void registerControllers() {
        List<? extends BaseController> controllers = applicationContext.getBeans(BaseController.class);
        UI.write("Found " + controllers.size() + " controllers");
        for (BaseController controller : controllers) {
            registerController(controller);
        }
    }

    public void registerController(BaseController controller) {
        routes.add(controller.getMethod() + " '" + controller.getPath() + "'", controller.getAcceptType(), controller);
        if (controller.isAllowCORS()) {
            routes.add("OPTIONS '" + controller.getPath() + "'", controller.getAcceptType(), controller);
        }
    }

    public int getPort() {
        return port;
    }

    private static class SparkServerThread extends SafeThread {

        private int port;
        private String staticFolder;
        private Routes routeMatcher;
        private EmbeddedServer server;

        private SparkServerThread(int port, String staticFolder, Routes routeMatcher) {
            super("SparkServerThread", true);
            this.port = port;
            this.staticFolder = staticFolder;
            this.routeMatcher = routeMatcher;
        }

        @Override
        protected void runInt() throws Exception {
            try {
                StaticFilesConfiguration staticFilesConfiguration = new StaticFilesConfiguration();
                if (staticFolder != null) {
                    staticFilesConfiguration.configure(staticFolder);
                }
                server = new EmbeddedJettyFactory().create(routeMatcher, staticFilesConfiguration, false);
                server.ignite("0.0.0.0", port, null, new CountDownLatch(1), 100, 1, 1000);
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
                server.extinguish();
            } catch (Exception exp) {
                UI.writeError("Received exception while terminating the spark server", exp);
            }
        }
    }
}
