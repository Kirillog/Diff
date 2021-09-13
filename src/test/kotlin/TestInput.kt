import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.IOException
import kotlin.test.*

internal class TestInput {
    private val testFile1 = File("test1.txt")
    private val testFile2 = File("test2.txt")


    @Test
    fun groupTestOfParcingArguments() {

        testFile1.createNewFile()
        testFile2.createNewFile()

        var command = parseArguments(
            arrayOf(
                "-brief",
                "-matches",
                "test1.txt",
                "test2.txt"
            )
        )
        assertTrue(
            (command.options["brief"] == true) and (command.options["matches"] == true)
                    and (command.options["unified"] == false) and (command.options["ignore-case"] == false)
        )

        assertThrows<IOException> {
            parseArguments(
                arrayOf(
                    "-color",
                    "-ignore-case",
                    "-unique",
                    "test1.txt",
                    "test2.txt"
                )
            )
        }

        command = parseArguments(
            arrayOf(
                "-color",
                "-unified",
                "test1.txt",
                "test2.txt"
            )
        )
        assertTrue(
            (command.options["color"] == true) and (command.options["unified"] == true)
                    and (command.options["brief"] == false) and (command.options["matches"] == false)
        )

        testFile1.delete()
        testFile2.delete()
    }

    @Test
    fun groupTestNamesOfFiles() {

        testFile1.createNewFile()
        testFile2.createNewFile()

        assertThrows<IOException> {
            parseArguments(
                arrayOf(
                    "notTested.txt",
                    "test2.txt"
                )
            )
        }
        assertThrows<IOException> {
            parseArguments(
                arrayOf(
                    "test1.txt",
                    "notTested.txt"
                )
            )
        }

        assertThrows<IOException> {
            parseArguments(
                arrayOf(
                    "test1.txt"
                )
            )
        }

        assertThrows<IOException> {
            parseArguments(
                arrayOf(
                    "test1.txt",
                    "test2.txt",
                    "test3.txt"
                )
            )
        }

        testFile1.delete()
        testFile2.delete()
    }
}
