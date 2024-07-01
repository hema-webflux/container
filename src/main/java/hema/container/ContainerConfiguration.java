package hema.container;

import hema.container.resolves.Resolver;
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
        return new Application((ReplacerBindingBuilder) app.getBean(Replacer.class), app.getBean(Factory.class));
    }

    @Bean
    @Lazy
    public Replacer replacer() {
        return new ReplacerBindingBuilder(app.getBean(Inflector.class));
    }

    @Bean
    @Lazy
    @Primary
    public Resolver resolver() {
        return new Contextual(app.getBean(Replacer.class), app.getBean(Inflector.class), new ConcurrentHashMap<>());
    }
}