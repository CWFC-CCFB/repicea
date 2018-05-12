package repicea.predictor.matapedia;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import repicea.math.Matrix;
import repicea.predictor.matapedia.MatapediaTree.MatapediaTreeSpecies;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.util.ObjectUtility;

public class MatapediaMortalityPredictorTest {

	private static List<MatapediaStand> Stands = readStands();
	
	
	private static List<MatapediaStand> readStands() {
		List<MatapediaStand> stands = new ArrayList<MatapediaStand>();
		MatapediaStandImpl stand = new MatapediaStandImpl(false, true, 1, 1);
		for (MatapediaTreeSpecies species : MatapediaTreeSpecies.values()) {
			for (double bal = 10d; bal <= 40d; bal += 10d) {
				for (double dbh = 5d; dbh <= 30d; dbh += 5d) {
					stand.addTree(new MatapediaTreeImpl(species, dbh, bal));
				}
			}
		}
		stands.add(stand);
		return stands;
	}
	
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testPredictions() throws IOException, ClassNotFoundException {
	
		
		MatapediaMortalityPredictor pred = new MatapediaMortalityPredictor(false);

		List<Double> predictions = new ArrayList<Double>();
		
		for (MatapediaStand st : Stands) {
			for (MatapediaTree tree : st.getMatapediaTrees()) {
				double prediction = pred.predictEventProbability(st, tree);
				predictions.add(prediction);
			}
		}
		
		String path = ObjectUtility.getPackagePath(getClass());
		
		String referenceFilename = path + "referenceMortalityResults.ser";
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//	  	try {
//	  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//	  		ObjectOutputStream out = new ObjectOutputStream(fos);
//	  		out.writeObject(predictions);
//	  		out.close();
//	  	} catch(IOException ex) {
//	  		ex.printStackTrace();
//	  		throw ex;
//	  	}

	  	System.out.println("Loading reference map...");
	  	List refList;
	  	try {
	  		FileInputStream fis = new FileInputStream(referenceFilename);
	  		ObjectInputStream in = new ObjectInputStream(fis);
	  		refList = (List) in.readObject();
	  		in.close();
	  	} catch(IOException ex) {
	  		ex.printStackTrace();
	  		throw ex;
	  	}

	  	System.out.println("Comparing results...");
  		assertEquals("Number of values", refList.size(), predictions.size());
	  	
	  	for (int i = 0; i < refList.size(); i++) {
	  		double valueRef = (Double) refList.get(i);
	  		double currentValue = predictions.get(i);
	  		assertEquals("Testing value " + i, valueRef, currentValue, 1E-8);
	  	}
		
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testInStochasticModeWithRandomEffectOnly() throws IOException {
		int nbReal = 100000;
		
		MatapediaMortalityPredictor stochasticPredictor = new MatapediaMortalityPredictor(false, true, false);
		MatapediaMortalityPredictor deterministicPredictor = new MatapediaMortalityPredictor(false);
		
		MatapediaStand stand = Stands.get(0);
		List<MatapediaTreeImpl> trees = ((List) stand.getMatapediaTrees());
		
		for (int treeID = 0; treeID < 10; treeID++) {
			MatapediaTreeImpl tree = trees.get(treeID*10);
			EmpiricalDistribution dist = new EmpiricalDistribution();
			Matrix resultWrapper;
			for (int i = 0; i < nbReal; i++) {
				((MatapediaStandImpl) stand).setMonteCarloRealizationId(i);
				resultWrapper = new Matrix(1,1);
				resultWrapper.m_afData[0][0] = stochasticPredictor.predictEventProbability(stand, tree);
				dist.addRealization(resultWrapper);
			}
			double meanDeterministic = deterministicPredictor.predictEventProbability(stand, tree);
			double meanStochastic = dist.getMean().m_afData[0][0];

			System.out.println("Deterministic : " + meanDeterministic + "; Stochastic : " + meanStochastic);			
			assertEquals(meanDeterministic, meanStochastic, 0.003);
		}
	}

	
}
