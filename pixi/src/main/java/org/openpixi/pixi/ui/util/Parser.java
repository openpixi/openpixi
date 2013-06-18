package org.openpixi.pixi.ui.util;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.NumberFormatException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;	 
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import org.openpixi.pixi.physics.Settings;

import org.openpixi.pixi.physics.grid.*;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.diagnostics.methods.*;

public class Parser extends DefaultHandler {

	SAXParserFactory factory;
	Settings settings;
	private boolean numberOfParticles;
	private boolean simulationWidth;
	private boolean simulationHeight;
	private boolean timeStep;
	private boolean interpolator;
	private boolean particleMover;
	private boolean diagnostics;
	private boolean particle;
	private boolean grid;
	private boolean particleIntervall;
	private boolean gridIntervall;
	private boolean iterations;
	
	public Parser(Settings settings){
		
		factory = SAXParserFactory.newInstance();		
		this.settings = settings;
	}
	
	public void parse(String path) {
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(path, this);
		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfig error");
		} catch (SAXException e) {
			System.out.println("Parsing aborted!\n" +
					"Probably the xml file is not formated correctly!\n" +
					"Not all parameters were processed!");
		} catch (IOException e) {
			System.out.println("IO error! Settings file was not parsed!\n" +
					"Probably the settings file is missing or is in the wrong path!\n" +
					"Reverting to defaults...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		
		try {
			
			if (qName.equalsIgnoreCase("numberOfParticles")) {
				numberOfParticles = true;
			} else if (qName.equalsIgnoreCase("simulationWidth")){
				simulationWidth = true;
			} else if (qName.equalsIgnoreCase("simulationHeight")) {
				simulationHeight = true;
			} else if (qName.equalsIgnoreCase("timeStep")) {
				timeStep = true;
			} else if (qName.equalsIgnoreCase("interpolatorAlgorithm")) {
				interpolator = true;
			} else if (qName.equalsIgnoreCase("particleSolver")) {
				particleMover = true;
			} else if(qName.equalsIgnoreCase("diagnostics")) {
				diagnostics = true;
			} else if(qName.equalsIgnoreCase("particle")) {
				particle = true;
			} else if(qName.equalsIgnoreCase("grid")) {
				grid = true;
			} else if(qName.equalsIgnoreCase("particleIntervall")) {
				particleIntervall = true;
			} else if(qName.equalsIgnoreCase("gridIntervall")) {
				gridIntervall = true;
			} else if(qName.equalsIgnoreCase("iterations")) {
				iterations = true;
			} else if (qName.equalsIgnoreCase("settings")) {
				//DO NOTHING
			}
			else throw new Exception();
			
		} catch (Exception e) {
			System.out.println(qName + " can not be parsed!");
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("diagnostics")) {
			diagnostics = false;
		}  
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		
		if (iterations) {
			setIterations(ch, start, length);
			iterations = false;
		}
		if (numberOfParticles) {
			setNumberOfParticles(ch, start, length);
			numberOfParticles = false;
		}
		if (simulationWidth) {
			setSimulationWidth(ch, start, length);
			simulationWidth = false;
		}
		if (simulationHeight) {
			setSimulationHeight(ch, start, length);
			simulationHeight = false;
		}
		if (timeStep) {
			setTimeStep(ch, start, length);
			timeStep = false;
		}
		if (interpolator) {
			setInterpolator(ch, start, length);
			interpolator = false;
		}
		if (particleMover) {
			setParticleSolver(ch, start, length);
			particleMover = false;
		}
		if (diagnostics && particle) {
			addParticleDiagnostics(ch, start, length);
			particle = false;
		}
		if (diagnostics && grid) {
			//NOTHING HERE YET
			grid = false;
		}
		if (diagnostics && particleIntervall) {
			setParticleIntervall(ch, start, length);
			particleIntervall = false;
		}
		if (diagnostics && gridIntervall) {
			setGridIntervall(ch, start, length);
			gridIntervall = false;
		}
	}
	
	private void setIterations(char ch[], int start, int length) {
		int n;
		try{
			n = Integer.parseInt(new String(ch, start, length));
			if ( n < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.out.println("Error: the number of iterations is not a positive integer! " +
					"Setting it to 100.");
			n = 100;
		}
		settings.setIterations(n);
	}
	
	private void setGridIntervall(char ch[], int start, int length) {
		int n;
		try{
			n = Integer.parseInt(new String(ch, start, length));
			if ( n < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.err.println("Error: the grid diagnostics intervall is not a positive " +
					"integer! Setting it to 20.");
			n = 20;
		}
		settings.setGridDiagnosticsIntervall(n);
	}
	
	private void setParticleIntervall(char ch[], int start, int length) {
		int n;
		try{
			n = Integer.parseInt(new String(ch, start, length));
			if ( n < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.err.println("Error: the particle diagnostics intervall is not a positive " +
					"integer! Setting it to 10.");
			n = 10;
		}
		settings.setParticleDiagnosticsIntervall(n);
	}
	
	private void addParticleDiagnostics(char ch[], int start, int length) {
		ParticleMethod mtd;
		String mtdname = new String(ch, start, length);
		try{
			if (mtdname.equalsIgnoreCase("Kinetic Energy")) {
				mtd = new KineticEnergy();
			} else throw new Exception();
		} catch (Exception e){
			System.out.println("Error: OpenPixi does not know about the " + mtdname + " diagnostic" +
					" method. Skipping this setting!");
			return;
		}
		settings.getParticleDiagnostics().add(mtd);
	}
	
	private void setNumberOfParticles(char ch[], int start, int length) {
		int n;
		try{
			n = Integer.parseInt(new String(ch, start, length));
			if ( n < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.out.println("Error: particle number is not a positive integer! Setting n = 100.");
			n = 100;
		}
		settings.setNumOfParticles(n);
	}
	
	private void setTimeStep(char ch[], int start, int length) {
		double t;
		try{
			t = Double.parseDouble(new String(ch, start, length));
			if ( t < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.out.println("Error: time step is not a positive value! Setting it to 1.");
			t = 1;
		}
		settings.setTimeStep(t);
	}
	
	private void setSimulationWidth(char ch[], int start, int length) {
		double w;
		try{
			w = Double.parseDouble(new String(ch, start, length));
			if ( w < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.out.println("Error: simulation width is not a positive value! Setting it to 100.");
			w = 100;
		}
		settings.setTimeStep(w);
	}
	
	private void setSimulationHeight(char ch[], int start, int length) {
		double h;
		try{
			h = Double.parseDouble(new String(ch, start, length));
			if ( h < 0) throw new NumberFormatException();
		} catch (NumberFormatException e){
			System.out.println("Error: simulation height is not a positive value! Setting it to 100.");
			h = 100;
		}
		settings.setTimeStep(h);
	}
	
	private void setInterpolator(char ch[], int start, int length) {
		InterpolatorAlgorithm alg;
		String algname = new String(ch, start, length);
		try{
			if (algname.equalsIgnoreCase("Cloud In Cell")) {
				alg = new CloudInCell();
			} else if (algname.equalsIgnoreCase("Charge Conserving CIC")) {
				alg = new ChargeConservingCIC();
			} else throw new Exception();
		} catch (Exception e){
			System.out.println("Error: OpenPixi does not know about the " + algname + " interpolation" +
					" algorithm. Setting it to CloudInCell.");
			alg = new CloudInCell();
		}
		settings.setInterpolator(alg);
	}
	
	private void setParticleSolver(char ch[], int start, int length) {
		Solver alg;
		String algname = new String(ch, start, length);
		try{
			if (algname.equalsIgnoreCase("Boris")) {
				alg = new Boris();
			} else if (algname.equalsIgnoreCase("Boris Damped")) {
				alg = new BorisDamped();
			} else 	if (algname.equalsIgnoreCase("Empty Solver")) {
				alg = new EmptySolver();
			} else if (algname.equalsIgnoreCase("Euler")) {
				alg = new Euler();
			} else if (algname.equalsIgnoreCase("Euler Richardson")) {
				alg = new EulerRichardson();
			} else if (algname.equalsIgnoreCase("Leap Frog")) {
				alg = new LeapFrog();
			} else 	if (algname.equalsIgnoreCase("Leap Frog Damped")) {
				alg = new LeapFrogDamped();
			} else if (algname.equalsIgnoreCase("Leap Frog Half Step")) {
				alg = new LeapFrogHalfStep();
			} else if (algname.equalsIgnoreCase("SemiImplicit Euler")) {
				alg = new SemiImplicitEuler();
			} 
			else throw new Exception();
		} catch (Exception e){
			System.out.println("Error: OpenPixi does not know about the " + algname + " solver" +
					" mover. Setting it to Boris.");
			alg = new Boris();
		}
		settings.setParticleSolver(alg);
	}
}
