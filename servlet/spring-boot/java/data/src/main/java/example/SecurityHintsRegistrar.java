package example;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public interface SecurityHintsRegistrar {
	void registerHints(RuntimeHints hints, ConfigurableListableBeanFactory beanFactory);
}
