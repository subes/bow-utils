package be.bagofwords.application;

public class SocketServerContextFactory extends MinimalApplicationContextFactory {

    @Override
    public void wireApplicationContext(ApplicationContext context) {
        super.wireApplicationContext(context);
        SocketServer server = new SocketServer(Integer.parseInt(context.getConfig("socket_port")));
        context.registerBean(server);
    }
}
