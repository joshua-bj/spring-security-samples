package example;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;
import org.springframework.stereotype.Component;

@Component
class AuthorizedSpringClassesProvider implements AuthorizedClassesProvider {
	private final SecurityAnnotationScanner<AuthorizeReturnObject> scanner = SecurityAnnotationScanners.requireUnique(AuthorizeReturnObject.class);
	private final Set<Class<?>> visitedClasses = new HashSet<>();

	@Override
	public Set<Class<?>> getAuthorizedClasses(ConfigurableListableBeanFactory beanFactory) {
		Set<Class<?>> classes = new HashSet<>();
		for (String name : beanFactory.getBeanDefinitionNames()) {
			Class<?> clazz = beanFactory.getType(name, false);
			if (clazz == null) {
				continue;
			}
			traverseType(clazz, classes);
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
