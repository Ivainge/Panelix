plugins {
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "ru.ivainge.panelix"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))
    // Spring Boot
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.5"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        archiveBaseName.set("Panelix")
        archiveClassifier.set("")
//        relocate("org.apache.tomcat", "ru.ivainge.panelix.libs.tomcat")
//        relocate("io.netty", "ru.ivainge.panelix.libs.netty")
//        relocate("com.fasterxml", "ru.ivainge.panelix.libs.jackson")
        mergeServiceFiles()
        append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
//        append("META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports") // если вдруг понадобится
    }

    build {
        dependsOn(shadowJar)
        finalizedBy("deployPlugin")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    register<Copy>("deployPlugin") {
        dependsOn(shadowJar)
        from(layout.buildDirectory.dir("libs"))
        include("${project.name}-${project.version}.jar")
        into("C:/Users/ivain/Рабочий стол/dev-mine-server/plugins")
    }
}