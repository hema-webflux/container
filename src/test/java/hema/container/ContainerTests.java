package hema.container;

import hema.web.inflector.InflectorConfiguration;
import org.json.JSONObject;
import org.json.JSONWriter;
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

    private static final Map<String, Object> data = new HashMap<>();

    @Test
    public void testFactoryNotNull() {
        assertNotNull(context.getBean(Factory.class));
    }

    @BeforeAll
    public static void before() {
        Map<String, Object> address = new HashMap<>();
        address.put("id", 1);
        address.put("user_id", 1);
        address.put("city", "Chengdu");
        address.put("address", "Jinjiang");

        data.put("id", "1");
        data.put("name", "tom");
        data.put("email", "tom@hotmail.com");
        data.put("address", new JSONObject(address).toString());
        data.put("status", "ENABLED");
    }

    @Test
    public void testRegularBeanInstantiation() {
        Factory factory = context.getBean(Factory.class);
        User    user    = factory.make(User.class, data);
        assertNotNull(user);
        System.out.println(user.address());
    }

}
