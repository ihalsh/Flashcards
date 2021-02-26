package flashcards

import java.io.File
import kotlin.random.Random

const val ADD = "add"
const val REMOVE = "remove"
const val ASK = "ask"
const val IMPORT = "import"
const val EXPORT = "export"
const val LOG = "log"
const val HARDEST = "hardest card"
const val RESET = "reset stats"
const val EXIT = "exit"

fun main(args: Array<String>) {
    FlashCards.run {
        importCardsFromFileOnStartup(args)
        while (true) {
            printAndLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            when (readLine()!!) {
                EXIT -> println("Bye bye").also {
                    saveCardsToFileOnExit(args)
                    return
                }
                ADD -> addCard()
                REMOVE -> removeCard()
                ASK -> practiceCards()
                IMPORT -> printAndLog("File name:").also { importCards() }
                EXPORT -> printAndLog("File name:").also { exportCards() }
                LOG -> saveLog()
                HARDEST -> showHardestCards()
                RESET -> resetMistakes()
            }
        }
    }
}

object FlashCards {

    private val cards = mutableMapOf<String, Pair<String, Int>>()
    private val log = mutableListOf<String>()

    fun importCardsFromFileOnStartup(args: Array<String>) {
        if (args.isNotEmpty()) args.forEachIndexed { index, s -> if (s == "-import") importCards(args[index + 1]) }
    }

    fun saveCardsToFileOnExit(args: Array<String>) {
        if (args.isNotEmpty()) args.forEachIndexed { index, s -> if (s == "-export") exportCards(args[index + 1]) }
    }

    fun printAndLog(s: String) {
        println(s)
        log.add(s)
    }

    fun addCard() {

        fun validate(list: Collection<String>, start: String, end: String): String? {
            val input = readLine()!!
            return if (list.contains(input)) {
                printAndLog(start + input + end)
                null
            } else return input
        }

        printAndLog("The card:")
        val term = validate(cards.keys, """The card """", """" already exists.""") ?: return
        printAndLog("The definition of the card:")
        val definition = validate(cards.values.map { it.first }, """The definition """", """" already exists.""")
                ?: return
        cards[term] = definition to 0.also { printAndLog("""The pair ("$term":"$definition") has been added.""") }
    }

    fun removeCard() {
        printAndLog("The card:")
        val input = readLine()!!
        if (cards.remove(input) != null) printAndLog("The card has been removed.") else
            printAndLog("""Can't remove "$input": there is no such card.""")
    }

    fun practiceCards() {

        fun MutableMap.MutableEntry<String, Pair<String, Int>>.recordMistake() {
            setValue(value.first to value.second + 1)
        }

        fun MutableMap.MutableEntry<String, Pair<String, Int>>.validateAnswer(answer: String): String {
            val allAnswers = cards.values.map { it.first }
            if (answer == value.first) return "Correct answer."
            if (!allAnswers.contains(answer)) return """Wrong answer. The correct one is "${value.first}"."""
                    .also { recordMistake() }
            return """Wrong answer. The correct one is "${value.first}", you've just written the definition of "${cards.filter { it.value.first == answer }.keys.first()}"."""
                    .also { recordMistake() }
        }

        printAndLog("How many times to ask?")
        val input = readLine()!!.toInt()
        repeat(input) {
            val card = cards.entries.elementAt(Random.nextInt(cards.size))
            printAndLog("""Print the definition of "${card.key}":""")
            val response = card.validateAnswer(readLine()!!)
            printAndLog(response)
        }
    }

    fun exportCards(fileName: String = readLine()!!) {
        File(fileName.toLowerCase())
                .writeText(cards.entries.joinToString("\n") { "${it.key}, ${it.value.first}, ${it.value.second}" })
        printAndLog("${cards.size} cards have been saved.")
    }

    fun importCards(fileName: String = readLine()!!) {
        val file = File(fileName)
        if (file.exists()) file.useLines { it.toList() }.run {
            forEach {
                val (key, value, numberOfMistakes) = it.split(", ")
                cards[key] = value to numberOfMistakes.toInt()
            }
            printAndLog("$size cards have been loaded.")
        } else printAndLog("File not found.")
    }

    fun saveLog() {
        printAndLog("File name:")
        File(readLine()!!).writeText(log.joinToString("\n"))
        printAndLog("The log has been saved.")
    }

    fun showHardestCards() {
        val maxNumberOfMistakes = cards.maxBy { it.value.second }?.value?.second ?: 0
        if (maxNumberOfMistakes > 0) {
            val listOfHardCards = cards.filter { it.value.second == maxNumberOfMistakes }.map { """"${it.key}"""" }
            val answer = buildString {
                append("The hardest ")
                append(if (listOfHardCards.size == 1) "card " else "cards ")
                append(if (listOfHardCards.size == 1) "is " else "are ")
                append(listOfHardCards.joinToString(separator = ", ", postfix = "."))
                append(" You have ")
                append(maxNumberOfMistakes)
                append(if (maxNumberOfMistakes == 1) " error answering " else " errors answering ")
                append(if (listOfHardCards.size == 1) "it." else "them.")
            }
            printAndLog(answer)
        } else printAndLog("There are no cards with errors.")
    }

    fun resetMistakes() {
        cards.entries.forEach { if (it.value.second > 0) it.setValue(it.value.first to 0) }
        printAndLog("Card statistics has been reset.")
    }
}