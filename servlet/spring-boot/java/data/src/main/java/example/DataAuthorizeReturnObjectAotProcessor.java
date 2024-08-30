package example;

import java.lang.reflect.Method;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;

class DataAuthorizeReturnObjectAotProcessor implements BeanFactoryInitializationAotProcessor {
	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
		return new DataAuthorizeReturnObjectAotContribution(beanFactory);
	}

	private static final class DataAuthorizeReturnObjectAotContribution implements BeanFactoryInitializationAotContribution {

		private final ConfigurableListableBeanFactory beanFactory;
		private final SecurityAnnotationScanner<AuthorizeReturnObject> scanner = SecurityAnnotationScanners.requireUnique(AuthorizeReturnObject.class);

		private DataAuthorizeReturnObjectAotContribution(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;

		}

		@Override
		public void applyTo(GenerationContext context, BeanFactoryInitializationCode code) {
			AuthorizationProxyHints hints = this.beanFactory.getBean(AuthorizationProxyHints.class);
			for (String name : this.beanFactory.getBeanDefinitionNames()) {
				ResolvableType type = this.beanFactory.getBeanDefinition(name).getResolvableType();
				if (!RepositoryFactoryBeanSupport.class.isAssignableFrom(type.toClass())) {
					continue;
				}
				Class<?>[] generics = type.resolveGenerics();
				Class<?> entity = generics[1];
				AuthorizeReturnObject authorize = this.beanFactory.findAnnotationOnBean(name, AuthorizeReturnObject.class);
				if (authorize != null) {
					hints.registerType(entity);
					continue;
				}
				Class<?> repository = generics[0];
				for (Method method : repository.getDeclaredMethods()) {
					AuthorizeReturnObject returnObject = scanner.scan(method, repository);
					if (returnObject == null) {
						continue;
					}
					// optimistically assume that the entity needs wrapping if any of the
					// repository methods use @AuthorizeReturnObject
					hints.registerType(entity);

				}
			}
		}

	}
}
