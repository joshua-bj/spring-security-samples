package example;

import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public interface AuthorizedClassesProvider {
	Set<Class<?>> getAuthorizedClasses(ConfigurableListableBeanFactory beanFactory);
}
