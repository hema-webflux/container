package hema.container;

import hema.container.resolves.ResolverConfiguration;
import hema.web.inflector.InflectorConfiguration;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig({ContainerConfiguration.class, InflectorConfiguration.class, ResolverConfiguration.class})
public class ContainerTests {

    @Autowired
    private ApplicationContext context;

    private static final Map<String, Object> data = new HashMap<>();

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
        data.put("numbers", new Integer[]{1, 2, 3, 4, 5});

        // alias
        data.put("user_id", 22);
        data.put("toggle", "DISABLED");
        data.put("nested", Map.of("user", Map.of("address", Map.of("city", "BeiJin"))));
        data.put("user_status", "ENABLED");
        data.put("order_numbers", "100,50,99,22");
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
                .replacer("id", "user_id")
                .replacer("status", "toggle");

        User user = factory.make(User.class, data);
        assertEquals(Status.DISABLED, user.status());
        assertEquals(22, user.id());
    }

    @Test
    public void testAliasNested() {
        Container factory = context.getBean(Container.class);
        factory.when(Address.class)
                .replacer("city", "nested.user.address.city");

        User user = factory.make(User.class, data);
        System.out.println(user);
        assertEquals("BeiJin", user.address().city());
    }

    @Test
    public void testResolveArray() {
        Container factory = context.getBean(Container.class);

        Order order = factory.make(Order.class, data);
        assertEquals(5, order.numbers().length);
    }

    @Test
    public void testResolveArrayForReplacerAlias() {
        Container factory = context.getBean(Container.class);
        factory.when(Order.class)
                .replacer("numbers", "order_numbers");

        Order order = factory.make(Order.class, data);
        assertEquals(100, order.numbers()[0]);
    }

}
