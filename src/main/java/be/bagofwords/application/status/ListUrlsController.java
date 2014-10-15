package be.bagofwords.application.status;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.application.annotations.BowController;
import be.bagofwords.util.Pair;
import be.bagofwords.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 07/10/14.
 */

@BowController
public class ListUrlsController extends BaseController implements CloseableComponent {

    private final List<Pair<String, String>> urls;
    private final RegisterUrlsServer registerUrlsServer;

    @Autowired
    public ListUrlsController(RemoteRegisterUrlsServerProperties properties) {
        super("/paths");
        this.urls = new ArrayList<>();
        this.registerUrlsServer = new RegisterUrlsServer(properties.getRegisterUrlServerPort(), this);
        this.registerUrlsServer.start();
    }

    @Override
    protected String handleRequest(Request request, Response response) throws Exception {
        StringBuilder result = new StringBuilder();
        synchronized (urls) {
            for (Pair<String, String> url : urls) {
                result.append("<a href=\"" + url.getSecond() + "\">" + url.getFirst() + " " + url.getSecond() + "</a><br>");
            }
        }
        return result.toString();
    }

    public void registerUrl(String name, String url) {
        synchronized (urls) {
            Pair<String, String> toRegister = new Pair<>(name, url);
            if (!urls.contains(toRegister)) {
                urls.add(0, toRegister);
            }
            while (urls.size() > 20) {
                urls.remove(urls.get(urls.size() - 1));
            }
        }
    }

    @Override
    public void terminate() {
        registerUrlsServer.terminateAndWaitForFinish();
    }
}
