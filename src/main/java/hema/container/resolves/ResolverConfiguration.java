package hema.container.resolves;

import hema.container.Factory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Parameter;

@Configuration
public class ResolverConfiguration {

    private final ApplicationContext applicationContext;

    public ResolverConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @Lazy
    @Primary
    public Factory<Resolver, Parameter> factory() {
        return new ResolverFactory(applicationContext);
    }

}
