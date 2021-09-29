import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import java.io.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDiff {
    private val testFile1 = File("test1.txt")
    private val testFile2 = File("test2.txt")
    private val standardOut = System.out
    private val stream = ByteArrayOutputStream()

    @BeforeAll
    fun setUp() {
        System.setOut(PrintStream(stream))
        testFile1.createNewFile()
        testFile2.createNewFile()
    }

    @AfterAll
    fun tearDown() {
        System.setOut(standardOut)
        testFile1.delete()
        testFile2.delete()
    }


    @Nested
    inner class GroupTestEmptyFilesDiff {
        private val command = Command(mutableMapOf())

        @Test
        fun emptySecondFile() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), emptyList(), command)), listOf(
                    "1,3d0",
                    "< A",
                    "< B",
                    "< C"
                )
            )
        }

        @Test
        fun emptyFirstFile() {
            assertContentEquals(
                diffOutput(computeDiff(emptyList(), listOf("A", "B", "C"), command)), listOf(
                    "0a1,3",
                    "> A",
                    "> B",
                    "> C"
                )
            )
        }

        @Test
        fun emptyBothFiles() {
            assertContentEquals(
                computeDiff(emptyList(), emptyList(), command), emptyList()
            )
        }
    }

    @Nested
    inner class GroupTestDiff {
        private val command = Command(mutableMapOf())

        @Test
        fun deleteCenterLine() {
            assertContentEquals(
                diffOutput(
                    computeDiff(
                        listOf("A", "B", "C"),
                        listOf("A", "C"),
                        command
                    )
                ),
                listOf("2d1", "< B")
            )
        }

        @Test
        fun changeLastLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("B", "D"), command)), listOf(
                    "1d0",
                    "< A",
                    "3c2",
                    "< C",
                    "---",
                    "> D"
                )
            )
        }

        @Test
        fun deleteFirstAndAddLastLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("C", "A"), command)), listOf(
                    "1,2d0",
                    "< A",
                    "< B",
                    "3a2",
                    "> A"
                )
            )
        }

        @Test
        fun deleteAllAndAddAllLines() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("D", "E"), command)), listOf(
                    "1,3c1,2",
                    "< A",
                    "< B",
                    "< C",
                    "---",
                    "> D",
                    "> E"
                )
            )
        }

        @Test
        fun deleteFirstAndAddDoubleLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("B", "B"), command)), listOf(
                    "1d0",
                    "< A",
                    "3c2",
                    "< C",
                    "---",
                    "> B"
                )
            )
        }
    }

    @Nested
    inner class GroupTestUnifiedDiff {
        private val command = Command(mutableMapOf("unified" to true))

        @Test
        fun smallBorderOfEqualLines() {
            command.unifiedBorder = 1
            assertContentEquals(
                diffOutput(
                    computeDiff(
                        listOf("A", "B", "C"),
                        listOf("A", "B"),
                        command
                    )
                ),
                listOf(
                    "@@ -2,2 +2,1 @@",
                    " B",
                    "-C"
                )
            )
        }

        @Test
        fun deleteCenterLine() {
            assertContentEquals(
                diffOutput(
                    computeDiff(
                        listOf("A", "B", "C"),
                        listOf("A", "C"),
                        command
                    )
                ),
                listOf(
                    "@@ -1,3 +1,2 @@",
                    " A",
                    "-B",
                    " C"
                )
            )
        }

        @Test
        fun changeLastLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("B", "D"), command)),
                listOf(
                    "@@ -1,3 +1,2 @@",
                    "-A",
                    " B",
                    "-C",
                    "+D"
                )
            )
        }

        @Test
        fun deleteFirstAndAddLastLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("C", "A"), command)),
                listOf(
                    "@@ -1,3 +1,2 @@",
                    "-A",
                    "-B",
                    " C",
                    "+A"
                )
            )
        }

        @Test
        fun deleteAllAndAddAllLines() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("D", "E"), command)),
                listOf(
                    "@@ -1,3 +1,2 @@",
                    "-A",
                    "-B",
                    "-C",
                    "+D",
                    "+E"
                )
            )
        }

        @Test
        fun deleteFirstAndAddDoubleLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("B", "B"), command)),
                listOf(
                    "@@ -1,3 +1,2 @@",
                    "-A",
                    " B",
                    "-C",
                    "+B"
                )
            )
        }

    }

    @Nested
    inner class GroupTestSideBySideOutput {
        private val command = Command(mutableMapOf("two-columns" to true))

        @Test
        fun deleteCenterLine() {
            assertContentEquals(
                diffOutput(
                    computeDiff(
                        listOf("A", "B", "C"),
                        listOf("A", "C"),
                        command
                    )
                ),
                listOf(
                    "A          A",
                    "B        <",
                    "C          C"
                )
            )
        }

        @Test
        fun changeLastLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("B", "D"), command)),
                listOf(
                    "A        <",
                    "B          B",
                    "C        | D"
                )
            )
        }

        @Test
        fun deleteFirstAndAddLastLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("C", "A"), command)),
                listOf(
                    "A        <",
                    "B        <",
                    "C          C",
                    "         > A"
                )
            )
        }

        @Test
        fun deleteAllAndAddAllLines() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("D", "E"), command)),
                listOf(
                    "A        | D",
                    "B        | E",
                    "C        <"
                )
            )
        }

        @Test
        fun deleteFirstAndAddDoubleLine() {
            assertContentEquals(
                diffOutput(computeDiff(listOf("A", "B", "C"), listOf("B", "B"), command)),
                listOf(
                    "A        <",
                    "B          B",
                    "C        | B"
                )
            )
        }
    }

    @Nested
    inner class BriefOptionTest {
        private val originalFileText = """
            Some text
            Some other text
        """.trimIndent()
        private val newFileText = """
            Some text
            Some text
        """.trimIndent()

        @BeforeEach
        fun reset() {
            stream.reset()
        }

        @Test
        fun differentFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(newFileText)
            val command = Command(mutableMapOf("brief" to true), testFile1.name, testFile2.name)
            val diff = computeDiff(originalFileText.split("\n"), newFileText.split("\n"), command)
            printDiff(diff, command)
            assertEquals("Files ${testFile1.name} and ${testFile2.name} differ", stream.toString().trim())
        }

        @Test
        fun equalFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(originalFileText)
            val command = Command(mutableMapOf("brief" to true), testFile1.name, testFile2.name)
            val diff = computeDiff(originalFileText.split("\n"), originalFileText.split("\n"), command)
            printDiff(diff, command)
            assertEquals("Files ${testFile1.name} and ${testFile2.name} are identical", stream.toString().trim())
        }
    }

    @Nested
    inner class CommonLinesOptionTest {
        private val originalFileText = """
            Some text
            Some other text
        """.trimIndent()
        private val newFileText = """
            Some text
            Some text
        """.trimIndent()

        @BeforeEach
        fun reset() {
            stream.reset()
        }

        @Test
        fun differentFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(newFileText)
            val command = Command(mutableMapOf("common-lines" to true), testFile1.name, testFile2.name)
            val diff = computeDiff(originalFileText.split("\n"), newFileText.split("\n"), command)
            printDiff(diff, command)
            assertEquals("Some text", stream.toString().trim().lines().joinToString("\n"))
        }

        @Test
        fun equalFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(originalFileText)
            val command = Command(mutableMapOf("common-lines" to true), testFile1.name, testFile2.name)
            val diff = computeDiff(originalFileText.split("\n"), originalFileText.split("\n"), command)
            printDiff(diff, command)
            assertEquals(originalFileText, stream.toString().trim().lines().joinToString("\n"))
        }
    }

    @Nested
    inner class IgnoreCaseTest {
        private val originalFileText = """
            Some text
            Some other tExt
        """.trimIndent()
        private val newFileText = """
            Some text
            soMe otHer text
        """.trimIndent()

        @BeforeEach
        fun reset() {
            stream.reset()
        }

        @Test
        fun differentFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(newFileText)
            val command = Command(mutableMapOf("ignore-case" to false), testFile1.name, testFile2.name)
            val diff = computeDiff(originalFileText.split("\n"), newFileText.split("\n"), command)
            printDiff(diff, command)
            assertEquals(
                """
                2c2
                < Some other tExt
                ---
                > soMe otHer text
                """.trimIndent(), stream.toString().trim()
            )
        }

        @Test
        fun equalFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(originalFileText)
            val command = Command(mutableMapOf("ignore-case" to true), testFile1.name, testFile2.name)
            val diff = computeDiff(originalFileText.split("\n"), originalFileText.split("\n"), command)
            printDiff(diff, command)
            assertEquals("", stream.toString().trim())
        }
    }

    private fun diffOutput(output: List<Line>): List<String> {
        val diff = mutableListOf<String>()
        for (i in output)
            diff.add(i.text)
        return diff
    }


}