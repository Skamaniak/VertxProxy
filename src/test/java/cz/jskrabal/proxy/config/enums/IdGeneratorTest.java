package cz.jskrabal.proxy.config.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by janskrabal on 15/06/16.
 */
public class IdGeneratorTest {

	private static final int GENERATION_COUNT = 1000;

	@Test
	public void uuidGeneratorTest() {
		int collisions = testGeneratorInCycle(IdGeneratorType.UUID);
        assertEquals(0, collisions);
	}

	@Test
	public void uuidGeneratorFormatTest() {
		// Throws exception if the string is not UUID
		UUID.fromString(IdGeneratorType.UUID.generateId());
	}

	@Test
	public void sequenceGeneratorTest() {
        int collisions = testGeneratorInCycle(IdGeneratorType.SEQUENCE);
        assertEquals(0, collisions);

    }

	@Test
	public void sequenceGeneratorFormatTest() {
		// Throws exception if the generated random is not a number
		Long.parseLong(IdGeneratorType.SEQUENCE.generateId());
	}

	@Test
	public void sequenceGeneratorSequenceTest() {
		long currentId, lastId = 0;

		for (long seq = 0; seq < GENERATION_COUNT; seq++) {
			currentId = Long.parseLong(IdGeneratorType.SEQUENCE.generateId());

			assertTrue(currentId > lastId);

			lastId = currentId;
		}
	}

    @Test
    public void randomGeneratorTest(){
        int collisions = testGeneratorInCycle(IdGeneratorType.RANDOM);
        assertTrue(collisions <= GENERATION_COUNT / 1000);
    }

	private <T> int testGeneratorInCycle(IdGenerator<T> generator) {
		T id;
		Set<T> uuids = new HashSet<>();
		for (int i = 0; i < GENERATION_COUNT; i++) {
			id = generator.generateId();
			uuids.add(id);
		}
        return GENERATION_COUNT - uuids.size();
	}
}
