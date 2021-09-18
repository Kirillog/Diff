import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import java.io.*
import java.util.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TestInput {
    private val testFile1 = File("test1.txt")
    private val testFile2 = File("test2.txt")
    private val standardErr = System.err
    private val stream = ByteArrayOutputStream()

    @BeforeAll
    fun setUp() {
        System.setErr(PrintStream(stream))
        testFile1.createNewFile()
        testFile2.createNewFile()
    }

    @AfterAll
    fun tearDown() {
        System.setErr(standardErr)
        testFile1.delete()
        testFile2.delete()
    }

    @Test
    fun testOfParsingArguments() {
        var command = parseArguments(
            arrayOf(
                "-brief",
                "-common-lines",
                "-two-columns",
                "-color",
                "-ignore-case",
                "-unified",
                "test1.txt",
                "test2.txt"
            )
        )
        assertTrue(
            (command.options["brief"] == true) and (command.options["common-lines"] == true)
                    and (command.options["unified"] == true) and (command.options["ignore-case"] == true)
                    and (command.options["two-columns"] == true) and (command.options["color"] == true)
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

        command = parseArguments(
            arrayOf(
                "-ignore-case",
                "-two-columns",
                "test1.txt",
                "test2.txt"
            )
        )
        assertTrue(
            (command.options["ignore-case"] == true) and (command.options["two-columns"] == true)
                    and (command.options["unified"] == false) and (command.options["common-lines"] == false)
        )

    }

    @Test
    fun testInvalidOption() {
        stream.reset()

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
    }

    @Test
    fun testReadFiles() {
        val text = """
            Some TexT has been Added to This File
            Nobody knows what time it takes to write tests
            excluding me.
        """.trimIndent()
        testFile1.writeText(text)
        assertContentEquals(text.split("\n"), readFile(testFile1.name, false))
        assertContentEquals(text.lowercase(Locale.getDefault()).split("\n"), readFile(testFile1.name, true))
    }

    @Nested
    inner class GroupTestNamesOfFiles {
        @BeforeEach
        fun reset() {
            stream.reset()
        }

        @Test
        fun wrongFirstFile() {
            parseArguments(
                arrayOf(
                    "notTested.txt",
                    "test2.txt"
                )
            )
            assertEquals("notTested.txt: No such file or directory", stream.toString().trim())
        }

        @Test
        fun wrongSecondFile() {
            parseArguments(
                arrayOf(
                    "test1.txt",
                    "notTested.txt"
                )
            )
            assertEquals("notTested.txt: No such file or directory", stream.toString().trim())
        }

        @Test
        fun missingSecondFile() {
            parseArguments(
                arrayOf(
                    "test1.txt"
                )
            )
            assertEquals("Missing operand after 'diff'", stream.toString().trim())
        }

        @Test
        fun extraOperand() {
            parseArguments(
                arrayOf(
                    "test1.txt",
                    "test2.txt",
                    "test3.txt"
                )
            )
            assertEquals("Extra operand 'test3.txt'", stream.toString().trim())
        }
    }
}
