package be.bagofwords.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by koen on 23/02/17.
 */
public class BeanCreator {

    private static final Logger logger = LoggerFactory.getLogger(BeanCreator.class);
    private Set<Class> beansBeingCreated = new HashSet<>();

    public <T> T createBean(Class<T> beanClass, ApplicationContext applicationContext) {
        if (beansBeingCreated.contains(beanClass)) {
            throw new RuntimeException("Dependency while creating bean " + beanClass);
        }
        try {
            T newBean;
            try {
                //Constructor with a single argument, the module?
                newBean = beanClass.getConstructor(ApplicationContext.class).newInstance(applicationContext);
            } catch (NoSuchMethodException exp) {
                //Constructor without any arguments?
                newBean = beanClass.getConstructor().newInstance();
            }
            logger.info("Created bean "+newBean) ;
            return newBean;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create bean " + beanClass, e);
        } finally {
            beansBeingCreated.remove(beanClass);
        }
    }
}
