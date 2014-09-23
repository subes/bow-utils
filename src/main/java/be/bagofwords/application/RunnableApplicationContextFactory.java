package be.bagofwords.application;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/5/14.
 */
public interface RunnableApplicationContextFactory extends ApplicationContextFactory {

    public MainClass getMainClass();

}
