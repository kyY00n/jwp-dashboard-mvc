package di.stage4.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.util.Arrays.asList;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public static DIContainer createContainerForPackage(final String rootPackageName) {
        // todo: allClassesInPackage에서 @Service @Repository 애노테이션이 붙은 애들을 따로 가져온다.
        Set<Class<?>> allClassesInPackage = ClassPathScanner.getAllClassesInPackage(rootPackageName);
        return new DIContainer(allClassesInPackage);
    }

    public DIContainer(final Set<Class<?>> classes) {
        this.beans = createBeans(classes);
        for (Object bean : this.beans) {
            try {
                setFields(bean);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 기본 생성자로 빈을 생성한다.
    private Set<Object> createBeans(final Set<Class<?>> classes) {
        return classes.stream()
                .filter(aClass -> aClass.isAnnotationPresent(Service.class)
                        || aClass.isAnnotationPresent(Repository.class))
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
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    Object injectedBean = getBean(field.getType());
                    try {
                        field.set(bean, injectedBean);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
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
