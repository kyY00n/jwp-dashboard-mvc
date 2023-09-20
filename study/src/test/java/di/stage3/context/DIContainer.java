package di.stage3.context;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import static java.util.Arrays.stream;
import static org.assertj.core.util.Arrays.asList;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) {
        beans = new HashSet<>();
        for (final var clazz : classes) {
            if (isAlreadyMade(clazz)) {
                continue;
            }
            final var bean = createBean(clazz, classes);
            beans.add(bean);
        }
    }

    private boolean isAlreadyMade(final Class<?> clazz) {
        return beans.stream()
                .anyMatch(bean -> bean.getClass().equals(clazz));
    }

    private Object createBean(final Class<?> clazz, final Set<Class<?>> classes) {
        if (clazz.isInterface()) {
            return classes.stream()
                    .filter(aClass -> asList(aClass.getInterfaces()).contains(clazz))
                    .findFirst()
                    .map(aClass -> createBean(aClass, classes))
                    .orElse(null);
        }
        return getObject(clazz, classes);
    }

    private Object getObject(final Class<?> clazz, final Set<Class<?>> classes) {
        final var constructors = clazz.getConstructors();
        if (constructors.length > 1) {
            throw new IllegalStateException("생성자가 2개 이상인 클래스는 다룰 수 없습니다.");
        }
        final var constructor = constructors[0];
        final var parameterTypes = constructor.getParameterTypes();
        final var args = stream(parameterTypes)
                .map(paramType -> {
                    final var bean = getBean(paramType);
                    if (bean == null) {
                        return createBean(paramType, classes);
                    }
                    return bean;
                })
                .toArray();
        try {
            Object instance = constructor.newInstance(args);
            beans.add(instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("생성자가 존재하지 않습니다.");
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return beans.stream()
                .filter(bean -> {
                  if (aClass.getClass().isInterface()) {
                      return asList(bean.getClass().getInterfaces()).contains(aClass);
                  }
                  return bean.getClass().equals(aClass);
                })
                .map(bean -> (T) bean)
                .findFirst()
                .orElse(null);
    }

}
