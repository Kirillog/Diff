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