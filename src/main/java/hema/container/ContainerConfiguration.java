package hema.container;

import hema.container.resolves.Resolver;
import hema.container.resolves.ResolverFactory;
import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

@Configuration
public class ContainerConfiguration {

    private final ApplicationContext app;

    public ContainerConfiguration(ApplicationContext app) {
        this.app = app;
    }

    @Bean
    @Lazy
    public Container container() {
        return new Application((ReplacerBindingBuilder) app.getBean(Replacer.class), new ResolverFactory(app));
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
        return new ContextualBindingBuilder(app.getBean(Replacer.class), app.getBean(Inflector.class));
    }

    @Bean
    @Lazy
    public Contextual contextual() {
        return (Contextual) app.getBean(Resolver.class);
    }
}