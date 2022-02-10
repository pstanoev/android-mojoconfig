package com.pstanoev

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;

class ConfigureAppTask extends DefaultTask {
    @InputFile
    File configFile = new File(project.projectDir, "app.conf")
    @Input
    boolean enabled = true
    @Input
    boolean cacheEnabled = true
    @Input
    String rootNodeName = "rootProject"
    @Input
    String appNodeName = "app"
    @Input
    boolean debug = false

    @TaskAction
    def action() {
        ConfigTool.checkAndConfigure(project, configFile, enabled, cacheEnabled, rootNodeName, appNodeName, debug)
	}
	

}
