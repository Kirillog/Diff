enum class Operation {
    ADD, DELETE, KEEP, CHANGE, INFO
}

data class Line(val text: String, val command: Operation)

/**
 * computes different types of diff output for [oldFileLines] and [newFileLines] depending on enabled [command] options
 */

fun computeDiff(oldFileLines: List<String>, newFileLines: List<String>, command: Command): List<Line> {
    val oldFileActions = Array(oldFileLines.size) { Operation.KEEP }
    val newFileActions = Array(newFileLines.size) { Operation.KEEP }
    val commonLines = if (command.options["ignore-case"] == true)
        calculateLCS(
            oldFileLines.map { it.lowercase() },
            newFileLines.map { it.lowercase() },
            oldFileActions,
            newFileActions
        )
    else
        calculateLCS(oldFileLines, newFileLines, oldFileActions, newFileActions)
    val converter = Converter(oldFileLines, newFileLines, oldFileActions, newFileActions)
    return when {
        command.options["unified"] == true ->
            converter.unifiedDiffOutput(command.unifiedBorder)
        command.options["two-columns"] == true ->
            converter.twoColumnsOutput()
        command.options["common-lines"] == true ->
            commonLines.map { Line(it, Operation.KEEP) }
        else ->
            converter.diffOutput()
    }
}

/**
 * prints normal [diffOutput]
 * @param command defines whether output be brief or color
 */
fun printDiff(diffOutput: List<Line>, command: Command) {
    when {
        command.options["brief"] == true ->
            if (diffOutput.isNotEmpty()) {
                print("Files ${command.originalFileName} and ${command.newFileName} differ")
            } else {
                print("Files ${command.originalFileName} and ${command.newFileName} are identical")
            }
        command.options["color"] == true -> {
            // ANSI codes
            val reset = "\u001B[0m"
            val red = "\u001B[31m"
            val green = "\u001B[32m"
            val blue = "\u001B[34m"
            val white = "\u001B[37m"
            val purple = "\u001B[35m"
            diffOutput.forEach { i ->
                println(
                    when (i.command) {
                        Operation.ADD -> green + i.text + reset
                        Operation.DELETE -> red + i.text + reset
                        Operation.INFO -> purple + i.text + reset
                        Operation.CHANGE -> blue + i.text + reset
                        else -> white + i.text + reset
                    }
                )
            }
        }
        else ->
            diffOutput.forEach { println(it.text) }
    }
}

