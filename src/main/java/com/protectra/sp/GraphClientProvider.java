package com.protectra.sp;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.microsoft.graph.serviceclient.GraphServiceClient;

@Component
public class GraphClientProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        GraphClientProvider.context = applicationContext;
    }

    /**
     * Returns the shared GraphServiceClient instance managed by Spring.
     */
    public static GraphServiceClient getGraphServiceClientInstance() {
        return context.getBean(GraphServiceClient.class);
    }
}
