package di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.util.Arrays.asList;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) throws IllegalAccessException {
        this.beans = createBeans(classes);
        for (Object bean : this.beans) {
            setFields(bean);
        }
    }

    // 기본 생성자로 빈을 생성한다.
    private Set<Object> createBeans(final Set<Class<?>> classes) {
        return classes.stream()
                .map(aClass -> {
                    try {
                        Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
                        declaredConstructor.setAccessible(true);
                        return declaredConstructor.newInstance();
                    } catch (NoSuchMethodException
                            | InvocationTargetException
                            | InstantiationException
                            | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toSet());
    }

    private void setFields(final Object bean) throws IllegalAccessException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.get(bean) != null) {
                continue;
            }
            Object injectedBean = getBean(field.getType());
            try {
                field.setAccessible(true);
                field.set(bean, injectedBean);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return beans.stream()
                .filter(bean -> {
                    if (aClass.isInterface()) {
                        return asList(bean.getClass().getInterfaces()).contains(aClass);
                    }
                    return bean.getClass().equals(aClass);
                })
                .findFirst()
                .map(bean -> (T) bean)
                .orElseThrow(() -> new RuntimeException("해당하는 빈이 존재하지 않습니다."));
    }
}
