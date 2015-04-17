package repicea.predictor.volume.honertotalvolume;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import repicea.predictor.volume.honertotalvolume.HonerTotalVolumeTree.HonerTotalVolumeTreeSpecies;
import repicea.util.ObjectUtility;

public class HonerTotalVolumeTest {
	
	@SuppressWarnings("rawtypes")
	@Test
	public void TotalVolumeTest() throws IOException, ClassNotFoundException {
		
		String path = ObjectUtility.getPackagePath(getClass());
		
		String referenceFilename = path + "referenceResults.ser";
		
		List<HonerTotalVolumeTreeSpecies> speciesNames = new ArrayList<HonerTotalVolumeTreeSpecies>();
		speciesNames.add(HonerTotalVolumeTreeSpecies.BOJ);
		speciesNames.add(HonerTotalVolumeTreeSpecies.EPR);
		speciesNames.add(HonerTotalVolumeTreeSpecies.ERS);
		speciesNames.add(HonerTotalVolumeTreeSpecies.SAB);
		double height;
		List<HonerTotalVolumeTreeImpl> trees = new ArrayList<HonerTotalVolumeTreeImpl>();
		for (HonerTotalVolumeTreeSpecies speciesName : speciesNames) {
			for (double dbh = 2; dbh <= 40; dbh += 2) {
				height = 1.4 * dbh - 0.02 * dbh * dbh;
				trees.add(new HonerTotalVolumeTreeImpl(speciesName, dbh, height));
			}
		}

		HonerTotalVolumePredictor pred = new HonerTotalVolumePredictor();
		List<Double> predictions = new ArrayList<Double>();
		double volume;
		for (HonerTotalVolumeTreeImpl tree : trees) {
			volume = pred.predictTreeTotalVolume(tree);
			predictions.add(volume);
			System.out.println("Species " + tree.getHonerSpecies().toString() + "; dbh2 " + tree.getSquaredDbhCm() + "; height " + tree.getHeightM() + "; volume " + volume);
		}
		
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
	
	
	public static void main(String[] args) {
		HonerTotalVolumeTreeImpl tree = new HonerTotalVolumeTreeImpl(HonerTotalVolumeTreeSpecies.SAB, 50, 24);
		HonerTotalVolumePredictor pred = new HonerTotalVolumePredictor();
		System.out.println(pred.predictTreeTotalVolume(tree));
		
	}
	
	
	

}
