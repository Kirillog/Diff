/**
 * converts [oldFileActions] and [newFileActions] for lines [oldFileLines] and [newFileLines] to unified, two-columns or standard diff outputs
 * call [unifiedDiffOutput], [twoColumnsOutput] or [diffOutput] to get them respectively
 */
class Converter(
    private val oldFileLines: List<String>,
    private val newFileLines: List<String>,
    private val oldFileActions: Array<Operation>,
    private val newFileActions: Array<Operation>
) {
    private val endOldFile = oldFileActions.size
    private val endNewFile = newFileActions.size
    private val changedSegments = convertActionsToSegments()
    private val diffOutput = mutableListOf<Line>()


    data class Segment(var left: Int, var right: Int) {

        fun isEmpty(): Boolean = left > right

        override fun toString(): String {
            return if (left == right)
                left.toString()
            else
                "$left,$right"
        }

        /**
         * moves [fileIterator] so [left, right] is segment of equal operations from [fileActions]
         */
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

    /**
     * stores changed segments: from [oldFileSegment] to [newFileSegment]
     */
    data class ChangeSegment(val oldFileSegment: Segment, val newFileSegment: Segment)

    /**
     * stores blocks of changes with context lines
     */
    data class Block(
        val oldFileSegment: Segment,
        val newFileSegment: Segment,
        val output: MutableList<Line> = mutableListOf()
    ) {

        fun addLine(line: Line) = output.add(line)

        fun isEmpty() = output.isEmpty()

        /**
         * returns the result of merge self with [nextBlock] and appropriate lines from [oldFileLines]
         */

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

    /**
     * adds Line([text], [operation]) to diffOutput
     */
    private fun addLine(text: String, operation: Operation) =
        diffOutput.add(Line(text, operation))

    /**
     * adds [command] of delete operation and deleted lines of [segment] from old file to diffOutput
     */
    private fun deleteFromOld(command: String, segment: Segment) =
        addToDiff(segment, oldFileLines, command, "< ", Operation.DELETE)

    /**
     * adds [command] of add operation and added lines of [segment] from new file to duffOutput
     */
    private fun addToNew(command: String, segment: Segment) =
        addToDiff(segment, newFileLines, command, "> ", Operation.ADD)

    /**
     * adds [command] of info operation and [lines] of [segment] with leading [symbol] to diffOutput
     */
    private fun addToDiff(
        segment: Segment,
        lines: List<String>,
        command: String,
        symbol: String,
        operation: Operation
    ) {
        addLine(command, Operation.INFO)
        for (i in segment.left..segment.right)
            addLine(symbol + lines[i - 1], operation)
    }

    /**
     * Form changed segments Operation.KEEP, [ Operation.ADD/Operation.DELETE/Operation.CHANGE ], Operation.KEEP between keep operations in each file
     */
    private fun convertActionsToSegments(): List<ChangeSegment> {
        val segments = mutableListOf<ChangeSegment>()
        var oldFileIterator = 0
        var newFileIterator = 0
        val oldFileSegment = Segment(1, 0)
        val newFileSegment = Segment(1, 0)
        while (oldFileIterator != endOldFile || newFileIterator != endNewFile) {
            // move segment to next segment the same format
            oldFileIterator = oldFileSegment.goNext(oldFileActions, oldFileIterator)
            newFileIterator = newFileSegment.goNext(newFileActions, newFileIterator)
            // add segments
            segments.add(ChangeSegment(oldFileSegment.copy(), newFileSegment.copy()))

            if (oldFileIterator != endOldFile)
                ++oldFileIterator
            if (newFileIterator != endNewFile)
                ++newFileIterator
        }
        return segments
    }

    /**
     * Transform [changedSegments] to diff output:
     * (segment of old file)(option)(segment of new file)
     * < deleted line
     * `---
     * `> added line,
     * where option may be "a", "d" or "c" for adding, deleting and changing operation respectively
     */
    fun diffOutput(): List<Line> {
        changedSegments.forEach { (oldFileSegment, newFileSegment) ->
            // decide what operation - 'a', 'd' or 'c' was applied
            when {
                // lines from oldFile was deleted and lines from newFile was added then change operation
                !oldFileSegment.isEmpty() && !newFileSegment.isEmpty() -> {
                    val command = oldFileSegment.toString() + "c" + newFileSegment.toString()
                    deleteFromOld(command, oldFileSegment)
                    addToNew("---", newFileSegment)
                }
                // lines from oldFile were deleted then delete operation
                !oldFileSegment.isEmpty() && newFileSegment.isEmpty() -> {
                    val command = oldFileSegment.toString() + "d" + newFileSegment.right.toString()
                    deleteFromOld(command, oldFileSegment)
                }
                // lines from newFile were added them add operation
                oldFileSegment.isEmpty() && !newFileSegment.isEmpty() -> {
                    val command = oldFileSegment.right.toString() + "a" + newFileSegment.toString()
                    addToNew(command, newFileSegment)
                }
            }
        }
        return diffOutput
    }

    /**
     * Transform [changedSegments] to blocks of changes:
     * blocks are the sum of all contextual, deletion (including changed) and addition lines.
     * @param unifiedBorder is specified number of common lines at beginning and end of block
     */
    private fun convertSegmentsToBlocks(unifiedBorder: Int): List<Block> {
        val outputBlocks = mutableListOf<Block>()
        // form blocks of changed lines Operation.KEEP, [block], Operation.KEEP between keep operations in each file
        changedSegments.forEach { (oldFileSegment, newFileSegment) ->
            // if lines from oldFile was deleted or lines from newFile was added
            if (!oldFileSegment.isEmpty() || !newFileSegment.isEmpty()) {
                val block = Block(oldFileSegment.copy(), newFileSegment.copy())
                for (i in oldFileSegment.left..oldFileSegment.right)
                    block.addLine(Line("-" + oldFileLines[i - 1], Operation.DELETE))
                for (i in newFileSegment.left..newFileSegment.right)
                    block.addLine(Line("+" + newFileLines[i - 1], Operation.ADD))
                outputBlocks.add(block)
            }

        }
        outputBlocks.add(Block(Segment(endOldFile + 2 * unifiedBorder + 1, 0), Segment(0, 0)))
        return mergeBlocks(outputBlocks, unifiedBorder)
    }

    /**
     * Merges [blocks] that have intersections
     * @param[unifiedBorder] defines whether blocks have common lines or not
     */

    private fun mergeBlocks(blocks: List<Block>, unifiedBorder: Int): List<Block> {
        val outputBlocks = mutableListOf<Block>()
        var currentBlock = Block(Segment(0, 0), Segment(0, 0))
        blocks.forEach { block ->
            currentBlock = when {
                // currentBlock is empty
                currentBlock.isEmpty() ->
                    block.copy()
                // blocks have intersections
                block.oldFileSegment.left - currentBlock.oldFileSegment.right <= 2 * unifiedBorder ->
                    currentBlock.merge(block, oldFileLines)
                // add block to output
                else -> {
                    outputBlocks.add(currentBlock)
                    block.copy()
                }
            }
        }
        return outputBlocks
    }

    /**
     * Returns list of unified diff output:
     * All output is consist of change hunks that contain the line differences in the file.
     * The unchanged, contextual lines are preceded by a space character, addition lines are preceded by a plus sign,
     * and deletion lines are preceded by a minus sign.
     * A hunk begins with range information: @@ -l,s +l,s @@, where l is number of line in file and s is length of hunk
     */
    fun unifiedDiffOutput(unifiedBorder: Int): List<Line> {
        val outputBlocks = convertSegmentsToBlocks(unifiedBorder)
        outputBlocks.forEach { block ->
            // calculate left border and right border of common lines of block
            val leftBorder = Integer.max(1, block.oldFileSegment.left - unifiedBorder)
            val rightBorder = Integer.min(endOldFile, block.oldFileSegment.right + unifiedBorder)

            val leftBorderNew = Integer.max(1, block.newFileSegment.left - unifiedBorder)
            val rightBorderNew = Integer.min(endNewFile, block.newFileSegment.right + unifiedBorder)

            val command =
                "@@ -$leftBorder,${rightBorder - leftBorder + 1} +$leftBorderNew,${rightBorderNew - leftBorderNew + 1} @@"
            addLine(command, Operation.INFO)
            // add common lines at start
            for (i in leftBorder until block.oldFileSegment.left)
                addLine(" " + oldFileLines[i - 1], Operation.KEEP)
            // add block lines
            block.output.forEach { addLine(it.text, it.command) }
            // add common lines at end
            for (i in block.oldFileSegment.right + 1..rightBorder)
                addLine(" " + oldFileLines[i - 1], Operation.KEEP)
        }
        return diffOutput
    }


    /**
     * Transform [changedSegments] to list of lines in two columns output:
     * connects common lines of old and new file in one line and
     * to each line of files that doesn't have appropriate,
     * add symbol of changes (< or >) and blank string
     */
    fun twoColumnsOutput(): List<Line> {
        val columnSize = oldFileLines.maxOf { it.length } + 10
        changedSegments.forEach { (oldFileSegment, newFileSegment) ->
            var (left1, right1) = oldFileSegment
            var (left2, right2) = newFileSegment
            // add changed lines
            while (left1 <= right1 && left2 <= right2) {
                addLine(
                    oldFileLines[left1 - 1].padEnd(columnSize - 2) + "| " + newFileLines[left2 - 1],
                    Operation.CHANGE
                )
                left1++
                left2++
            }
            // add deleted lines
            while (left1 <= right1) {
                addLine(oldFileLines[left1 - 1].padEnd(columnSize - 2) + "<", Operation.DELETE)
                left1++
            }
            // add "added" lines
            while (left2 <= right2) {
                addLine("> ".padStart(columnSize) + newFileLines[left2 - 1], Operation.ADD)
                left2++
            }
            // add common lines
            if (oldFileSegment.right != endOldFile && newFileSegment.right != endNewFile)
                addLine(
                    oldFileLines[oldFileSegment.right].padEnd(columnSize) + newFileLines[newFileSegment.right],
                    Operation.KEEP
                )
        }
        return diffOutput
    }
}