package example;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;
import org.springframework.stereotype.Component;

@Component
class AuthorizedDataClassesProvider implements AuthorizedClassesProvider {
	private final SecurityAnnotationScanner<AuthorizeReturnObject> scanner = SecurityAnnotationScanners.requireUnique(AuthorizeReturnObject.class);
	private final Set<Class<?>> visitedClasses = new HashSet<>();

	@Override
	public Set<Class<?>> getAuthorizedClasses(ConfigurableListableBeanFactory beanFactory) {
		Set<Class<?>> classes = new HashSet<>();
		for (String name : beanFactory.getBeanDefinitionNames()) {
			ResolvableType type = beanFactory.getBeanDefinition(name).getResolvableType();
			if (!RepositoryFactoryBeanSupport.class.isAssignableFrom(type.toClass())) {
				continue;
			}
			Class<?>[] generics = type.resolveGenerics();
			Class<?> entity = generics[1];
			AuthorizeReturnObject authorize = beanFactory.findAnnotationOnBean(name, AuthorizeReturnObject.class);
			if (authorize != null) {
				classes.add(entity);
				traverseType(entity, classes);
				continue;
			}
			Class<?> repository = generics[0];
			for (Method method : repository.getDeclaredMethods()) {
				AuthorizeReturnObject returnObject = this.scanner.scan(method, repository);
				if (returnObject == null) {
					continue;
				}
				// optimistically assume that the entity needs wrapping if any of the
				// repository methods use @AuthorizeReturnObject
				classes.add(entity);
				traverseType(entity, classes);
				break;
			}
		}
		return classes;
	}

	private void traverseType(Class<?> clazz, Set<Class<?>> classes) {
		if (clazz == Object.class || this.visitedClasses.contains(clazz)) {
			return;
		}
		this.visitedClasses.add(clazz);
		for (Method m : clazz.getDeclaredMethods()) {
			AuthorizeReturnObject object = this.scanner.scan(m, clazz);
			if (object == null) {
				continue;
			}
			Class<?> returnType = m.getReturnType();
			classes.add(returnType);
			traverseType(returnType, classes);
		}
	}

}
