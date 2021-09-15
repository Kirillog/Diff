enum class Operation {
    ADD, DELETE, KEEP
}

enum class Move {
    UP, LEFT, UPNLEFT
}

data class Cell(var x: Int, var y: Int) {
    operator fun plusAssign(move: Move) {
        when (move) {
            Move.UP -> --x
            Move.LEFT -> --y
            Move.UPNLEFT -> {
                --x
                --y
            }
        }
    }

    override operator fun equals(other: Any?): Boolean = other is Cell && x == other.x && y == other.y
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

}

/**
 * @return largest common subsequence of two list of Strings [oldFileLines] and [newFileLines]
 *@param[oldFileActions] stores actions that were applied to each line of oldFile
 *@param[newFileActions] stores the same for newFile
 */

fun calculateLCS(
    oldFileLines: List<String>,
    newFileLines: List<String>,
    oldFileActions: Array<Operation>,
    newFileActions: Array<Operation>
): List<String> {
    val oldFileSize = oldFileLines.size
    val newFileSize = newFileLines.size

    // store length of lcs using i lines of oldFile and j lines of newFile
    val lengthOfLCS = Array(oldFileSize + 1) { IntArray(newFileSize + 1) { 0 } }
    // store previous step of calculating lcs
    val previousStep = Array(oldFileSize + 1) { Array(newFileSize + 1) { Move.UPNLEFT } }

    for (i in 1..oldFileSize)
        previousStep[i][0] = Move.UP
    for (j in 1..newFileSize)
        previousStep[0][j] = Move.LEFT
    for (i in 1..oldFileSize)
        for (j in 1..newFileSize)
            if (oldFileLines[i - 1] == newFileLines[j - 1]) {
                lengthOfLCS[i][j] = lengthOfLCS[i - 1][j - 1] + 1
                previousStep[i][j] = Move.UPNLEFT
            } else if (lengthOfLCS[i - 1][j] > lengthOfLCS[i][j - 1]) {
                lengthOfLCS[i][j] = lengthOfLCS[i - 1][j]
                previousStep[i][j] = Move.UP
            } else {
                lengthOfLCS[i][j] = lengthOfLCS[i][j - 1]
                previousStep[i][j] = Move.LEFT
            }

    // calculate lines of lcs using previous steps
    val commonLines = MutableList(lengthOfLCS[oldFileSize][newFileSize]) { "" }
    val currentCell = Cell(oldFileSize, newFileSize)
    val zeroCell = Cell(0, 0)
    while (currentCell != zeroCell) {
        val (x, y) = currentCell
        if (previousStep[x][y] == Move.UPNLEFT) {
            oldFileActions[x - 1] = Operation.KEEP
            newFileActions[y - 1] = Operation.KEEP
            commonLines[lengthOfLCS[x][y] - 1] = oldFileLines[x - 1]
        } else if (previousStep[x][y] == Move.UP) {
            oldFileActions[x - 1] = Operation.DELETE
        } else {
            newFileActions[y - 1] = Operation.ADD
        }
        currentCell += previousStep[x][y]
    }
    return commonLines
}

data class Segment(var left: Int, var right: Int) {
    fun isEmpty(): Boolean = left > right
    override fun toString(): String {
        return if (left == right)
            left.toString()
        else
            "$left,$right"
    }
}

/** compares [oldFileActions] and [newFileActions] to convert to diff output
 * @return list of strings in normal diff output format
 */

fun convertActionsToDiffOutput(
    oldFileLines: List<String>,
    newFileLines: List<String>,
    oldFileActions: Array<Operation>,
    newFileActions: Array<Operation>
): List<String> {
    var oldFileIterator = 0
    var newFileIterator = 0
    val endOldFile = oldFileActions.size
    val endNewFile = newFileActions.size
    val oldFileSegment = Segment(1, 0)
    val newFileSegment = Segment(1, 0)
    val diffOutput = mutableListOf<String>()
    while (oldFileIterator != endOldFile || newFileIterator != endNewFile) {
        while (oldFileIterator != endOldFile && oldFileActions[oldFileIterator] != Operation.KEEP)
            ++oldFileIterator
        while (newFileIterator != endNewFile && newFileActions[newFileIterator] != Operation.KEEP)
            ++newFileIterator
        oldFileSegment.right = oldFileIterator
        newFileSegment.right = newFileIterator
        // if lines from oldFile was deleted and lines from newFile was added
        if (!oldFileSegment.isEmpty() && !newFileSegment.isEmpty()) {
            val command = oldFileSegment.toString() + "c" + newFileSegment.toString()
            diffOutput.add(command)
            for (i in oldFileSegment.left..oldFileSegment.right)
                diffOutput.add("< " + oldFileLines[i - 1])
            diffOutput.add("---")
            for (i in newFileSegment.left..newFileSegment.right)
                diffOutput.add("> " + newFileLines[i - 1])
        }
        // else if lines from oldFile were deleted
        else if (!oldFileSegment.isEmpty() && newFileSegment.isEmpty()) {
            val command = oldFileSegment.toString() + "d" + newFileSegment.right.toString()
            diffOutput.add(command)
            for (i in oldFileSegment.left..oldFileSegment.right)
                diffOutput.add("< " + oldFileLines[i - 1])
        }
        // else if lines from newFile were added
        else if (oldFileSegment.isEmpty() && !newFileSegment.isEmpty()) {
            val command = oldFileSegment.right.toString() + "a" + newFileSegment.toString()
            diffOutput.add(command)
            for (i in newFileSegment.left..newFileSegment.right)
                diffOutput.add("> " + newFileLines[i - 1])
        }
        if (oldFileIterator != endOldFile)
            ++oldFileIterator
        if (newFileIterator != endNewFile)
            ++newFileIterator
        oldFileSegment.left = oldFileIterator + 1
        newFileSegment.left = newFileIterator + 1
    }
    return diffOutput
}

/**prints normal diff output for [oldFileLines] and [newFileLines]
 *@params [command] defines options of command
 */
fun printDiff(oldFileLines: List<String>, newFileLines: List<String>, command: Command) {
    val oldFileActions = Array(oldFileLines.size) { Operation.KEEP }
    val newFileActions = Array(newFileLines.size) { Operation.KEEP }
    val commonLines = calculateLCS(oldFileLines, newFileLines, oldFileActions, newFileActions)
    val diffOutput = convertActionsToDiffOutput(oldFileLines, newFileLines, oldFileActions, newFileActions)
    if (command.options["brief"] == true) {
        if (diffOutput.isNotEmpty()) {
            print("Files ${command.originalFileName} and ${command.newFileName} differ")
        } else {
            print("Files ${command.originalFileName} and ${command.newFileName} are identical")
        }
    } else if (command.options["common-lines"] == true) {
        for (i in commonLines)
            println(i)
    } else if (command.options["color"] == true) {
        // ANSI codes
        val reset = "\u001B[0m";
        val red = "\u001B[31m";
        val green = "\u001B[32m";
        val blue = "\u001B[34m";
        for (i in diffOutput)
            println(
                if (i.first() == '>')
                    green + i + reset
                else if (i.first() == '<')
                    red + i + reset
                else
                    blue + i + reset
            )
    } else {
        for (i in diffOutput)
            println(i)
    }
}

