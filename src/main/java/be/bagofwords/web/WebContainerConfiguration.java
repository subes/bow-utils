package be.bagofwords.web;

import be.bagofwords.application.MainClass;
import be.bagofwords.application.annotations.BowConfiguration;
import be.bagofwords.util.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@BowConfiguration
public class WebContainerConfiguration {

    @Bean
    @Autowired
    public WebContainer createWebContainer(MainClass mainClass) {
        long hashCode = HashUtils.hashCode(mainClass.getClass().getSimpleName());
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        int randomPortForApplication = (int) (1023 + (hashCode % (65535 - 1023)));
        return new WebContainer(randomPortForApplication);
    }

}
