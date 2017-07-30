/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-7-29. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;

import be.bagofwords.util.SocketConnection;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteLogFactory implements ILoggerFactory {

    private final SocketConnection socketConnection;
    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

    public RemoteLogFactory(SocketConnection socketConnection) {
        this.socketConnection = socketConnection;
    }

    @Override
    public Logger getLogger(String name) {
        if (!this.loggerMap.containsKey(name)) {
            this.loggerMap.put(name, new RemoteLogger(socketConnection));
        }
        return this.loggerMap.get(name);
    }

}
