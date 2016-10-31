package be.bagofwords.application;

import be.bagofwords.application.memory.MemoryManager;
import be.bagofwords.cache.CachesManager;

public class MinimalApplicationContextFactory extends BaseApplicationContextFactory {

    @Override
    public void wireApplicationContext(ApplicationContext context) {
        super.wireApplicationContext(context);
        MemoryManager memoryManager = new MemoryManager();
        BowTaskScheduler bowTaskScheduler = new BowTaskScheduler();
        context.registerBean(memoryManager);
        context.registerBean(bowTaskScheduler);
        CachesManager cachesManager = new CachesManager(context);
        context.registerBean(cachesManager);
        ApplicationManager manager = new ApplicationManager(context);
        context.registerBean(manager);
    }
}
