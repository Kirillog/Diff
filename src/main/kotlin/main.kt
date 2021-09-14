fun main(args: Array<String>) {
    val command = parseArguments(args)
    val linesOriginal = readFile(command.originalFileName, command.options["ignore-files"] ?: false)
    val linesNew = readFile(command.newFileName, command.options["ignore-files"] ?: false)
    printDiff(linesOriginal, linesNew, command)
}
