package example;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public final class SecurityHintsAotProcessor implements BeanFactoryInitializationAotProcessor {
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
			this.beanFactory.getBeanProvider(SecurityHintsRegistrar.class).forEach((provider) -> {
				provider.registerHints(context.getRuntimeHints(), this.beanFactory);
			});
		}

	}


}
