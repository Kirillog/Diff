import java.io.File
import java.io.IOException
import java.util.*

const val FLAG = '-'

data class Command(
    var options: MutableMap<String, Boolean>,
    var originalFileName: String,
    var newFileName: String
)

/** reads program arguments from input
 *@param args array of arguments
 *@return data class Command that stores options and file names
 */

fun parseArguments(args: Array<String>): Command {
    val options = mutableMapOf(
        "brief" to false,
        "common-lines" to false,
        "ignore-case" to false,
        "color" to false,
        "unified" to false,
        "two-columns" to false
    )
    var originalFileName = ""
    var newFileName = ""
    var firstFileIsSpecified = false
    var secondFileIsSpecified = false
    try {
        args.forEach { argument ->
            if (argument.first() == FLAG) {
                val option = argument.drop(1)
                if (!options.containsKey(option))
                    throw IOException("Invalid option -- $option")
                else
                    options[option] = true
            } else if (!firstFileIsSpecified) {
                if (File(argument).exists())
                    originalFileName = argument
                else
                    throw IOException("$argument: No such file or directory")
                firstFileIsSpecified = true
            } else if (!secondFileIsSpecified) {
                if (File(argument).exists())
                    newFileName = argument
                else
                    throw IOException("$argument: No such file or directory")
                secondFileIsSpecified = true
            } else {
                throw IOException("Extra operand '$argument'")
            }
        }
        if (!secondFileIsSpecified)
            throw IOException("Missing operand after 'diff'")
    } catch (error: IOException) {
        System.err.println(error.message)
    }
    return Command(options, originalFileName, newFileName)
}

/** read strings from fileName line by line
 * @param lowerCase true when option ignore case is enabled
 * @return list of lines of file
 */
fun readFile(fileName: String, lowerCase: Boolean): List<String> {
    val lineList = mutableListOf<String>()
    File(fileName).useLines { lines ->
        lines.forEach {
            lineList.add(
                if (lowerCase)
                    it.lowercase(Locale.getDefault())
                else
                    it
            )
        }
    }
    return lineList
}