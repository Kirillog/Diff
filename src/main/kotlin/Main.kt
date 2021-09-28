import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val command = parseArguments(args)
    if (command.options["exit"] == true)
        exitProcess(0)
    val linesOriginal = readFile(command.originalFileName)
    val linesNew = readFile(command.newFileName)
    val diffOutput = computeDiff(linesOriginal, linesNew, command)
    printDiff(diffOutput, command)
}
