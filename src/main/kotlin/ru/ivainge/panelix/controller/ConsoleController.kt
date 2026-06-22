package ru.ivainge.panelix.controller

import org.bukkit.Bukkit
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ConsoleController {

    @GetMapping("/players")
    fun getPlayers(): List<String> {
        return Bukkit.getOnlinePlayers().map { it.name }
    }

    @GetMapping("/ping")
    fun ping(): String = "pong"
}