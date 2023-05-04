/*
 * This file is part of the repicea-iotools library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.io.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.tools.ImportFieldElement.FieldType;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The TestRecordReader is an implementation of RecordReader. It serves only for test purposes.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
public class RecordReaderImpl2 extends REpiceaRecordReader {

	private enum Level {
		stratumLevel,
		plotLevel, 
		treeLevel 
	}

	private enum FieldID implements LevelProviderEnum {
		STRATUM(Level.stratumLevel),
		
		PLOT(Level.plotLevel),
		
		LATITUDE(Level.plotLevel),
		LONGITUDE(Level.plotLevel),
		ALTITUDE(Level.plotLevel),
		
		ECOREGION(Level.plotLevel),
		TYPEECO(Level.plotLevel),
		DRAINAGE_CLASS(Level.plotLevel),
		
		ORIGIN(Level.plotLevel),
		DISTURBANCE(Level.plotLevel),
		
		PLOTWEIGHT(Level.plotLevel),
		
		PRECTOT(Level.plotLevel),
		MEANTEMP(Level.plotLevel),
		DEGJR(Level.plotLevel),
		PRECUTIL(Level.plotLevel),
		PRECSAIS(Level.plotLevel),
		JRXGEL(Level.plotLevel),
		JRXGELC(Level.plotLevel),
		JRCROIS(Level.plotLevel),
		DPV(Level.plotLevel),
		ARIDITE(Level.plotLevel),
		NEIGEP(Level.plotLevel),
		NEIGET(Level.plotLevel),

		SPECIES(Level.treeLevel),
		TREESTATUS(Level.treeLevel),
		TREEFREQ(Level.treeLevel),
		TREEDHPCM(Level.treeLevel),
		TREEHEIGHT(Level.treeLevel),
		TREEVOLUME(Level.treeLevel),
		TREEQUALITY(Level.treeLevel),
		
		AGE3M(Level.plotLevel),
		AGE4M(Level.plotLevel),
		AGE7M(Level.plotLevel),
		AGE12M(Level.plotLevel),
		AGEHD(Level.plotLevel),
		DOMINANT_HEIGHT(Level.plotLevel);
		
		private Level level;
		
		FieldID(Level level) {
			this.level = level;
		}

		@Override
		public Level getFieldLevel() {
			return level;
		}
	}



	private static enum MessageID implements TextableEnum {
		DescStratumName("Stratum Identifier (String)", "Identifiant de strate (String)"),
		HelpStratumName("This field indicates the strata that are in the input file. This field is optional. If you do not specify any field, the model considers that the input file contains a single stratum. If many strata are found in the input file, the module makes it possible to select one stratum.",
				"Ce champ identifie les strates qui sont comprises dans votre fichier d'entr\u00E9e. Ce champs est facultatif. Si vous ne lui associez aucun champ, ART\u00C9MIS consid\u00E8re que votre fichier ne contient qu'une seule strate. Dans le cas contraire, ART\u00C9MIS vous offrira la possibilit\u00E9 de s\u00E9lectionner une des strates de votre fichier d'entr\u00E9e si celui-ci en contient plus d'une."),
		DescPlotName("Plot Identifier (String)", "Identifiant de placette (String)"),
		HelpPlotName("This field indicates the plots in the input file. This field is mandatory.", "Ce champ identifie les placettes qui sont comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire."),
		DescPlotYCoord("Latitude (Double)", "Latitude (Double)"),
		HelpPlotYCoord("This field indicates the latitude of the plots in the input file. This field is mandatory. The latitude must be in a degree-decimal format (e.g.: 48.54383).", 
				"Ce champ identifie la latitude des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. La latitude doit \u00EAtre lue dans un format degr\u00E9-d\u00E9cimal (p.ex.: 48.54383)."),
		DescPlotXCoord("Longitude (Double)", "Longitude (Double)"),
		HelpPlotXCoord("This field indicates the longitude of the plots in the input file. This field is mandatory. The longitude must be in a degree-decimal format (e.g.: -72.38273).", 
				"Ce champ identifie la longitude des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. La longitude doit \u00EAtre lue dans un format degr\u00E9-d\u00E9cimal (p.ex.: -72.38273)."),
		DescPlotAltitude("Elevation (Double)", "Altitude (Double)"),
		HelpPlotAltitude("This field indicates the elevation above sea level of the plots in the input file. This field is mandatory. The elevation must be expressed in meter (e.g.: 332 m).", 
				"Ce champ identifie l'altitude des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. L'altitude doit \u00EAtre exprim\u00E9e en m\u00E8tres (p.ex.: 332 m)."),
		DescPlotEcoRegion("Ecological Region (String)", "R\u00E9gion \u00E9cologique (String)"),
		HelpPlotEcoRegion("This field indicates the ecological region in which are located the plots in the input file. This field is mandatory. The values are the usual two-character codes used by Quebec Ministry of Natural Resources and Wildlife (e.g.: 5g).", 
				"Ce champ identifie la r\u00E9gion \u00E9cologique des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 deux caract\u00E8res de la r\u00E9gion \u00E9cologique tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: 5g)."),
		DescPlotTypeEco("Ecological type (String)", "Type \u00E9cologique (String)"),
		HelpPlotTypeEco("This field indicates the ecological type of the plots in the input file. This field is required. The values are the usual four-character codes used by Quebec Ministry of Natural Resources and Wildlife (e.g.: FE32).",
				"Ce champ identifie le type ecologique des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 quatre caract\u00E8res du type �cologique tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: FE32)."),
		LatitudeError("latitude must be between 45 and 53 degrees", "la latitude doit \u00EAtre comprise entre 45 et 53 degr\u00E9s"),
		LongitudeError("longitude must be between -80 and -58 degrees", "la longitude doit \u00EAtre comprise entre -80 et -58 degr\u00E9s"),
		AltitudeError("elevation must be between 0 and 1300 m", "l'altitude doit \u00EAtre comprise entre 0 et 1300 m"),
		EcolRegError("the following ecological region is not considered in ART\u00C9MIS module: ", "la r\u00E9gion \u00E9cologique suivante n'est pas reconnue par ART\u00C9MIS : "),
		VegPotError("the following potential vegetation is not considered in ART\u00C9MIS module: ", "la v\u00E9g\u00E9tation potentielle suivante n'est pas reconnue par ART\u00C9MIS : "),
		DrainageError("the following drainage class is not considered in ART\u00C9MIS module: ", "la classe de drainage suivante n'est pas reconnue par ART\u00C9MIS : "),
		WeightError("the weight of a plot must be between 0 and 100", "le poids d'une placette doit \u00EAtre compris entre 0 et 100"),
		TotalPrecError("mean annual precipitation must be between 0 and 1700 mm", "les pr\u00E9cipitations annuelles moyennes doivent \u00EAtre comprises entre 0 et 1700 mm"),
		MeanTempError("mean annual temperature must be between -7 and +10 C degrees", "la temp\u00E9rature annuelle moyenne doit \u00EAtre comprise entre -7 et +10 degr\u00E9s Celsius"),
		SpeciesError("the following species is not considered in ART\u00C9MIS module: ", "l'esp\u00E8ce suivante n'est pas reconnue par ART\u00C9MIS : "),
		StatusError("the following status is not considered in ART\u00C9MIS module: ", "l'\u00E9tat suivant n'est pas reconnu par ART\u00C9MIS : "),
		DBHError("DBH values are inconsistent", "les valeurs de DHP sont incoh\u00E9rentes"),
		TreeFreqError("tree frequencies are inconsistent", "les fr\u00E9quence des arbres sont incoh\u00E9rentes"),
		TreeHeightError("tree height is inconsistent", "la hauteur d'arbre est incoh\u00E9rente"),
		TreeVolumeError("tree volume must be between 0 and 30 m3", "le volume d'un arbre doit \u00EAtre compris entre 0 et 30 m3"),
		
		DescPlotDrainClass("Draining Class (String)", "Classe de drainage (Texte)"),
		HelpPlotDrainClass("This field sets the drainage class for the plots of the input dataset. This field is mandatory. It is the usual code provided by the Quebec Ministry of Natural Resources and Wildlife (e.g. 3).", 
				"Ce champ identifie la classe de drainage des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 un caract\u00E8re de la classe de drainage tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: 3)."),
		DescWeight("Plot weight (Double)", "Poids de la placette (Double)"),
		helpPlotWeight("This field sets the weight of the individual plots given that the stratum encompasses many plots. This fiels is optional. In case no field would be specified, ART\u00C9MIS will consider each plot as a complete plot (i.e. unitary weight). The value of this field is usually provided by the Forest Inventory Branch of the Quebec Ministry of Natural Resources and Wildlife.",
				"Ce champ identifie le poids des placettes comprises dans votre fichier d'entr\u00E9e par rapport \u00E0 l'ensemble de la strate. Ce champ est facultatif. Si vous ne lui associez aucun champ, ART\u00C9MIS attribuera un poids unitaire par d\u00E9faut. La valeur de ce champ est habituellement fournit par la compilation de l'inventaire provincial de la Direction des inventaires forestiers du MRNF."),
		DescPlotTotalPrec("Annual precipitation in mm (Double)", "Pr\u00E9cipitations annuelles en mm (Double)"),
		HelpTotalPrec("This field sets the mean annual precipitation of the plots. This field is optional. It must be computed as the mean for the 1971-2000 period and may be estimated using BioSIM, a software developed by the Canadian Forest Service.",
				"Ce champ identifie les pr\u00E9cipitations annuelles moyennes des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est facultatif. Il correspond \u00E0 la moyenne sur la p\u00E9riode 1971-2000. La valeur de ce champ est habituellement estim\u00E9e \u00E0 l'aide du logiciel BioSIM du Service canadien des for\u00EAts."),
		DescPlotMeanTemp("Mean temperature in degrees (Double)", "Temp\u00E9rature moyenne en degr\u00E9s (Double)"),
		HelpPlotMeanTemp("This field identifies the mean annual temperature of the plots. It is optional. It must be computed as the mean for the 1971-2000 period and may be estimated using BioSIM, a software developed by the Canadian Forest Service.",
				"Ce champ identifie la temp\u00E9rature annuelle moyenne des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est facultatif. Il correspond \u00E0 la moyenne sur la p\u00E9riode 1971-2000. La valeur de ce champ est habituellement estim\u00E9e \u00E0 l'aide du logiciel BioSIM du Service canadien des for\u00EAts."),
		DescTreeSpecies("Tree Species (String)", "Code d'essence de l'arbre (Texte)"),
		HelpTreeSpecies("This field sets the tree species for the trees of the input file. This field is mandatory. It must be specified using the usual species code of the Quebec Ministry of Natural Resources and Wildlife (e.g.: SAB).",
				"Ce champ identifie l'esp\u00E8ce des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 trois caract\u00E8res de l'esp\u00E8ce tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: SAB)."),
		DescTreeStatus("Tree status code (String)", "Code d'\u00E9tat de l'arbre (Texte)"),
		HelpTreeStatus("This field identifies the tree status. It is mandatory. It must be specified using the usual tree status code of the Quebec Ministry of Natural Resources and Wildlife (e.g.: 10).",
				"Ce champ identifie l'\u00E9tat des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 deux caract\u00E8res de l'\u00E9tat des arbres tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: 10)."),
		DescTreeDBH("Tree diameter at breast height (Double)", "Diam\u00E8tre a hauteur de poitrine de l'arbre (Double)"),
		HelpTreeDBH("This field sets the diameter at breast height (DBH, 1,3 m) of the trees. It is mandatory. The DBH measure must be in cm (e.g.: 24.2 cm).",
				"Ce champ identifie le diam\u00E8tre \u00E0 hauteur de poitrine (DHP, 1,3 m) des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit de la mesure du DHP en centim\u00E8tres (p.ex.: 24,2 cm)."),
		DescTreeFreq("Tree frequency (Double)", "Fr\u00E9quence de l'arbre (Double)"),
		HelpTreeFreq("This field sets the tree frequency. It is mandatory. The value must be a double expressed at the plot level (e.g.: 4.0 trees in the plot).",
				"Ce champ identifie la fr\u00E9quence des arbres de m\u00EAme diam\u00E8tre et de m\u00EAme esp\u00E8ce dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. La valeur doit \u00EAtre un double exprim\u00E9 \u00E0 l'\u00E9chelle de la placette (p.ex.: 4,0 arbres dans la placette)."),
		DescTreeHeight("Tree height (Double)", "Hauteur de l'arbre (Double)"),
		HelpTreeHeight("This field identifies the tree height. It is optional. ART\u00C9MIS estimates the tree height when no height is specified using a height-diameter relationship. The value of this field must be a double expressed in m (e.g.: 15.4 m).",
				"Ce champ identifie la hauteur des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est facultatif. ART\u00C9MIS estime la hauteur des arbres dont la hauteur est inconnue \u00E0 l'aide d'une relation hauteur-diam\u00E8tre. La valeur de ce champ doit \u00EAtre un double exprim\u00E9 en m\u00E8tres (p.ex.: 15,4 m)."),
		DescTreeVolume("Tree volume (Double)", "Volume de l'arbre (Double)"),  
		HelpTreeVolume("This field identifies the tree volume. It is optional. If some tree volumes were measured, ART\u00C9MIS will compute correction factors in order to correct the predictions of the volume equation for this particular plot. The value must be a double expressed in m3 (e.g.: 0.6072 m3). This field is not taken into account during stochastic simulation.",
				"Ce champ identifie le volume de l'arbre. Il est facultatif. Si vous avez observ\u00E9 ou estim\u00E9 le volume de certains arbres \u00E0 l'aide de tarifs de cubage locaux, ART\u00C9MIS corrigera les pr\u00E9visions en volume en fonction de ces valeurs. La valeur doit \u00EAtre un double exprim\u00E9 en m\u00E8tres cubes (p.ex.: 0,6072 m3). Ce champ n'est pas pris en compte lors des simulations stochastiques.")
		;

		
		MessageID(String englishString, String frenchString) {
			setText(englishString, frenchString);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}

	private static List<ImportFieldElement> defineFields() {
		List<ImportFieldElement> importFields = new ArrayList<ImportFieldElement>();
		importFields.add(new ImportFieldElement(FieldID.STRATUM,
												MessageID.DescStratumName.toString (),
												"capsis.quebecmrnf.field.stratumname",
												true,
												MessageID.HelpStratumName.toString (),
												FieldType.String));
		importFields.add(new ImportFieldElement(FieldID.PLOT,
												MessageID.DescPlotName.toString (),
												"capsis.quebecmrnf.field.plotname",
												false,
												MessageID.HelpPlotName.toString (),
												FieldType.String));
		importFields.add(new ImportFieldElement(	FieldID.SPECIES,
				MessageID.DescTreeSpecies.toString(),
				"capsis.artemis.field.treeessence",
				false,
				MessageID.HelpTreeSpecies.toString(),
				FieldType.String));
		return importFields;
	}

	Map<String, String> resultMap = new HashMap<String, String>();
	
	
	@SuppressWarnings({"rawtypes" })
	@Override
	protected Enum defineGroupFieldEnum() {
		return FieldID.STRATUM;
	}

	@Override
	protected void readLineRecord(Object[] oArray, int lineCounter) throws VariableValueException, Exception {
		int plotIdIndex = getImportFieldManager().getIndexOfThisField(FieldID.PLOT);
		int speciesIndex = getImportFieldManager().getIndexOfThisField(FieldID.SPECIES);
		resultMap.put(oArray[plotIdIndex].toString(), oArray[speciesIndex].toString());
	}

	
	@Override
	protected List<ImportFieldElement> defineFieldsToImport() {
		return RecordReaderImpl2.defineFields();
	}


	

	
	
	
}
