package example;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.authorization.AuthorizationProxyFactory;

public class AuthorizationProxyFactoryAotProcessor implements BeanFactoryInitializationAotProcessor {
	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
		return new AuthorizationProxyFactoryAotContribution(beanFactory);
	}

	private static final class AuthorizationProxyFactoryAotContribution implements BeanFactoryInitializationAotContribution {

		private final ConfigurableListableBeanFactory beanFactory;

		private AuthorizationProxyFactoryAotContribution(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public void applyTo(GenerationContext context, BeanFactoryInitializationCode code) {
			AuthorizationProxyFactory proxyFactory = this.beanFactory.getBean(AuthorizationProxyFactory.class);
			AuthorizationProxyHints hints = this.beanFactory.getBean(AuthorizationProxyHints.class);
			for (Class<?> type : hints.types()) {
				Class<?> proxyClass = (Class<?>) proxyFactory.proxy(type);
				context.getRuntimeHints().reflection().registerType(proxyClass,
					MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_FIELDS);
			}
		}

	}


}
