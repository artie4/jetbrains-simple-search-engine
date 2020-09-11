package app.service

import java.io.BufferedReader
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

class SimpleSearchEngine(
    fileName: String,
    private val separator: String
) {

    private var enabled = true
    private val scanner = Scanner(System.`in`)
    private val data: MutableList<String> = mutableListOf()
    private val indexes: MutableList<MutableMap<String, MutableList<Int>>> = mutableListOf()

    init {
        val numberOfIndexes = Files.newBufferedReader(Paths.get(fileName))
            .lines().mapToInt { it.split(separator).size }.max().orElse(0)

        if (numberOfIndexes > 0) {
            for (i in 0 until numberOfIndexes) {
                indexes.add(mutableMapOf())
            }
            val reader = BufferedReader(FileReader(fileName))
            var lineNumer = 0
            reader.use {
                while (it.ready()) {
                    val curLine = it.readLine()
                    data.add(curLine)
                    val lineTokens = curLine.split(separator)
                    for (i in indexes.indices) {
                        if (i <= lineTokens.lastIndex) {
                            indexes[i].merge(lineTokens[i].toLowerCase(), mutableListOf(lineNumer))
                            { oldValue, value -> oldValue.addAll(value); oldValue }
                        }
                    }
                    lineNumer++
                }
            }
        }
    }

    fun start() {
        while (enabled) {
            menu()
        }
    }

    private fun menu() {
        val menuActions = arrayOf("1. Find a record", "2. Print all records", "0. Exit")
        println("=== Menu ===")
        menuActions.forEach { println(it) }
        val number = scanner.nextLine().toInt()
        when (number) {
            1 -> findByCriteria()
            2 -> printData()
            0 -> exit()
            else -> println("Incorrect option! Try again.")
        }
    }

    private fun findByCriteria() {
        println("Select a matching strategy: ALL, ANY, NONE")
        val strategy = scanner.nextLine()!!.trimEnd().toLowerCase()
        println("Enter criteria to search all suitable records.")
        val criterions = scanner.nextLine()!!.trimEnd().toLowerCase().split(" ")
        val indexesByEachCrit: List<List<Int>> = criterions.stream()
            .map { crit -> indexes.filter { idx -> idx[crit] != null }.flatMap { idx -> idx[crit]!!.toList() } }
            .collect(Collectors.toList())

        when (strategy) {
            "any" -> {
                if (indexesByEachCrit.isEmpty()) {
                    println("No matching records found.")
                } else {
                    val distinctIndexes = indexesByEachCrit.flatMapTo(mutableSetOf<Int>(), { it.toSet() })
                    println("${distinctIndexes.size} records found:")
                    distinctIndexes.forEach { println(data[it]) }
                }
            }
            "all" -> {
                val joinIdx = indexesByEachCrit.first()
                    .filter { idx -> indexesByEachCrit.stream().allMatch({ it.contains(idx) }) }.toList()
                if (joinIdx.isNullOrEmpty()) {
                    println("No matching records found.")
                } else {
                    println("${joinIdx.size} records found:")
                    joinIdx.forEach { println(data[it]) }
                }
            }
            "none" -> {
                val prohibitedIndexes = indexesByEachCrit.flatMapTo(mutableSetOf<Int>(), { it.toSet() })
                val availableIndexes = data.indices.toList().filterNot { idx -> prohibitedIndexes.contains(idx) }
                if (availableIndexes.isEmpty()) {
                    println("No matching records found.")
                } else {
                    println("${availableIndexes.size} records found:")
                    availableIndexes.forEach { println(data[it]) }
                }
            }
        }
    }


    private fun printData() {
        println("=== List of records ===")
        data.forEach { println(it) }
    }

    private fun exit() {
        enabled = false
        println("Bye!")
    }

}