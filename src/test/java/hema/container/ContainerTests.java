package hema.container;

import hema.web.inflector.InflectorConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig({ContainerConfiguration.class, InflectorConfiguration.class})
public class ContainerTests {

    @Autowired
    private ApplicationContext context;

    private static Map<String, Object> data;

    @Test
    public void testFactoryNotNull() {
        assertNotNull(context.getBean(Factory.class));
    }

    @Test
    public void testRegularBeanInstantiation() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "1");
        data.put("name", "tom");
        data.put("email", "tom@hotmail.com");
        data.put("address", "{\"id\":1,\"user_id\":1,\"city\":\"Chengdu\",\"address\":\"Shizhishan\"}");
        data.put("status", "ENABLED");

        Factory factory = context.getBean(Factory.class);
        User user = factory.make(User.class, data);
        assertNotNull(user);
        System.out.println(user);
    }

}
