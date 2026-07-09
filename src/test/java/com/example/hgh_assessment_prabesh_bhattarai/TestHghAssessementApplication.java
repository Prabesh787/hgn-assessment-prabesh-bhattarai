package com.example.hgh_assessment_prabesh_bhattarai;

import org.springframework.boot.SpringApplication;

public class TestHghAssessementApplication {

	public static void main(String[] args) {
		SpringApplication.from(HghAssessementApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
