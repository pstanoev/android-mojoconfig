# MojoConfig Android Configuration Generator

Generates Kotlin configuration class out of [Lightbend Config](https://github.com/lightbend/config) configuration file.

Benefits:
- type safety for configuration variables
- obfuscation of configuration variables possible (with external hardening tool)
- no runtime penalty for reading configuration file

The configuration class is generated in the Gradle configuration phase and only if the configuration file has been changed.

It is super fast.

## 1. Configuration
Create `app.config` file besides your ``build.gradle`` holding your configuration variables.

Variables per application flavor or build type are supported.

The format of the file is HOCON (Human readable JSON). 

Example:

`app.config`

```
rootProject {
  app {
    main {
      enabled = false
      output {
        dir = "app/src/main/java"
        package = "com.company.app"
        className = "AppConfig"
      }

      config {
        baseUrl = ""
        logHttpRequests = true
        refreshTime = 500
      }
    }

    debug = ${rootProject.app.main} {
      enabled = true
      output {
        dir = "app/src/debug/java"
      }

      config {
        baseUrl = "https://example.debug"
        refreshTime = 1000
      }
    }

    release = ${rootProject.app.main} {
      enabled = true
      output {
        dir = "app/src/release/java"
      }
      config {
        baseUrl = "https://example.release"
        logHttpRequests = false
      }
    }
  }
}
```

## 2. Configure Gradle

Add ``jitpack.io`` repository in your root project ``build.gradle`` and the dependency to the plugin:

``` groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    
    dependencies {
        ...
        classpath "com.github.pstanoev.android-mojoconfig:android-mojoconfig:0.2.0"
    }
    ...
}
```

In your application ``build.gradle``:

``` groovy
apply plugin: 'com.github.pstanoev.android-mojoconfig'

configureApp {
    String configFilename = "app.conf"
    configFile = new File(buildscript.sourceFile.getParentFile(), configFilename)
}

preBuild.dependsOn 'configureApp'
```

## 3. Run
Run the gradle task ``configureApp`` and it should generate the ``AppConfig.kt`` file.

```
gradlew configureApp
```

## 4. Generated file
The generated file ``AppConfig.kt`` should look like something like this:

``` kotlin
// Generated configuration file. Do not modify!
package com.company.app
...
object AppConfig {
   const val baseUrl: String = "https://example.debug"
   const val refreshTime: Int = 1000
   const val logHttpRequests: Boolean = false
}

```

## 5. Cache
The plugin detectes if the config file is changed with file that is generated ``build/lastUsedConfigFileHash.txt``.
It only runs when the config file changes.
Delete this file to make the plugin regenerate the config class even if the config file is not modified.


## 6. Configuration options

``configFile`` the configuration file to use. Default ``app.conf`` in project dir

``enabled`` master switch to disable it completely without removing the plugin. Default ``true``

``rootNodeName`` the name or nested path (example a.b.c) to the root object in the config. Default ``rootProject``

``appNodeName`` the name or nested path to the app object in the config. Default ``app``. 

``cacheEnabled`` should the plugin run only when changes to the config file are detected. Setting this to false will make the plugin run every time the app is built. Default ``true``

``debug`` enable to print debug statements.

All options with default values:

``` groovy
configureApp {
    configFile = new File(project.projectDir, "app.conf")
    enabled = true
    rootNodeName = "rootProject"
    appNodeName = "app"
    cacheEnabled = true
    debug = false
}
```

## 7. Root project extensions
To add ``rootProject.ext`` variables to the root project, add your key  value pairs in ``.ext`` of the root node:

```
rootProject {
  
  # values that will be added added to rootProject.ext
  ext {
    optionalExtVariableA = "something"
    # values from the rest of the config file can be referenced
    optionalExtVariableB = $rootProject.app.something
  }
  
  app {
    something = "yes!"
    ...
  }
}
```

