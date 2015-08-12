package be.bagofwords.application;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/4/14.
 */
public interface ApplicationContextFactory {

    void wireApplicationContext();

    AnnotationConfigApplicationContext getApplicationContext();

    public String getApplicationName();

}
