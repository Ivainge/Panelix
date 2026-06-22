package ru.ivainge.panelix

import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.concurrent.thread

class Panelix : JavaPlugin() {
    private var springContext: ConfigurableApplicationContext? = null

    override fun onEnable() {
        extractDefaultResources()

        thread(start = true, isDaemon = true) {
            Thread.currentThread().contextClassLoader = Panelix::class.java.classLoader
            try {
                val context = AnnotationConfigServletWebServerApplicationContext()

                val staticDir = File(dataFolder, "static")
                val staticPath = if (staticDir.exists()) {
                    "file:${staticDir.absolutePath.replace("\\", "/")}"
                } else {
                    "classpath:/static/"
                }
                val staticLocations = if (staticDir.exists()) {
                    "$staticPath,classpath:/static/"
                } else {
                    "classpath:/static/"
                }

                context.environment.propertySources.addFirst(
                    MapPropertySource(
                        "panelix", mapOf(
                            "server.port" to config.getString("server.port", "8080"),
                            "server.address" to config.getString("server.address", "0.0.0.0"),
                            "spring.main.web-application-type" to "servlet",
                            "spring.web.resources.cache.period" to "0",
                            "spring.web.resources.static-locations" to staticLocations
                        )
                    )
                )
                context.register(PanelixApplication::class.java)
                context.refresh()
                springContext = context
            } catch (e: Exception) {
                logger.severe("Не удалось запустить Spring: ${e.message}")
                e.printStackTrace()
            }
        }
        logger.info("Веб-интерфейс запущен")
    }

    override fun onDisable() {
        springContext?.close()
        logger.info("Веб-интерфейс остановлен")
    }

    private fun extractDefaultResources() {
        if (!dataFolder.exists()) dataFolder.mkdirs()

        // config.yml
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            val configResource = javaClass.classLoader.getResource("config.yml")
            if (configResource != null) {
                Files.copy(configResource.openStream(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                logger.info("config.yml создан из шаблона")
            } else {
                logger.warning("Не найден ресурс config.yml в JAR")
            }
        }

        // static/ — копируем только если папка static не существует (даже пустая)
        val staticDir = File(dataFolder, "static")
        if (!staticDir.exists()) {
            try {
                copyResourcesFromClasspath("static", staticDir.toPath())
                logger.info("Папка static создана и наполнена из JAR")
            } catch (e: Exception) {
                logger.severe("Ошибка при копировании static: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun copyResourcesFromClasspath(resourceDir: String, targetDir: Path) {
        val classLoader = javaClass.classLoader
        val baseUrl = classLoader.getResource(resourceDir)
            ?: throw IllegalStateException("Папка ресурсов $resourceDir не найдена в classpath")
        val baseUri = baseUrl.toURI()
        when (baseUri.scheme) {
            "jar" -> {
                // Ресурсы внутри JAR
                val jarUri = baseUri.rawSchemeSpecificPart.substringBefore("!")
                val jarFile = java.util.jar.JarFile(File(java.net.URI(jarUri)))
                val entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name
                    if (entryName.startsWith("$resourceDir/") && !entry.isDirectory) {
                        val relativePath = entryName.substring("$resourceDir/".length)
                        val targetFile = targetDir.resolve(relativePath)
                        Files.createDirectories(targetFile.parent)
                        jarFile.getInputStream(entry).use { input ->
                            Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
                jarFile.close()
            }
            else -> {
                // Ресурсы из файловой системы (например, при разработке в IDE)
                val rootDir = File(baseUri.path)
                if (rootDir.isDirectory) {
                    rootDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            val relativePath = file.toRelativeString(rootDir)
                            val targetFile = targetDir.resolve(relativePath)
                            Files.createDirectories(targetFile.parent)
                            Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            }
        }
    }
}