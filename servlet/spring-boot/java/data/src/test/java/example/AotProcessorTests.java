package example;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class AotProcessorTests {
	private final RuntimeHints hints = new RuntimeHints();

	private final GenerationContext context = mock(GenerationContext.class);
	private final BeanFactoryInitializationCode code = mock(BeanFactoryInitializationCode.class);

	private final AuthorizationProxyFactoryAotProcessor proxy = new AuthorizationProxyFactoryAotProcessor();

	@Autowired
	ConfigurableListableBeanFactory beanFactory;

	@Test
	void findsAllNeededClassesToProxy() {
		given(this.context.getRuntimeHints()).willReturn(this.hints);
		this.proxy.processAheadOfTime(beanFactory).applyTo(this.context, this.code);
		Collection<String> canonicalNames = new ArrayList<>();
		this.hints.reflection().typeHints().forEach((hint) -> canonicalNames.add(hint.getType().getCanonicalName()));
		assertThat(canonicalNames).contains("example.Message$$SpringCGLIB$$0", "example.User$$SpringCGLIB$$0");
	}

}
