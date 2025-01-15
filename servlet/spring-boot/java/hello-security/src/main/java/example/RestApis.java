package example;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class RestApis {
    @RequestMapping("/hello")
	public Map<String, Object> hello() {
		Map<String, Object> result = new HashMap<String,Object>();
		result.put("hello", "world");
		return result;
	}

}
