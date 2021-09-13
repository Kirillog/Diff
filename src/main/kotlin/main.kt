fun main(args: Array<String>) {
    val command = parseArguments(args)
    val linesListOriginal = readFile(command.originalFileName, command.options["ignore-files"] ?: false)
    val linesListNew = readFile(command.newFileName, command.options["ignore-files"] ?: false)
    lcs(linesListOriginal, linesListNew)
}
