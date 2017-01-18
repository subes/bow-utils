package be.bagofwords.http;

import be.bagofwords.util.StringUtils;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 06/10/14.
 */
public class DownloadResult {
    private boolean success;
    private String errorMessage;

    public DownloadResult(boolean success, String errorMessage) {
        if (!success && StringUtils.isEmpty(errorMessage)) {
            throw new RuntimeException("An unsuccessful download should always have an error message.");
        }
        this.errorMessage = errorMessage;
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
