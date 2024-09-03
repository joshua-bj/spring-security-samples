package example;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.authorization.AuthorizationProxyFactory;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;

class AuthorizeReturnObjectHintsRegistrar implements SecurityHintsRegistrar {
	private final AuthorizationProxyFactory proxyFactory;
	private final SecurityAnnotationScanner<AuthorizeReturnObject> scanner = SecurityAnnotationScanners.requireUnique(AuthorizeReturnObject.class);
	private final Set<Class<?>> visitedClasses = new HashSet<>();

	AuthorizeReturnObjectHintsRegistrar(AuthorizationProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	@Override
	public void registerHints(RuntimeHints hints, ConfigurableListableBeanFactory beanFactory) {
		for (String name : beanFactory.getBeanDefinitionNames()) {
			Class<?> clazz = beanFactory.getType(name, false);
			if (clazz == null) {
				continue;
			}
			traverseType(clazz, hints);
		}
	}

	private void traverseType(Class<?> clazz, RuntimeHints hints) {
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
			traverseType(returnType, hints);
		}
	}

	private void registerProxy(RuntimeHints hints, Class<?> clazz) {
		Class<?> proxied = (Class<?>) this.proxyFactory.proxy(clazz);
		hints.reflection().registerType(proxied, MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_FIELDS);
	}

}
