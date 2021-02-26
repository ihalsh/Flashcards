const val YES = "Yes!"
const val NO = "No!"
const val UN = "Unknown number"

fun main() {
    println(when (readLine()!!) {
        "1", "3", "4" -> NO
        "2" -> YES
        else -> UN
    })
}