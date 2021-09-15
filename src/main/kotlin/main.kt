fun main(args: Array<String>) {
    val command = parseArguments(args)
    val linesOriginal = readFile(command.originalFileName, command.options["ignore-case"] ?: false)
    val linesNew = readFile(command.newFileName, command.options["ignore-case"] ?: false)
    printDiff(linesOriginal, linesNew, command)
}
