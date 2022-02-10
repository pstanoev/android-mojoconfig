package com.pstanoev

import org.gradle.api.Plugin
import org.gradle.api.Project

class MojoConfig implements Plugin<Project> {
    void apply(Project project) {

		project.tasks.register("configureApp", ConfigureAppTask.class) {
			group = "mojoconfig"
			description = "Reads configuration file and writes AppConfig.kt"
		}

		project.afterEvaluate {
		  //project.task
		}
    }
}
