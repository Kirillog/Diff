import org.junit.jupiter.api.Test
import kotlin.test.*

class TestLCS {
    private fun <T> equalsOr(array1: Array<T>, array2: Array<T>, array3: Array<T>): Boolean {
        return array1.contentEquals(array2) || array2.contentEquals(array3)
    }

    @Test
    fun groupTestEmptyFiles() {
        var first = Array(3) { Operation.KEEP }
        var second = emptyArray<Operation>()
        assertContentEquals(calculateLCS(listOf("A", "B", "C"), emptyList(), first, second), emptyList())
        assertContentEquals(first, Array(3) { Operation.DELETE })
        assertContentEquals(second, emptyArray())

        first = second.also { second = first }
        assertContentEquals(calculateLCS(emptyList(), listOf("A", "B", "C"), first, second), emptyList())
        assertContentEquals(first, emptyArray())
        assertContentEquals(second, Array(3) { Operation.ADD })

        second = emptyArray()
        assertContentEquals(calculateLCS(emptyList(), emptyList(), first, second), emptyList())
        assertContentEquals(first, emptyArray())
        assertContentEquals(second, emptyArray())

    }

    @Test
    fun groupTestSmallLCS() {
        val first = Array(3) { Operation.KEEP }
        val second = Array(2) { Operation.KEEP }
        assertContentEquals(calculateLCS(listOf("A", "B", "C"), listOf("A", "C"), first, second), listOf("A", "C"))
        assertContentEquals(first, arrayOf(Operation.KEEP, Operation.DELETE, Operation.KEEP))
        assertContentEquals(second, arrayOf(Operation.KEEP, Operation.KEEP))

        assertContentEquals(calculateLCS(listOf("A", "B", "C"), listOf("B", "D"), first, second), listOf("B"))
        assertContentEquals(first, arrayOf(Operation.DELETE, Operation.KEEP, Operation.DELETE))
        assertContentEquals(second, arrayOf(Operation.KEEP, Operation.ADD))

        assertContentEquals(calculateLCS(listOf("A", "B", "C"), listOf("C", "A"), first, second), listOf("C"))
        assertContentEquals(first, arrayOf(Operation.DELETE, Operation.DELETE, Operation.KEEP))
        assertContentEquals(second, arrayOf(Operation.KEEP, Operation.ADD))

        assertContentEquals(calculateLCS(listOf("A", "B", "C"), listOf("D", "E"), first, second), emptyList())
        assertContentEquals(first, arrayOf(Operation.DELETE, Operation.DELETE, Operation.DELETE))
        assertContentEquals(second, arrayOf(Operation.ADD, Operation.ADD))

        assertContentEquals(calculateLCS(listOf("A", "B", "C"), listOf("B", "B"), first, second), listOf("B"))
        assertContentEquals(first, arrayOf(Operation.DELETE, Operation.KEEP, Operation.DELETE))
        assertTrue(equalsOr(second, arrayOf(Operation.KEEP, Operation.ADD), arrayOf(Operation.ADD, Operation.KEEP)))
    }

    @Test
    fun groupTestLCS() {
        val first = Array(7) { Operation.KEEP }
        val second = Array(7) { Operation.KEEP }

        assertContentEquals(
            calculateLCS(
                listOf("B", "B", "C", "D", "D", "E", "A"),
                listOf("B", "D", "A", "E", "D", "E", "E"),
                first,
                second
            ), listOf("B", "D", "D", "E")
        )
        assertContentEquals(
            first,
            arrayOf(
                Operation.DELETE,
                Operation.KEEP,
                Operation.DELETE,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.DELETE
            )
        )
        assertContentEquals(
            second,
            arrayOf(
                Operation.KEEP,
                Operation.KEEP,
                Operation.ADD,
                Operation.ADD,
                Operation.KEEP,
                Operation.KEEP,
                Operation.ADD
            )
        )

        assertContentEquals(
            calculateLCS(
                listOf("C", "D", "A", "C", "B", "D", "C"),
                listOf("C", "C", "D", "C", "B", "D", "C"),
                first,
                second
            ), listOf("C", "D", "C", "B", "D", "C")
        )
        assertContentEquals(
            first,
            arrayOf(
                Operation.KEEP,
                Operation.KEEP,
                Operation.DELETE,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP
            )
        )
        assertContentEquals(
            second,
            arrayOf(
                Operation.ADD,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP
            )
        )

        assertContentEquals(
            calculateLCS(
                listOf("E", "E", "B", "B", "E", "B", "D"),
                listOf("A", "B", "C", "A", "C", "D", "A"),
                first,
                second
            ), listOf("B", "D")
        )
        assertContentEquals(
            first,
            arrayOf(
                Operation.DELETE,
                Operation.DELETE,
                Operation.DELETE,
                Operation.DELETE,
                Operation.DELETE,
                Operation.KEEP,
                Operation.KEEP
            )
        )
        assertContentEquals(
            second,
            arrayOf(
                Operation.ADD,
                Operation.KEEP,
                Operation.ADD,
                Operation.ADD,
                Operation.ADD,
                Operation.KEEP,
                Operation.ADD
            )
        )

    }
}