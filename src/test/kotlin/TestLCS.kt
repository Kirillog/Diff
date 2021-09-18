import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.*

class TestLCS {
    private fun <T> equalsOr(array1: Array<T>, array2: Array<T>, array3: Array<T>): Boolean {
        return array3.contentEquals(array1) || array3.contentEquals(array2)
    }

    @Nested
    inner class GroupTestEmptyFiles {
        private var first = Array(3) { Operation.KEEP }
        private var second = emptyArray<Operation>()

        @Test
        fun emptySecondFile() {
            assertContentEquals(emptyList(), calculateLCS(listOf("A", "B", "C"), emptyList(), first, second))
            assertContentEquals(Array(3) { Operation.DELETE }, first)
            assertContentEquals(emptyArray(), second)

        }

        @Test
        fun emptyFirstFile() {
            first = second.also { second = first }
            assertContentEquals(emptyList(), calculateLCS(emptyList(), listOf("A", "B", "C"), first, second))
            assertContentEquals(emptyArray(), first)
            assertContentEquals(Array(3) { Operation.ADD }, second)
        }

        @Test
        fun emptyBothFiles() {
            first = emptyArray()
            assertContentEquals(emptyList(), calculateLCS(emptyList(), emptyList(), first, second))
            assertContentEquals(emptyArray(), first)
            assertContentEquals(emptyArray(), second)
        }

    }

    @Nested
    inner class GroupTestSmallLCS {
        private val first = Array(3) { Operation.KEEP }
        private val second = Array(2) { Operation.KEEP }

        @Test
        fun deleteCenterLine() {
            assertContentEquals(listOf("A", "C"), calculateLCS(listOf("A", "B", "C"), listOf("A", "C"), first, second))
            assertContentEquals(arrayOf(Operation.KEEP, Operation.DELETE, Operation.KEEP), first)
            assertContentEquals(arrayOf(Operation.KEEP, Operation.KEEP), second)
        }

        @Test
        fun changeLastLine() {
            assertContentEquals(listOf("B"), calculateLCS(listOf("A", "B", "C"), listOf("B", "D"), first, second))
            assertContentEquals(arrayOf(Operation.DELETE, Operation.KEEP, Operation.DELETE), first)
            assertContentEquals(arrayOf(Operation.KEEP, Operation.ADD), second)
        }

        @Test
        fun deleteFirstAndAddLastLine() {
            assertContentEquals(listOf("C"), calculateLCS(listOf("A", "B", "C"), listOf("C", "A"), first, second))
            assertContentEquals(arrayOf(Operation.DELETE, Operation.DELETE, Operation.KEEP), first)
            assertContentEquals(arrayOf(Operation.KEEP, Operation.ADD), second)
        }

        @Test
        fun deleteAllAndAddAllLines() {
            assertContentEquals(emptyList(), calculateLCS(listOf("A", "B", "C"), listOf("D", "E"), first, second))
            assertContentEquals(arrayOf(Operation.DELETE, Operation.DELETE, Operation.DELETE), first)
            assertContentEquals(arrayOf(Operation.ADD, Operation.ADD), second)
        }

        @Test
        fun deleteFirstAndAddDoubleLine() {
            assertContentEquals(listOf("B"), calculateLCS(listOf("A", "B", "C"), listOf("B", "B"), first, second))
            assertContentEquals(arrayOf(Operation.DELETE, Operation.KEEP, Operation.DELETE), first)
            assertTrue(equalsOr(arrayOf(Operation.KEEP, Operation.ADD), arrayOf(Operation.ADD, Operation.KEEP), second))
        }
    }

    @Test
    fun groupTestLCS() {
        val first = Array(7) { Operation.KEEP }
        val second = Array(7) { Operation.KEEP }

        assertContentEquals(
            listOf("B", "D", "D", "E"),
            calculateLCS(
                listOf("B", "B", "C", "D", "D", "E", "A"),
                listOf("B", "D", "A", "E", "D", "E", "E"),
                first,
                second
            )
        )
        assertContentEquals(
            arrayOf(
                Operation.DELETE,
                Operation.KEEP,
                Operation.DELETE,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.DELETE
            ), first
        )
        assertContentEquals(
            arrayOf(
                Operation.KEEP,
                Operation.KEEP,
                Operation.ADD,
                Operation.ADD,
                Operation.KEEP,
                Operation.KEEP,
                Operation.ADD
            ), second
        )

        assertContentEquals(
            listOf("C", "D", "C", "B", "D", "C"),
            calculateLCS(
                listOf("C", "D", "A", "C", "B", "D", "C"),
                listOf("C", "C", "D", "C", "B", "D", "C"),
                first,
                second
            )
        )
        assertContentEquals(
            arrayOf(
                Operation.KEEP,
                Operation.KEEP,
                Operation.DELETE,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP
            ), first
        )
        assertContentEquals(
            arrayOf(
                Operation.ADD,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP,
                Operation.KEEP
            ), second
        )

        assertContentEquals(
            listOf("B", "D"),
            calculateLCS(
                listOf("E", "E", "B", "B", "E", "B", "D"),
                listOf("A", "B", "C", "A", "C", "D", "A"),
                first,
                second
            )
        )
        assertContentEquals(
            arrayOf(
                Operation.DELETE,
                Operation.DELETE,
                Operation.DELETE,
                Operation.DELETE,
                Operation.DELETE,
                Operation.KEEP,
                Operation.KEEP
            ), first
        )
        assertContentEquals(
            arrayOf(
                Operation.ADD,
                Operation.KEEP,
                Operation.ADD,
                Operation.ADD,
                Operation.ADD,
                Operation.KEEP,
                Operation.ADD
            ),
            second
        )

    }
}