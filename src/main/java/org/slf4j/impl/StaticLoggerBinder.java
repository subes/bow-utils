/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-7-29. For license
 * information see the LICENSE file in the root folder of this repository.
 */

// package org.slf4j.impl;
//
// import be.bagofwords.exec.RemoteLogFactory;
// import org.slf4j.ILoggerFactory;
// import org.slf4j.spi.LoggerFactoryBinder;
//
// public class StaticLoggerBinder implements LoggerFactoryBinder {
//
//     private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
//
//     private static RemoteLogFactory remoteLogFactory;
//
//     public static final StaticLoggerBinder getSingleton() {
//         return SINGLETON;
//     }
//
//     // to avoid constant folding by the compiler, this field must *not* be final
//     public static String REQUESTED_API_VERSION = "1.6.99"; // !final
//
//     private static final String loggerFactoryClassStr = RemoteLogFactory.class.getName();
//
//     public static void setRemoteLogFactory(RemoteLogFactory remoteLogFactory) {
//         StaticLoggerBinder.remoteLogFactory = remoteLogFactory;
//     }
//
//     public ILoggerFactory getLoggerFactory() {
//         return remoteLogFactory;
//     }
//
//     public String getLoggerFactoryClassStr() {
//         return loggerFactoryClassStr;
//     }
// }
