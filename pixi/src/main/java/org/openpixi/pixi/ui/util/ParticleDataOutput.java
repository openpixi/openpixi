package org.openpixi.pixi.ui.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ParticleDataOutput extends EmptyParticleDataOutput{
	
	private BufferedWriter totalKineticEnergy;
	
	
	public ParticleDataOutput(String dir, String runid) throws IOException {
		totalKineticEnergy = writerFactory(dir + "Energy" + runid);
	}
	
	public void kineticEnergy(double energy) {
		try {
			totalKineticEnergy.write("" + energy);
			totalKineticEnergy.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeStreams() {
		try {
			
			totalKineticEnergy.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedWriter writerFactory(String path) throws IOException {
		File file;
		FileWriter fstream;
		BufferedWriter out;
	
		file = new File(path);
		fstream = new FileWriter(file);
		out = new BufferedWriter(fstream);
		return out;
	}
}
