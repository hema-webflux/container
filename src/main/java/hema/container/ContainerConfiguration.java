package hema.container;

import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ContainerConfiguration {

    private final ApplicationContext app;

    public ContainerConfiguration(ApplicationContext app) {
        this.app = app;
    }

    @Bean
    @Lazy
    @SuppressWarnings("unchecked")
    public Container container() {
        return new Application((AliasBinding) app.getBean(Replacer.class), app.getBean(Factory.class));
    }

    @Bean("property")
    @Lazy
    public Replacer aliasable() {
        return new AliasBinding(new ConcurrentHashMap<>(), app.getBean(Inflector.class));
    }
}