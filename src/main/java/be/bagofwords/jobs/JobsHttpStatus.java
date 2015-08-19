package be.bagofwords.jobs;

import be.bagofwords.application.annotations.EagerBowComponent;
import be.bagofwords.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import spark.Request;
import spark.Response;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/8/14.
 */
@EagerBowComponent
public class JobsHttpStatus extends BaseController {

    @Autowired
    private JobRunner jobRunner;

    public JobsHttpStatus() {
        super("/progress");
    }

    @Override
    protected Object handleRequest(Request request, Response response) throws Exception {
        return jobRunner.createHtmlStatus();
    }
}