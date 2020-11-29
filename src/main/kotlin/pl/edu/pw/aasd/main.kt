package pl.edu.pw.aasd

import pl.edu.pw.aasd.agent.HelloWorldAgent

fun main() {
    val agent = HelloWorldAgent()
    println(agent.isAlive)
    println("What's your name?")
    val name = readLine()
    println("Hello $name!")
}