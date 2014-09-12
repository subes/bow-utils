package be.bow.application.status;

import be.bow.application.ApplicationContextFactory;
import be.bow.application.ApplicationLifeCycle;
import be.bow.application.annotations.EagerBowComponent;
import be.bow.util.SpringUtils;
import be.bow.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

@EagerBowComponent
public class HttpApplicationStatus extends BaseController {

    @Autowired
    private ApplicationLifeCycle applicationLifeCycle;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ApplicationContextFactory applicationContextFactory;


    public HttpApplicationStatus() {
        super("/status");
    }

    protected String getOutput() throws IOException {
        StringBuilder sb = new StringBuilder();
        List<? extends StatusViewable> statusViewables = SpringUtils.getInstantiatedBeans(applicationContext, StatusViewable.class);
        for (StatusViewable statusViewable : statusViewables) {
            statusViewable.printHtmlStatus(sb);
        }
        return sb.toString();
    }


    @Override
    public String handleRequest(Request request, Response response) throws IOException {
        StringBuilder sb = new StringBuilder();
        String applicationName = applicationContextFactory.getApplicationName();
        sb.append("<html><head><title>" + applicationName + ": application status</title></head><body>");
        sb.append(getOutput());
        sb.append("</body></html>");
        return sb.toString();
    }


}
