package ru.ivainge.panelix.controller

import org.bukkit.Bukkit
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
@RequestMapping("/api")
class ConsoleController {

//    @GetMapping("/players")
//    fun getPlayers(): List<String> {
//        return Bukkit.getOnlinePlayers().map { it.name }
//    }

    @PostMapping("/players")
    fun getPlayers(): List<String> {
        val list = listOf("apple", "banana", "aboba", "gay", "nigga", "ivainge", "tiger", "watermelon", "strawberry")
        return list.shuffled().take(Random.nextInt(1, list.size + 1)).sorted()
    }

    @GetMapping("/ping")
    fun ping(): String = "pong"

    @PostMapping("/ping")
    fun test(): String {
        return "yagey"
    }
}