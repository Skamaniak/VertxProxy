package cz.jskrabal.proxy.config.enums;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by janskrabal on 15/06/16.
 */
public class ConfigurationParameterTest {

	@Test
	public void testGetFullJsonKey() {
		String[] fullKeyParts;
		List<String> keyParts;

		for (ConfigurationParameter parameter : ConfigurationParameter.values()) {
			keyParts = parameter.getJsonKeyParts();
            fullKeyParts = parameter.getFullJsonKey().split("\\.");

            assertEquals(keyParts.size(), fullKeyParts.length);

			for (int i = 0; i < keyParts.size(); i++) {
                assertEquals(keyParts.get(i), fullKeyParts[i]);
			}
		}
	}

}
