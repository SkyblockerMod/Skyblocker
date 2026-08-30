package de.hysky.skyblocker.skyblock.garden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GreenhousePasteTest {
	@Test
	void extractLayoutCode() {
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/?layout=ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://api.skyshards.com/share/ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("  ABC  "));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?tab=1&layout=ABC&mode=x"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://api.skyshards.com/share/ABC?foo=bar"));
		Assertions.assertEquals("a-b_c=", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=a-b_c="));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://skymutations.eu/?layout=ABC"));
		Assertions.assertEquals("NrBMBoIIgLQewLYCMCuATK4AMBdcZwAWcWRVDbPAgNhPmXU13wgA46zHL9jp7ymVYsVIMKzYMVqiB3SeHYyuE2n07iqtEf2Wbw0nRvy1FhwfnZqx54O23qb7Aw7ntTLiQEZw3qABEAUwBDNAACAAUAGyCAOwAXAGdMTypvaECQiOj4pJ9U8ABmEgBhAEsYtACAJwSACxCA5PyRDLCo2MSm-G8AVhIAMUiAuriqoIAHLuBvaVasjtyU7vAAdhLyypr6yqnvRTn2nN3wAE4SA+zOvJYfc+C2y8WqCCL-e-mj64I+t8zDq6WBDWvweCymEDOII+AKoRV8g2GtVGEymRWgCJGY0mXyKrzKFWqdQaqKIdz+jxJPwxSKxJNm73+T3wRWB+M2RJ2OIUAyGmJRXMh1OR2MBxF8FzBX2Irwln1F4B+sphPFWZNBcqEpzV0KZwD64oZFK+fXShslgL6eI2hO2jWNpKhjKmfSpvJp-It+m1TvtrOtW2J9v2Zo1+D6kKVutoBvJ5r0MpDyuAtEViajqsdRsBtAjaama3hbuF+cgPMRxa+aytBIDnMBaxaecrCrLfJFVDW9NjoeAaz9NY5dvr3KgQtpzcFRfHgPYMfVSfY1fZtqm7Fd5enVHY-eXgZnWszcfwZznOqmZ1N3aTZwTV91Z0bd-PLcPPbOXfn94zkefwafXzOXN-xSHAgA",
				GreenhousePaste.extractLayoutCode("https://skymutations.eu/greenhouse?layout=NrBMBoIIgLQewLYCMCuATK4AMBdcZwAWcWRVDbPAgNhPmXU13wgA46zHL9jp7ymVYsVIMKzYMVqiB3SeHYyuE2n07iqtEf2Wbw0nRvy1FhwfnZqx54O23qb7Aw7ntTLiQEZw3qABEAUwBDNAACAAUAGyCAOwAXAGdMTypvaECQiOj4pJ9U8ABmEgBhAEsYtACAJwSACxCA5PyRDLCo2MSm-G8AVhIAMUiAuriqoIAHLuBvaVasjtyU7vAAdhLyypr6yqnvRTn2nN3wAE4SA+zOvJYfc+C2y8WqCCL-e-mj64I+t8zDq6WBDWvweCymEDOII+AKoRV8g2GtVGEymRWgCJGY0mXyKrzKFWqdQaqKIdz+jxJPwxSKxJNm73+T3wRWB+M2RJ2OIUAyGmJRXMh1OR2MBxF8FzBX2Irwln1F4B+sphPFWZNBcqEpzV0KZwD64oZFK+fXShslgL6eI2hO2jWNpKhjKmfSpvJp-It+m1TvtrOtW2J9v2Zo1+D6kKVutoBvJ5r0MpDyuAtEViajqsdRsBtAjaama3hbuF+cgPMRxa+aytBIDnMBaxaecrCrLfJFVDW9NjoeAaz9NY5dvr3KgQtpzcFRfHgPYMfVSfY1fZtqm7Fd5enVHY-eXgZnWszcfwZznOqmZ1N3aTZwTV91Z0bd-PLcPPbOXfn94zkefwafXzOXN-xSHAgA&fake_param=true")
		);
	}
}
