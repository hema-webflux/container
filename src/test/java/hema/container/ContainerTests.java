package hema.container;

import hema.web.inflector.InflectorConfiguration;
import org.json.JSONObject;
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

    @SuppressWarnings("unchecked")
    public Object value(String alias, Map<String, Object> datasource) {

        int dotPlaceholder = alias.indexOf(".");
        if (dotPlaceholder == -1) {
            return datasource.get(alias);
        }

        String currentKey    = alias.substring(0, dotPlaceholder);
        Object current = datasource.get(currentKey);

        if (current instanceof Map<?, ?>) {
            String remainingKeys = alias.substring(dotPlaceholder + 1);
            return value(remainingKeys, (Map<String, Object>) current);
        }

        return current;
    }

    @Test
    public void testFactoryNotNull() {
        assertNotNull(context.getBean(Container.class));
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

        // alias
        data.put("user_id", 22);
        data.put("toggle", "DISABLED");
        data.put("nested",Map.of("user", Map.of("address", Map.of("city", "BeiJin"))));
    }

    @Test
    public void testRegularBeanInstantiation() {
        Container factory = context.getBean(Container.class);
        User      user    = factory.make(User.class, data);
        assertNotNull(user);
        assertNotNull(user.address());
        assertEquals(1, user.address().id());
        assertEquals("Chengdu", user.address().city());
    }

    @Test
    public void testContainerAlias() {
        Container factory = context.getBean(Container.class);
        factory.when(User.class)
                .alias("id", "user_id")
                .alias("status", "toggle");

        User user = factory.make(User.class, data);
        assertEquals(Status.DISABLED, user.status());
        assertEquals(22, user.id());
    }

    @Test
    public void testAliasNested() {
        Container factory = context.getBean(Container.class);
        factory.when(Address.class)
                .alias("city", "nested.user.address.city");

        User user = factory.make(User.class, data);
        assertEquals("BeiJin", user.address().city());
    }

}
