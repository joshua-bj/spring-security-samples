package example;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;

public class AuthorizeReturnObjectAotProcessor implements BeanFactoryInitializationAotProcessor {

	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
		return new AuthorizationProxyFactoryAotContribution(beanFactory);
	}

	private static final class AuthorizationProxyFactoryAotContribution implements BeanFactoryInitializationAotContribution {

		private final ConfigurableListableBeanFactory beanFactory;
		private final SecurityAnnotationScanner<AuthorizeReturnObject> scanner = SecurityAnnotationScanners.requireUnique(AuthorizeReturnObject.class);
		private final Set<Class<?>> shouldProxy = new HashSet<>();
		private final Set<Class<?>> visitedClasses = new HashSet<>();

		private AuthorizationProxyFactoryAotContribution(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public void applyTo(GenerationContext context, BeanFactoryInitializationCode code) {
			AuthorizationProxyHints hints = this.beanFactory.getBean(AuthorizationProxyHints.class);
			for (String name : this.beanFactory.getBeanDefinitionNames()) {
				Class<?> clazz = this.beanFactory.getType(name, false);
				if (clazz == null) {
					continue;
				}
				traverseType(clazz);
			}
			for (Class<?> clazz : hints.types()) {
				traverseType(clazz);
			}
			for (Class<?> clazz : this.shouldProxy) {
				hints.registerType(clazz);
			}
		}

		private void traverseType(Class<?> clazz) {
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
				this.shouldProxy.add(returnType);
				traverseType(returnType);
			}
		}
	}
	
}
