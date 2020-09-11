package app

import app.service.SimpleSearchEngine

fun main(args: Array<String>) {
    // args example: --data data.txt --separator " "
    SimpleSearchEngine(args[1], args[3]).start()
}