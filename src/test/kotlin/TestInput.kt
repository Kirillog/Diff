import java.io.*
import kotlin.test.*

internal class TestInput {
    private val testFile1 = File("test1.txt")
    private val testFile2 = File("test2.txt")
    private val standardErr = System.err
    private val stream = ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        System.setErr(PrintStream(stream))
    }

    @AfterTest
    fun tearDown() {
        System.setErr(standardErr)
    }
    @Test
    fun groupTestOfParsingArguments() {

        testFile1.createNewFile()
        testFile2.createNewFile()

        var command = parseArguments(
            arrayOf(
                "-brief",
                "-common-lines",
                "test1.txt",
                "test2.txt"
            )
        )
        assertTrue(
            (command.options["brief"] == true) and (command.options["common-lines"] == true)
                    and (command.options["unified"] == false) and (command.options["ignore-case"] == false)
        )

        parseArguments(
            arrayOf(
                "-color",
                "-ignore-case",
                "-unique",
                "test1.txt",
                "test2.txt"
            )
        )

        assertEquals(
            "Invalid option -- unique", stream.toString().trim()
        )

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
                    and (command.options["brief"] == false) and (command.options["ignore-case"] == false)
        )

        testFile1.delete()
        testFile2.delete()
    }

    @Test
    fun groupTestNamesOfFiles() {

        testFile1.createNewFile()
        testFile2.createNewFile()

        parseArguments(
            arrayOf(
                "notTested.txt",
                "test2.txt"
            )
        )
        assertEquals(stream.toString().trim(), "notTested.txt: No such file or directory")
        stream.reset()

        parseArguments(
            arrayOf(
                "test1.txt",
                "notTested.txt"
            )
        )
        assertEquals(stream.toString().trim(), "notTested.txt: No such file or directory")
        stream.reset()

        parseArguments(
            arrayOf(
                "test1.txt"
            )
        )
        assertEquals(stream.toString().trim(), "Missing operand after 'diff'")
        stream.reset()

        parseArguments(
            arrayOf(
                "test1.txt",
                "test2.txt",
                "test3.txt"
            )
        )
        assertEquals(stream.toString().trim(), "Extra operand 'test3.txt'")
        testFile1.delete()
        testFile2.delete()
    }
}
