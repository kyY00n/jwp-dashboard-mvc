package reflection;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;

        Method method = clazz.getMethod("test1");
        assertDoesNotThrow(() -> method.invoke(clazz.newInstance()));
    }
}
