package ru.ivainge.panelix.controller

import org.bukkit.Bukkit
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
@RequestMapping("/api")
class ApiController {

    @GetMapping("/players")
    fun getPlayers(): List<String> {
        return Bukkit.getOnlinePlayers().map { it.name }
    }

    @GetMapping("/testPlayers")
    fun getTestPlayers(): List<String> {
        val list = listOf("apple", "banana", "aboba", "someone", "biba", "boba", "ivainge", "tiger", "watermelon", "strawberry")
        return list.shuffled().take(Random.nextInt(1, list.size + 1)).sorted()
    }

    @GetMapping("/ping")
    fun ping(): String = "get pong"

    @PostMapping("/ping")
    fun test(): String {
        return "post pong"
    }
}