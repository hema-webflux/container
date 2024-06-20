package hema.container;

import hema.web.inflector.InflectorConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig({ContainerConfiguration.class, InflectorConfiguration.class})
public class ContainerTests {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testFactoryNotNull() {
        assertNotNull(context.getBean(Factory.class));
    }

}
