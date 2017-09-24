package cz.jskrabal.proxy.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

/**
 * Created by janskrabal on 15/06/16.
 */
class IdGeneratorTest {

    @Test
    fun uuidGeneratorTest() {
        val collisions = testGeneratorInCycle(IdGeneratorType.UUID)
        assertEquals(0, collisions.toLong())
    }

    @Test
    fun uuidGeneratorFormatTest() {
        // Throws exception if the string is not UUID
        UUID.fromString(IdGeneratorType.UUID.generateId())
    }

    @Test
    fun sequenceGeneratorTest() {
        val collisions = testGeneratorInCycle(IdGeneratorType.SEQUENCE)
        assertEquals(0, collisions.toLong())

    }

    @Test
    fun sequenceGeneratorFormatTest() {
        // Throws exception if the generated random is not a number
        java.lang.Long.parseLong(IdGeneratorType.SEQUENCE.generateId())
    }

    @Test
    fun sequenceGeneratorSequenceTest() {
        var currentId: Long
        var lastId: Long = 0

        for (seq in 0..GENERATION_COUNT - 1) {
            currentId = java.lang.Long.parseLong(IdGeneratorType.SEQUENCE.generateId())

            assertTrue(currentId > lastId)

            lastId = currentId
        }
    }

    @Test
    fun randomGeneratorTest() {
        val collisions = testGeneratorInCycle(IdGeneratorType.RANDOM)
        assertTrue(collisions <= GENERATION_COUNT / 1000)
    }

    private fun <T> testGeneratorInCycle(generator: IdGenerator<T>): Int {
        var id: T
        val uuids = HashSet<T>()
        for (i in 0..GENERATION_COUNT - 1) {
            id = generator.generateId()
            uuids.add(id)
        }
        return GENERATION_COUNT - uuids.size
    }

    companion object {

        private val GENERATION_COUNT = 1000
    }
}
