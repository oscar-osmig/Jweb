package com.osmig.Jweb;

import com.osmig.Jweb.app.App;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// The @SpringBootApplication (App) lives in the com.osmig.Jweb.app child
// package, so a bare @SpringBootTest cannot find it by searching upward from
// this package. Point the test at App explicitly.
@SpringBootTest(classes = App.class)
class JwebApplicationTests {

	@Test
	void contextLoads() {
	}

}
