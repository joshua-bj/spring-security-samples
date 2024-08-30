package example;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class AuthorizationProxyHints {
	private Set<Class<?>> classesToProxy = new HashSet<>();

	public void registerType(Class<?> clazz) {
		this.classesToProxy.add(clazz);
	}

	public Set<Class<?>> types() {
		return Collections.unmodifiableSet(this.classesToProxy);
	}
}
