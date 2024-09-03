package example;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.security.authorization.AuthorizationProxyFactory;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;

class AuthorizeReturnObjectDataHintsRegistrar implements SecurityHintsRegistrar {
	private final AuthorizationProxyFactory proxyFactory;
	private final SecurityAnnotationScanner<AuthorizeReturnObject> scanner = SecurityAnnotationScanners.requireUnique(AuthorizeReturnObject.class);
	private final Set<Class<?>> visitedClasses = new HashSet<>();

	public AuthorizeReturnObjectDataHintsRegistrar(AuthorizationProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	@Override
	public void registerHints(RuntimeHints hints, ConfigurableListableBeanFactory beanFactory) {
		for (String name : beanFactory.getBeanDefinitionNames()) {
			ResolvableType type = beanFactory.getBeanDefinition(name).getResolvableType();
			if (!RepositoryFactoryBeanSupport.class.isAssignableFrom(type.toClass())) {
				continue;
			}
			Class<?>[] generics = type.resolveGenerics();
			Class<?> entity = generics[1];
			AuthorizeReturnObject authorize = beanFactory.findAnnotationOnBean(name, AuthorizeReturnObject.class);
			if (authorize != null) {
				registerProxy(hints, entity);
				traverseType(hints, entity);
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
				registerProxy(hints, entity);
				traverseType(hints, entity);
				break;
			}
		}
	}

	private void traverseType(RuntimeHints hints, Class<?> clazz) {
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
			registerProxy(hints, returnType);
			traverseType(hints, returnType);
		}
	}

	private void registerProxy(RuntimeHints hints, Class<?> clazz) {
		Class<?> proxied = (Class<?>) this.proxyFactory.proxy(clazz);
		hints.reflection().registerType(proxied, MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_FIELDS);
	}

}
