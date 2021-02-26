fun main() {
    readLine()!!
            .split(" ")
            .map { it.toInt() }
            .run {
                val subList = subList(1, 3).sorted()
                println("${this[0] in subList[0]..subList[1]}")
            }
}