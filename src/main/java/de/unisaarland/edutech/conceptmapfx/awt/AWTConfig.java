package de.unisaarland.edutech.conceptmapfx.awt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AWTConfig {

	private int conceptCreationWeight;
	private int ownEditWeight;
	private int foreignEditWeight;
	private int conceptsLinkedWeight;
	private int linkEditWeight;

	public AWTConfig() {
		try {
			Properties p = new Properties();
			p.load(new FileInputStream("awt.properties"));

			conceptCreationWeight = getPropertyForAWT(p, "conceptCreationWeight");
			ownEditWeight = getPropertyForAWT(p, "ownEditWeight");
			foreignEditWeight = getPropertyForAWT(p, "foreignEditWeight");
			linkEditWeight = getPropertyForAWT(p, "linkEditWeight");

			conceptsLinkedWeight = getPropertyForAWT(p, "conceptsLinked");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static double getZPDLowerBound() {
		return loadZPDParam("zpdLowerBound");
	}

	private static double loadZPDParam(String param) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream("awt.properties"));
			return Double.parseDouble(p.getProperty(param, "no property found!"));
		} catch (Exception e) {
			throw new RuntimeException("error loading zpd", e);
		}
	}

	public static double getZPDHigherBound() {
		return loadZPDParam("zpdHigherBound");
	}

	public long compute(long cCreate, long ownEdit, long foreignEdit, long linkEdit, long conceptsLinked) {
		return conceptCreationWeight * cCreate + ownEditWeight * ownEdit + foreignEditWeight * foreignEdit
				+ conceptsLinkedWeight * conceptsLinked + linkEditWeight * linkEdit;
	}

	private int getPropertyForAWT(Properties p, String key) {
		String s = p.getProperty(key);

		if (s == null)
			throw new RuntimeException("Config Parameter not found or invalid: " + key);

		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Config Parameter not found or invalid: " + e.getMessage());
		}

	}
}
