package com.example.hgh_assessment_prabesh_bhattarai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class HghAssessementApplicationTests {

	@Test
	void contextLoads() {
	}

}
