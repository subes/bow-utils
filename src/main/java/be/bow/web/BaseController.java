package be.bow.web;

import be.bow.application.annotations.BowComponent;
import be.bow.ui.UI;
import org.apache.commons.lang3.exception.ExceptionUtils;
import spark.Request;
import spark.Response;
import spark.Route;

@BowComponent
public abstract class BaseController extends Route {

    private String path;

    protected BaseController(String path) {
        super(path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            return handleRequest(request, response);
        } catch (Exception exp) {
            UI.writeError("Received exception while rendering " + this.getClass() + " for url " + getPath(), exp);
            String stackTrace = ExceptionUtils.getStackTrace(exp);
            return "<pre>" + stackTrace + "</pre>";
        }
    }

    protected abstract Object handleRequest(Request request, Response response) throws Exception;
}
