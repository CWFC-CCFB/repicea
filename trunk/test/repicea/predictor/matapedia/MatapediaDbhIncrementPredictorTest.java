package repicea.predictor.matapedia;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import repicea.predictor.matapedia.MatapediaTree.MatapediaTreeSpecies;
import repicea.util.ObjectUtility;

@SuppressWarnings("rawtypes")
public class MatapediaDbhIncrementPredictorTest {

	@Test
	public void testPredictions() throws IOException, ClassNotFoundException {
		
		List<MatapediaStand> stands = new ArrayList<MatapediaStand>();
		MatapediaStandImpl stand = new MatapediaStandImpl(false, false, 1, 1);
		for (MatapediaTreeSpecies species : MatapediaTreeSpecies.values()) {
			for (double bal = 10d; bal <= 40d; bal += 10d) {
				for (double dbh = 5d; dbh <= 30d; dbh += 5d) {
					stand.addTree(new MatapediaTreeImpl(species, dbh, bal));
				}
			}
		}
		stands.add(stand);
		
		MatapediaDbhIncrementPredictor pred = new MatapediaDbhIncrementPredictor(false);

		List<Double> predictions = new ArrayList<Double>();
		
		for (MatapediaStand st : stands) {
			for (MatapediaTree tree : st.getMatapediaTrees()) {
				double prediction = pred.predictGrowth(st, tree);
				predictions.add(prediction);
			}
		}

		
		String path = ObjectUtility.getPackagePath(getClass());
		
		String referenceFilename = path + "referenceGrowthResults.ser";
		
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
	
	
	
}
