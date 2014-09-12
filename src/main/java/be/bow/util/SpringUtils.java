package be.bow.util;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class SpringUtils {

    public static <T> List<? extends T> getInstantiatedBeans(ApplicationContext applicationContext, Class<T> _class) {
        List<? extends T> singletons = new ArrayList<>();
        String[] all = applicationContext.getBeanDefinitionNames();
        ConfigurableListableBeanFactory clbf = ((AbstractApplicationContext) applicationContext).getBeanFactory();
        for (String name : all) {
            Object s = clbf.getSingleton(name);
            if (s != null && _class.isAssignableFrom(s.getClass())) {
                ((List) singletons).add(s);
            }
        }
        return singletons;
    }

}
