package com.osmig.Jweb;

import com.osmig.Jweb.app.App;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = App.class, properties = "jweb.data.enabled=false")
class JwebApplicationTests {

	@Test
	void contextLoads() {
	}

}
