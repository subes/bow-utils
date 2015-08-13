package be.bagofwords.web;

import be.bagofwords.application.annotations.BowComponent;
import be.bagofwords.ui.UI;
import org.apache.commons.lang3.exception.ExceptionUtils;
import spark.*;

import java.util.Objects;

import static spark.Spark.before;

@BowComponent
public abstract class BaseController extends RouteImpl {

    private String path;
    private boolean allowCORS;

    protected BaseController(String path) {
        this(path, false);
    }

    protected BaseController(String path, boolean allowCORS) {
        super(path, "text/html");
        this.path = path;
        this.allowCORS = allowCORS;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            if (isAllowCORS()) {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Request-Method", "*");
                response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
                if ("OPTIONS".equals(request.requestMethod())) {
                    return "";
                }
            }
            return handleRequest(request, response);
        } catch (Exception exp) {
            response.status(500);
            UI.writeError("Received exception while rendering " + this.getClass() + " for url " + getPath(), exp);
            String stackTrace = ExceptionUtils.getStackTrace(exp);
            return "<pre>" + stackTrace + "</pre>";
        }
    }

    protected abstract Object handleRequest(Request request, Response response) throws Exception;

    public boolean isAllowCORS() {
        return allowCORS;
    }
}
