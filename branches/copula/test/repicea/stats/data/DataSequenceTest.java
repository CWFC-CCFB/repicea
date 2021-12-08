package repicea.stats.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DataSequenceTest {

	@Test
	public void simpleTestsWithQuebecPermanentPlotTreeStatuses() {
		List<Object> terminalStatuses = new ArrayList<Object>();
		terminalStatuses.add(23.0);
		terminalStatuses.add(24.0);
		terminalStatuses.add(25.0);
		terminalStatuses.add(26.0);
		terminalStatuses.add(29.0);
		List<Object> deadStatuses = new ArrayList<Object>();
		deadStatuses.add(14.0);
		deadStatuses.add(16.0);
//		deadStatuses.add(34.0);
//		deadStatuses.add(36.0);
//		deadStatuses.add(44.0);
//		deadStatuses.add(46.0);
//		deadStatuses.add(54.0);
//		deadStatuses.add(56.0);
		List<Object> aliveStatuses = new ArrayList<Object>();
		aliveStatuses.add(10.0);
		aliveStatuses.add(12.0);
		List<Object> forgottenStatuses = new ArrayList<Object>();
		forgottenStatuses.add(30.0);
		forgottenStatuses.add(32.0);
		List<Object> recruitStatuses = new ArrayList<Object>();
		recruitStatuses.add(40.0);
		recruitStatuses.add(42.0);
		List<Object> renumberedStatuses = new ArrayList<Object>();
		renumberedStatuses.add(50.0);
		renumberedStatuses.add(52.0);
		
		DataSequence dataSequence = new DataSequence();

		List<Object> alives = new ArrayList<Object>();
		alives.addAll(aliveStatuses);
		alives.addAll(forgottenStatuses);
		alives.addAll(recruitStatuses);
		alives.addAll(renumberedStatuses);
		
		
		List<Object> possibleOutcomes;
		for (Object obj : alives) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(aliveStatuses);
			possibleOutcomes.addAll(deadStatuses);
			possibleOutcomes.addAll(terminalStatuses);
			dataSequence.put(obj, DataSequence.convertListToMap(possibleOutcomes));
		}
		for (Object obj : deadStatuses) {
			possibleOutcomes = new ArrayList<Object>();
			possibleOutcomes.addAll(terminalStatuses);
			dataSequence.put(obj, DataSequence.convertListToMap(possibleOutcomes));
		}
		
		DataPattern pattern = new DataPattern(null, 10.0, 10.0, 10.0, 16.0, 26.0);
		
		boolean isOk = dataSequence.doesFitInThisSequence(pattern, null);

		Assert.assertTrue(isOk);
		
		pattern.clear();
		pattern.add(10.0);
		pattern.add(16.0);
		pattern.add(10.0);
		pattern.add(10.0);
		pattern.add(26.0);

		isOk = dataSequence.doesFitInThisSequence(pattern, null);
		Assert.assertTrue(!isOk);
	}
}
