/*******************************************************************************
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-7-29. For license
 * information see the LICENSE file in the root folder of this repository.
 ******************************************************************************/

package be.bagofwords.exec;

import be.bagofwords.logging.LogLevel;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoteLogStatement {

    public LogLevel level;
    public String message;
    public String[] stackTrace;
    public String logger;

    public RemoteLogStatement(@JsonProperty("level") LogLevel level,
                              @JsonProperty("logger") String logger,
                              @JsonProperty("message") String message,
                              @JsonProperty("stackTrace") String[] stackTrace) {
        this.level = level;
        this.logger = logger;
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
