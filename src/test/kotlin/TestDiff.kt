import org.junit.jupiter.api.Test
import kotlin.test.*

internal class TestDiff {

    @Test
    fun groupTestEmptyFiles() {
        var first = Array(3) { Operation.KEEP }
        var second = emptyArray<Operation>()

        calculateLCS(listOf("A", "B", "C"), emptyList(), first, second)
        assertContentEquals(
            diffOutput(convertActionsToDiffOutput(listOf("A", "B", "C"), emptyList(), first, second)), listOf(
                "1,3d0",
                "< A",
                "< B",
                "< C"
            )
        )

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

        second = emptyArray()
        calculateLCS(emptyList(), emptyList(), first, second)
        assertContentEquals(
            convertActionsToDiffOutput(emptyList(), emptyList(), first, second), emptyList()
        )
    }

    @Test
    fun groupTestDiffOutput() {
        val first = Array(3) { Operation.KEEP }
        val second = Array(2) { Operation.KEEP }

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

    @Test
    fun groupTestUnifiedDiffOutput() {
        val first = Array(3) { Operation.KEEP }
        val second = Array(2) { Operation.KEEP }

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

    @Test
    fun groupTestSideBySideOutput() {
        val first = Array(3) { Operation.KEEP }
        val second = Array(2) { Operation.KEEP }

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

        calculateLCS(listOf("A", "B", "C"), listOf("B", "D"), first, second)
        assertContentEquals(
            diffOutput(convertActionsToSideBySideOutput(listOf("A", "B", "C"), listOf("B", "D"), first, second)),
            listOf(
                "A        <",
                "B          B",
                "C        | D"
            )
        )

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

        calculateLCS(listOf("A", "B", "C"), listOf("D", "E"), first, second)
        assertContentEquals(
            diffOutput(convertActionsToSideBySideOutput(listOf("A", "B", "C"), listOf("D", "E"), first, second)),
            listOf(
                "A        | D",
                "B        | E",
                "C        <"
            )
        )

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

    private fun diffOutput(output: List<Line>): List<String> {
        val diff = mutableListOf<String>()
        for (i in output)
            diff.add(i.text)
        return diff
    }


}