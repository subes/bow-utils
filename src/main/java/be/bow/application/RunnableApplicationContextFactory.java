package be.bow.application;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/5/14.
 */
public interface RunnableApplicationContextFactory extends ApplicationContextFactory {

    public Class<? extends MainClass> getMainClass();

}
