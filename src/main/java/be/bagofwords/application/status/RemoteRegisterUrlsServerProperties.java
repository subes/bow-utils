package be.bagofwords.application.status;

import be.bagofwords.application.EnvironmentProperties;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 07/10/14.
 */
public interface RemoteRegisterUrlsServerProperties extends EnvironmentProperties {

    public String getDatabaseServerAddress();

    public int getRegisterUrlServerPort();
}
