package hema.container;

import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

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

    @Bean
    @Lazy
    @Scope("prototype")
    public Replacer aliasable() {
        return new AliasBinding(new ConcurrentHashMap<>(), app.getBean(Inflector.class));
    }

    @Bean
    @Lazy
    @Primary
    public Resolver resolver() {
        return new Query(app.getBean(Replacer.class), app.getBean(Inflector.class));
    }
}