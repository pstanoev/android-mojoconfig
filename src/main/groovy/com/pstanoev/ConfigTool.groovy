package com.pstanoev

import org.gradle.api.Project
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import java.security.MessageDigest

class ConfigTool {
	
	public static void checkAndConfigure(Project project, File configFile, boolean enabled, boolean cacheEnabled, 
    	String rootNodeName, String appNodeName, boolean debug) {
	
		if (!enabled) {
    	  println("MojoConfig: is disabled, won't configure App")
    	  return
    	}

    	println("MojoConfig: Configuring App")
        
        // construct path to config file
	    println "\nMojoConfig: Configuring App with config file: " + configFile.getAbsolutePath()
	    if (!configFile.exists()) {
	        throw new IllegalArgumentException("MojoConfig: Confguration file not found: " + configFile.getAbsolutePath())
	    }
	
	    // parse config file
	    Config rootConfig = ConfigFactory.parseFile(configFile).resolve()
	    Config tConfig = rootConfig.getConfig(rootNodeName)
	
	    // set global config properties
	    Project rootProject = project.rootProject
	    configureGlobalProperties(configFile, tConfig, rootProject)
	
	    // flag to disable the configuration cache check ("is config file modified since last build"), useful for debugging
	
	    // check if configuration file has changed since last build
	    File lastUsedConfigFileHashCache = null
	    String configFileHash = null
	    if (cacheEnabled) {
	        // sha1 of the config file and build file
	        File[] filesToCheckForModification = new File[1]
	        filesToCheckForModification[0] = configFile
	        //filesToCheckForModification[1] = buildscript.sourceFile
	        //filesToCheckForModification[2] = new File(buildscript.sourceFile.getParentFile(), "config-tool-kotlin-generator.gradle")
	        configFileHash = calcSha1forFiles(filesToCheckForModification)
	        // println "Config file and build files hash:      " + configFileHash
	
	        // sha1 of the config file that was used in last build
	        lastUsedConfigFileHashCache = new File(project.buildDir, "lastUsedConfigFileHash.txt")
	        String lastUsedConfigFileHash = lastUsedConfigFileHashCache.exists() ? lastUsedConfigFileHashCache.getText("UTF-8") : null
	
	        if (lastUsedConfigFileHash != null && lastUsedConfigFileHash.equals(configFileHash)) {
	            // config file still the same as it was from last build, abort
	            println "MojoConfig: Configuration classes should be up to date, will not be re-generated"
	            return
	        } else {
	            // config file has changed
	            println "MojoConfig: Configuration file " + configFile.getName() + " has changed, will regenerate configuration classes"
	        }
	    }
	
	    // generate classes out of the config file
	    Config appConfig = appNodeName != null ? tConfig.getConfig(appNodeName) : tConfig
	    ConfigToolKotlin.configureAppKotlin(appConfig, configFile, project.rootDir, debug)
	
	    // store the hash of the config file as last step
	    if (cacheEnabled) {
	        if (!lastUsedConfigFileHashCache.getParentFile().exists()) {
	            lastUsedConfigFileHashCache.getParentFile().mkdirs()
	        }
	        lastUsedConfigFileHashCache.text = configFileHash
	    }
	
	    println "MojoConfig: Finished configuring\n"
	    
    }
    
    private static String calcSha1forFiles(File[] files) {
	    MessageDigest md = MessageDigest.getInstance("SHA-1")
	    for (File file : files) {
	        file.eachByte 4096, { bytes, size ->
	            md.update(bytes, 0, size)
	        }
	    }
	    return md.digest().encodeHex().toString()
	}
    
    
    private static void configureGlobalProperties(File configFile, Config tConfig, Project rootProject) {
	    if (!tConfig.hasPath("ext")) {
	        println "MojoConfig: No root project extensions from config (rootProject.ext)"
	        return
	    }
	    println "MojoConfig: Configuring root project extension with config file: " + configFile.getName()
	    Config extConfig = tConfig.getConfig("ext")
	
	    for (Map.Entry<String, ConfigValue> e : extConfig.entrySet()) {
	        def key = e.key
	        def value = e.value
	        def valueUnwrapped = value.unwrapped()
	        println key + " = " + value + " : " + valueUnwrapped
	        rootProject.ext.set(key, valueUnwrapped)
	    }
	}

}