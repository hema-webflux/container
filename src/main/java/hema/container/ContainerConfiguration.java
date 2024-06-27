package hema.container;

import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ContainerConfiguration {

    private final ApplicationContext app;

    public ContainerConfiguration(ApplicationContext app) {
        this.app = app;
    }

    @Bean
    @Lazy
    public Container container() {
        Inflector inflector = app.getBean(Inflector.class);
        Replacer  aliasable = app.getBean(Replacer.class);

        return new Application(
                app,
                (AliasBinding) aliasable,
                new ResolverFactory(),
                new Queryable(aliasable, inflector),
                inflector
        );
    }

    @Bean("property")
    @Lazy
    public Replacer aliasable() {
        return new AliasBinding(new ConcurrentHashMap<>());
    }
}