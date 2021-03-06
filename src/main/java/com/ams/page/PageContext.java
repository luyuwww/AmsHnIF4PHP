package com.ams.page;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class PageContext extends Page {
    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    private static ThreadLocal<PageContext> context = new ThreadLocal<PageContext>();

    public static PageContext getContext() {
        PageContext ci = context.get();
        if (ci == null) {
            ci = new PageContext();
            context.set(ci);
        }
        return ci;
    }

    public static void removeContext() {
        context.remove();
    }

    protected void initialize() {

    }

}
