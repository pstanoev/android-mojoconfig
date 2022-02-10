package com.pstanoev

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction


class ConfigureAppTask extends DefaultTask {
    File configFile = new File(project.projectDir, "app.conf")
    boolean enabled = true
    boolean cacheEnabled = true
    String rootNodeName = "rootProject"
    String appNodeName = "app"
    boolean debug = false

    @TaskAction
    def action() {
        ConfigTool.checkAndConfigure(project, configFile, enabled, cacheEnabled, rootNodeName, appNodeName, debug)
	}
	

}
