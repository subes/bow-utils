package be.bagofwords.application;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/23/14.
 */
public class BaseRunnableApplicationContextFactory extends BaseApplicationContextFactory {

    private MainClass mainClass;

    public BaseRunnableApplicationContextFactory(MainClass mainClass) {
        this.mainClass = mainClass;
    }

    @Override
    public String getApplicationName() {
        return mainClass.getClass().getSimpleName();
    }

    @Override
    public AnnotationConfigApplicationContext createApplicationContext() {
        singleton("mainClass", mainClass);
        return super.createApplicationContext();
    }
}
