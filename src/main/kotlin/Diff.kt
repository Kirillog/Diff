import java.lang.Integer.max
import java.lang.Integer.min

enum class Operation {
    ADD, DELETE, KEEP, CHANGE, INFO
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
        while (it < fileActions.size && fileActions[it] != Operation.KEEP)
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

    fun isEmpty() = output.isEmpty()

    fun merge(nextBlock: Block, oldFileLines: List<String>): Block {
        val result = Block(
            Segment(oldFileSegment.left, nextBlock.oldFileSegment.right),
            Segment(newFileSegment.left, nextBlock.newFileSegment.right)
        )
        output.forEach { result.addLine(it) }
        for (i in oldFileSegment.right + 1 until nextBlock.oldFileSegment.left)
            result.addLine(Line(" " + oldFileLines[i - 1], Operation.KEEP))
        nextBlock.output.forEach { result.addLine(it) }
        return result
    }
}

/** compares [oldFileActions] and [newFileActions] to convert to diff output
 * @return list of strings in unified diff output format
 */

fun convertActionsToUnifiedDiffOutput(
    oldFileLines: List<String>,
    newFileLines: List<String>,
    oldFileActions: Array<Operation>,
    newFileActions: Array<Operation>,
    unifiedBorder: Int = 3
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
    outputBlocks.add(Block(Segment(endOldFile + 2 * unifiedBorder + 1, 0), Segment(0, 0)))
    // merge blocks that have intersections
    var currentBlock = Block(Segment(0, 0), Segment(0, 0))
    for (block in outputBlocks) {
        // if currentBlock is empty
        if (currentBlock.isEmpty())
            currentBlock = block.copy()
        // else if blocks have intersections
        else if (block.oldFileSegment.left - currentBlock.oldFileSegment.right <= 2 * unifiedBorder)
            currentBlock = currentBlock.merge(block, oldFileLines)
        // else add block to output
        else {
            // calculate left border and right border of common lines of block
            val leftBorder = max(1, currentBlock.oldFileSegment.left - unifiedBorder)
            val rightBorder = min(endOldFile, currentBlock.oldFileSegment.right + unifiedBorder)

            val leftBorderNew = max(1, currentBlock.newFileSegment.left - unifiedBorder)
            val rightBorderNew = min(endNewFile, currentBlock.newFileSegment.right + unifiedBorder)

            val command =
                "@@ -$leftBorder,${rightBorder - leftBorder + 1} +$leftBorderNew,${rightBorderNew - leftBorderNew + 1} @@"
            diffOutput.add(Line(command, Operation.INFO))
            // add common lines at start
            for (i in leftBorder until currentBlock.oldFileSegment.left)
                diffOutput.add(Line(" " + oldFileLines[i - 1], Operation.KEEP))
            // add block lines
            currentBlock.output.forEach { diffOutput.add(it) }
            // add common lines at end
            for (i in currentBlock.oldFileSegment.right + 1..rightBorder)
                diffOutput.add(Line(" " + oldFileLines[i - 1], Operation.KEEP))
            currentBlock = block.copy()
        }
    }
    return diffOutput
}

/** compares [oldFileActions] and [newFileActions] to convert to side by side output
 *
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
            diffOutput.add(Line(oldFileLines[left1 - 1].padEnd(columnSize - 2) + "<", Operation.DELETE))
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

/**computes different types of diff output for [oldFileLines] and [newFileLines] depending on enabled [command] options
 *
 */

fun computeDiff(oldFileLines: List<String>, newFileLines: List<String>, command: Command): List<Line> {
    val oldFileActions = Array(oldFileLines.size) { Operation.KEEP }
    val newFileActions = Array(newFileLines.size) { Operation.KEEP }
    val commonLines = calculateLCS(oldFileLines, newFileLines, oldFileActions, newFileActions)
    return when {
        command.options["unified"] == true ->
            convertActionsToUnifiedDiffOutput(
                oldFileLines,
                newFileLines,
                oldFileActions,
                newFileActions,
                command.unifiedBorder
            )
        command.options["two-columns"] == true ->
            convertActionsToSideBySideOutput(oldFileLines, newFileLines, oldFileActions, newFileActions)
        command.options["common-lines"] == true ->
            commonLines.map { Line(it, Operation.KEEP) }
        else ->
            convertActionsToDiffOutput(oldFileLines, newFileLines, oldFileActions, newFileActions)
    }
}

/**prints normal [diffOutput]
 *@params [command] defines whether output be brief or color
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

