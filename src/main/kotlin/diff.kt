import java.lang.Integer.max
import java.lang.Integer.min

enum class Operation {
    ADD, DELETE, KEEP, CHANGE, INFO
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

    fun goNext(fileActions: Array<Operation>, fileIterator: Int): Int {
        var it = fileIterator
        left = it + 1
        // increment fileIterator while reach not keeping action
        while (it <= fileActions.size && fileActions[it] != Operation.KEEP)
            ++it
        right = it
        return it
    }
}

data class Line(val text: String, val command: Operation)


/** compares [oldFileActions] and [newFileActions] to convert to diff output
 * @return list of strings in normal diff output format
 */

fun convertActionsToDiffOutput(
    oldFileLines: List<String>,
    newFileLines: List<String>,
    oldFileActions: Array<Operation>,
    newFileActions: Array<Operation>
): List<Line> {
    var oldFileIterator = 0
    var newFileIterator = 0
    val endOldFile = oldFileActions.size
    val endNewFile = newFileActions.size
    val oldFileSegment = Segment(1, 0)
    val newFileSegment = Segment(1, 0)
    val diffOutput = mutableListOf<Line>()
    // form segments Operation.KEEP, [segment], Operation.KEEP between keep operations in each file
    while (oldFileIterator != endOldFile || newFileIterator != endNewFile) {
        // move segment to next segment the same format
        oldFileIterator = oldFileSegment.goNext(oldFileActions, oldFileIterator)
        newFileIterator = newFileSegment.goNext(newFileActions, newFileIterator)
        // decide what operation - 'a', 'd' or 'c' was applied
        // if lines from oldFile was deleted and lines from newFile was added then change operation
        if (!oldFileSegment.isEmpty() && !newFileSegment.isEmpty()) {
            val command = oldFileSegment.toString() + "c" + newFileSegment.toString()
            diffOutput.add(Line(command, Operation.INFO))
            for (i in oldFileSegment.left..oldFileSegment.right)
                diffOutput.add(Line("< " + oldFileLines[i - 1], Operation.DELETE))
            diffOutput.add(Line("---", Operation.CHANGE))
            for (i in newFileSegment.left..newFileSegment.right)
                diffOutput.add(Line("> " + newFileLines[i - 1], Operation.ADD))
        }
        // else if lines from oldFile were deleted then delete operation
        else if (!oldFileSegment.isEmpty() && newFileSegment.isEmpty()) {
            val command = oldFileSegment.toString() + "d" + newFileSegment.right.toString()
            diffOutput.add(Line(command, Operation.INFO))
            for (i in oldFileSegment.left..oldFileSegment.right)
                diffOutput.add(Line("< " + oldFileLines[i - 1], Operation.DELETE))
        }
        // else if lines from newFile were added them add operation
        else if (oldFileSegment.isEmpty() && !newFileSegment.isEmpty()) {
            val command = oldFileSegment.right.toString() + "a" + newFileSegment.toString()
            diffOutput.add(Line(command, Operation.INFO))
            for (i in newFileSegment.left..newFileSegment.right)
                diffOutput.add(Line("> " + newFileLines[i - 1], Operation.ADD))
        }
        if (oldFileIterator != endOldFile)
            ++oldFileIterator
        if (newFileIterator != endNewFile)
            ++newFileIterator

    }
    return diffOutput
}

data class Block(
    val oldFileSegment: Segment,
    val newFileSegment: Segment,
    val output: MutableList<Line> = mutableListOf()
) {

    fun addLine(line: Line) {
        output.add(line)
    }

    fun isEmpty(): Boolean {
        return output.isEmpty()
    }

    fun merge(nextBlock: Block, oldFileLines: List<String>): Block {
        val result = Block(
            Segment(oldFileSegment.left, nextBlock.oldFileSegment.right),
            Segment(newFileSegment.left, nextBlock.newFileSegment.right)
        )
        for (i in output)
            result.addLine(i)
        for (i in oldFileSegment.right + 1 until nextBlock.oldFileSegment.left)
            result.addLine(Line(oldFileLines[i - 1], Operation.KEEP))
        for (i in nextBlock.output)
            result.addLine(i)
        return result
    }
}

const val BORDER_SIZE = 3

/** compares [oldFileActions] and [newFileActions] to convert to diff output
 * @return list of strings in unified diff output format
 */

fun convertActionsToUnifiedDiffOutput(
    oldFileLines: List<String>,
    newFileLines: List<String>,
    oldFileActions: Array<Operation>,
    newFileActions: Array<Operation>
): List<Line> {
    var oldFileIterator = 0
    var newFileIterator = 0
    val endOldFile = oldFileActions.size
    val endNewFile = newFileActions.size
    val oldFileSegment = Segment(1, 0)
    val newFileSegment = Segment(1, 0)
    val outputBlocks = mutableListOf<Block>()
    val diffOutput = mutableListOf<Line>()
    // form blocks of changed lines Operation.KEEP, [block], Operation.KEEP between keep operations in each file
    while (oldFileIterator != endOldFile || newFileIterator != endNewFile) {
        // move segment to next segment the same format
        oldFileIterator = oldFileSegment.goNext(oldFileActions, oldFileIterator)
        newFileIterator = newFileSegment.goNext(newFileActions, newFileIterator)
        // if lines from oldFile was deleted or lines from newFile was added
        if (!oldFileSegment.isEmpty() || !newFileSegment.isEmpty()) {
            val block = Block(oldFileSegment.copy(), newFileSegment.copy())
            for (i in oldFileSegment.left..oldFileSegment.right)
                block.addLine(Line("-" + oldFileLines[i - 1], Operation.DELETE))
            for (i in newFileSegment.left..newFileSegment.right)
                block.addLine(Line("+" + newFileLines[i - 1], Operation.ADD))
            outputBlocks.add(block)
        }
        if (oldFileIterator != endOldFile)
            ++oldFileIterator
        if (newFileIterator != endNewFile)
            ++newFileIterator
    }
    outputBlocks.add(Block(Segment(endOldFile + 2 * BORDER_SIZE + 1, 0), Segment(0, 0)))
    // merge blocks that have intersections
    var currentBlock = Block(Segment(0, 0), Segment(0, 0))
    for (block in outputBlocks) {
        // if currentBlock is empty
        if (currentBlock.isEmpty())
            currentBlock = block.copy()
        // else if blocks have intersections
        else if (block.oldFileSegment.left - currentBlock.oldFileSegment.right <= 2 * BORDER_SIZE)
            currentBlock = currentBlock.merge(block, oldFileLines)
        // else add block to output
        else {
            // calculate left border and right border of common lines of block
            val leftBorder = max(1, currentBlock.oldFileSegment.left - BORDER_SIZE)
            val rightBorder = min(endOldFile, currentBlock.oldFileSegment.right + BORDER_SIZE)

            val leftBorderNew = max(1, currentBlock.newFileSegment.left - BORDER_SIZE)
            val rightBorderNew = min(endNewFile, currentBlock.newFileSegment.right + BORDER_SIZE)

            val command =
                "@@ -$leftBorder,${rightBorder - leftBorder + 1} +$leftBorderNew,${rightBorderNew - leftBorderNew + 1} @@"
            diffOutput.add(Line(command, Operation.INFO))
            // add common lines at start
            for (i in leftBorder until currentBlock.oldFileSegment.left)
                diffOutput.add(Line(oldFileLines[i - 1], Operation.KEEP))
            // add block lines
            for (i in currentBlock.output)
                diffOutput.add(i)
            // add common lines at end
            for (i in currentBlock.oldFileSegment.right + 1..rightBorder)
                diffOutput.add(Line(oldFileLines[i - 1], Operation.KEEP))
            currentBlock = block.copy()
        }
    }
    return diffOutput
}

/** compares [oldFileActions] and [newFileActions] to convert to side by side output
 * @return list of strings in side by side output
 */

fun convertActionsToSideBySideOutput(
    oldFileLines: List<String>,
    newFileLines: List<String>,
    oldFileActions: Array<Operation>,
    newFileActions: Array<Operation>
): List<Line> {
    var oldFileIterator = 0
    var newFileIterator = 0
    val columnSize = oldFileLines.maxOf { it.length } + 10
    val endOldFile = oldFileActions.size
    val endNewFile = newFileActions.size
    val oldFileSegment = Segment(1, 0)
    val newFileSegment = Segment(1, 0)
    val diffOutput = mutableListOf<Line>()
    // form segments between keep operations in each file
    while (oldFileIterator != endOldFile || newFileIterator != endNewFile) {
        oldFileIterator = oldFileSegment.goNext(oldFileActions, oldFileIterator)
        newFileIterator = newFileSegment.goNext(newFileActions, newFileIterator)
        // add changed lines
        var (left1, right1) = oldFileSegment
        var (left2, right2) = newFileSegment
        while (left1 <= right1 && left2 <= right2) {
            diffOutput.add(
                Line(
                    oldFileLines[left1 - 1].padEnd(columnSize - 2) + "| " + newFileLines[left2 - 1],
                    Operation.CHANGE
                )
            )
            left1++
            left2++
        }
        // add deleted lines
        while (left1 <= right1) {
            diffOutput.add(Line(oldFileLines[left1 - 1].padEnd(columnSize - 2) + "< ", Operation.DELETE))
            left1++
        }
        // add "added" lines
        while (left2 <= right2) {
            diffOutput.add(Line("> ".padStart(columnSize) + newFileLines[left2 - 1], Operation.ADD))
            left2++
        }
        if (oldFileIterator != endOldFile && newFileIterator != endNewFile)
            diffOutput.add(
                Line(
                    oldFileLines[oldFileIterator].padEnd(columnSize) + newFileLines[newFileIterator],
                    Operation.KEEP
                )
            )

        if (oldFileIterator != endOldFile)
            ++oldFileIterator
        if (newFileIterator != endNewFile)
            ++newFileIterator

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
    val diffOutput = if (command.options["unified"] == true)
        convertActionsToUnifiedDiffOutput(oldFileLines, newFileLines, oldFileActions, newFileActions)
    else if (command.options["two-columns"] == true)
        convertActionsToSideBySideOutput(oldFileLines, newFileLines, oldFileActions, newFileActions)
    else
        convertActionsToDiffOutput(oldFileLines, newFileLines, oldFileActions, newFileActions)
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
        val white = "\u001B[37m";
        val purple = "\u001B[35m"
        for (i in diffOutput)
            println(
                when (i.command) {
                    Operation.ADD -> green + i + reset
                    Operation.DELETE -> red + i + reset
                    Operation.INFO -> purple + i + reset
                    Operation.CHANGE -> blue + i + reset
                    else -> white + i + reset
                }
            )
    } else {
        for (i in diffOutput)
            println(i)
    }
}

