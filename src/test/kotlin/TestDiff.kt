import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import java.io.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TestDiff {
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
        private var first = Array(3) { Operation.KEEP }
        private var second = emptyArray<Operation>()

        @Test
        fun emptySecondFile() {
            calculateLCS(listOf("A", "B", "C"), emptyList(), first, second)
            assertContentEquals(
                diffOutput(convertActionsToDiffOutput(listOf("A", "B", "C"), emptyList(), first, second)), listOf(
                    "1,3d0",
                    "< A",
                    "< B",
                    "< C"
                )
            )
        }

        @Test
        fun emptyFirstFile() {
            first = second.also { second = first }
            calculateLCS(emptyList(), listOf("A", "B", "C"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToDiffOutput(emptyList(), listOf("A", "B", "C"), first, second)), listOf(
                    "0a1,3",
                    "> A",
                    "> B",
                    "> C"
                )
            )
        }

        @Test
        fun emptyBothFiles() {
            first = emptyArray()
            calculateLCS(emptyList(), emptyList(), first, second)
            assertContentEquals(
                convertActionsToDiffOutput(emptyList(), emptyList(), first, second), emptyList()
            )
        }
    }

    @Nested
    inner class GroupTestDiff {
        private val first = Array(3) { Operation.KEEP }
        private val second = Array(2) { Operation.KEEP }

        @Test
        fun deleteCenterLine() {
            calculateLCS(listOf("A", "B", "C"), listOf("A", "C"), first, second)
            assertContentEquals(
                diffOutput(
                    convertActionsToDiffOutput(
                        listOf("A", "B", "C"),
                        listOf("A", "C"),
                        first,
                        second
                    )
                ),
                listOf("2d1", "< B")
            )
        }

        @Test
        fun changeLastLine() {
            calculateLCS(listOf("A", "B", "C"), listOf("B", "D"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToDiffOutput(listOf("A", "B", "C"), listOf("B", "D"), first, second)), listOf(
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
            calculateLCS(listOf("A", "B", "C"), listOf("C", "A"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToDiffOutput(listOf("A", "B", "C"), listOf("C", "A"), first, second)), listOf(
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
            calculateLCS(listOf("A", "B", "C"), listOf("D", "E"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToDiffOutput(listOf("A", "B", "C"), listOf("D", "E"), first, second)), listOf(
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
            calculateLCS(listOf("A", "B", "C"), listOf("B", "B"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToDiffOutput(listOf("A", "B", "C"), listOf("B", "B"), first, second)), listOf(
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
        private val first = Array(3) { Operation.KEEP }
        private val second = Array(2) { Operation.KEEP }

        @Test
        fun deleteCenterLine() {
            calculateLCS(listOf("A", "B", "C"), listOf("A", "C"), first, second)
            assertContentEquals(
                diffOutput(
                    convertActionsToUnifiedDiffOutput(
                        listOf("A", "B", "C"),
                        listOf("A", "C"),
                        first,
                        second
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
            calculateLCS(listOf("A", "B", "C"), listOf("B", "D"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToUnifiedDiffOutput(listOf("A", "B", "C"), listOf("B", "D"), first, second)),
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
            calculateLCS(listOf("A", "B", "C"), listOf("C", "A"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToUnifiedDiffOutput(listOf("A", "B", "C"), listOf("C", "A"), first, second)),
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
            calculateLCS(listOf("A", "B", "C"), listOf("D", "E"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToUnifiedDiffOutput(listOf("A", "B", "C"), listOf("D", "E"), first, second)),
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
            calculateLCS(listOf("A", "B", "C"), listOf("B", "B"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToUnifiedDiffOutput(listOf("A", "B", "C"), listOf("B", "B"), first, second)),
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
        private val first = Array(3) { Operation.KEEP }
        private val second = Array(2) { Operation.KEEP }

        @Test
        fun deleteCenterLine() {
            calculateLCS(listOf("A", "B", "C"), listOf("A", "C"), first, second)
            assertContentEquals(
                diffOutput(
                    convertActionsToSideBySideOutput(
                        listOf("A", "B", "C"),
                        listOf("A", "C"),
                        first,
                        second
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
            calculateLCS(listOf("A", "B", "C"), listOf("B", "D"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToSideBySideOutput(listOf("A", "B", "C"), listOf("B", "D"), first, second)),
                listOf(
                    "A        <",
                    "B          B",
                    "C        | D"
                )
            )
        }

        @Test
        fun deleteFirstAndAddLastLine() {
            calculateLCS(listOf("A", "B", "C"), listOf("C", "A"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToSideBySideOutput(listOf("A", "B", "C"), listOf("C", "A"), first, second)),
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
            calculateLCS(listOf("A", "B", "C"), listOf("D", "E"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToSideBySideOutput(listOf("A", "B", "C"), listOf("D", "E"), first, second)),
                listOf(
                    "A        | D",
                    "B        | E",
                    "C        <"
                )
            )
        }

        @Test
        fun deleteFirstAndAddDoubleLine() {
            calculateLCS(listOf("A", "B", "C"), listOf("B", "B"), first, second)
            assertContentEquals(
                diffOutput(convertActionsToSideBySideOutput(listOf("A", "B", "C"), listOf("B", "B"), first, second)),
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
            printDiff(
                originalFileText.split("\n"), newFileText.split("\n"), Command(
                    mutableMapOf(
                        "brief" to true
                    ), testFile1.name, testFile2.name
                )
            )
            assertEquals("Files ${testFile1.name} and ${testFile2.name} differ", stream.toString().trim())
        }

        @Test
        fun equalFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(originalFileText)
            printDiff(
                originalFileText.split("\n"), originalFileText.split("\n"), Command(
                    mutableMapOf(
                        "brief" to true
                    ), testFile1.name, testFile2.name
                )
            )
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
            printDiff(
                originalFileText.split("\n"), newFileText.split("\n"), Command(
                    mutableMapOf(
                        "common-lines" to true
                    ), testFile1.name, testFile2.name
                )
            )
            assertEquals("Some text", stream.toString().trim().lines().joinToString("\n"))
        }

        @Test
        fun equalFiles() {
            testFile1.writeText(originalFileText)
            testFile2.writeText(originalFileText)
            printDiff(
                originalFileText.split("\n"), originalFileText.split("\n"), Command(
                    mutableMapOf(
                        "common-lines" to true
                    ), testFile1.name, testFile2.name
                )
            )
            assertEquals(originalFileText, stream.toString().trim().lines().joinToString("\n"))
        }
    }

    private fun diffOutput(output: List<Line>): List<String> {
        val diff = mutableListOf<String>()
        for (i in output)
            diff.add(i.text)
        return diff
    }


}