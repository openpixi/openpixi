package org.openpixi.pixi.ui.tab;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.MainControlApplet;
import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.UserPreferences;
import org.openpixi.pixi.ui.util.FileIO;
import org.openpixi.pixi.ui.util.yaml.YamlPanelWriter;
import org.openpixi.pixi.ui.util.yaml.YamlPanels;
import org.openpixi.pixi.ui.util.yaml.YamlParser;

public class FileTab extends Box {

	private MainControlApplet parent;
	private SimulationAnimation simulationAnimation;
	private PanelManager panelManager;
	private JFileChooser fc;
	private JButton openButton;
	private JButton saveButton;
	private JButton applyButton;
	private JButton moreButton;
	private JTextArea fileTextArea;
	JMenuItem itemApplyPanelSettings;
	JMenuItem itemWritePanelSettings;

	public FileTab(MainControlApplet parent, SimulationAnimation simulationAnimation, PanelManager panelManager) {
		super(BoxLayout.PAGE_AXIS);
		this.parent = parent;
		this.simulationAnimation = simulationAnimation;
		this.panelManager = panelManager;

		fc = new JFileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		File initialDirectory = workingDirectory;

		File inputDirectory = new File(workingDirectory, "input");
		if (inputDirectory.exists()) {
			initialDirectory = inputDirectory;
		}

		File preferenceDirectory = getPathFromPreferences();
		if (preferenceDirectory != null) {
			initialDirectory = preferenceDirectory;
		}
		fc.setCurrentDirectory(initialDirectory);

		openButton = new JButton("Open...");
		openButton.addActionListener(new OpenButtonListener());
		saveButton = new JButton("Save...");
		saveButton.addActionListener(new SaveButtonListener());
		applyButton = new JButton("Apply");
		applyButton.addActionListener(new ApplyButtonListener());
		moreButton = new JButton("More...");
		moreButton.addActionListener(new MoreButtonListener());
		fileTextArea = new JTextArea();
		JScrollPane scrollpane = new JScrollPane(fileTextArea);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(openButton);
		buttonBox.add(saveButton);
		buttonBox.add(applyButton);
		buttonBox.add(moreButton);

		this.add(buttonBox);
		this.add(scrollpane);
	}

	class OpenButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			int returnVal = fc.showOpenDialog(parent);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					String content = FileIO.readFile(file);
					fileTextArea.setText(content);
					fileTextArea.setCaretPosition(0); // Jump to top position
					applyTextAreaSettings();
					applyTextAreaPanelSettings();
				} catch (IOException e) {
					// TODO Error message
				}
				putPathInPreferences(file);
			} else {
				// Open command cancelled by user
			}
		}
	}

	class SaveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

			int returnVal = fc.showSaveDialog(parent);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					// Show confirmation dialog
					int response = JOptionPane.showConfirmDialog(
							parent,
							"Are you sure you want to override existing file?",
							"Confirm", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						// Ok, proceed
					} else if (response == JOptionPane.NO_OPTION) {
						return;
					} else if (response == JOptionPane.CLOSED_OPTION) {
						return;
					}
				}
				String string = fileTextArea.getText();
				try {
					FileIO.writeFile(file, string);
				} catch (IOException e) {
					// TODO Error message
				}
				putPathInPreferences(file);
			} else {
				// Save command cancelled by user
			}
		}
	}

	class ApplyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			applyTextAreaSettings();

			// Also start animation immediately:
			simulationAnimation.startAnimation();
		}
	}

	class MoreButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			PopupMenu menu = new PopupMenu();
			menu.show(moreButton, 0, moreButton.getHeight());
		}
	}

	class PopupMenu extends JPopupMenu {

		public PopupMenu() {
			itemApplyPanelSettings = new JMenuItem("Apply panel settings");
			itemApplyPanelSettings.addActionListener(new MenuSelected());
			add(itemApplyPanelSettings);

			itemWritePanelSettings = new JMenuItem("Write panel settings");
			itemWritePanelSettings.addActionListener(new MenuSelected());
			add(itemWritePanelSettings);
		}
	}

	class MenuSelected implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {

			if (event.getSource() == itemApplyPanelSettings) {
				applyTextAreaPanelSettings();
			} else if (event.getSource() == itemWritePanelSettings) {
				writeTextAreaPanelSettings();
			}
		}
	}

	/**
	 * Apply the settings from the text area and reset the simulation.
	 */
	public void applyTextAreaSettings() {
		String string = fileTextArea.getText();
		if(string.length() > 0)
		{
			Settings settings = new Settings();
			YamlParser parser = new YamlParser(settings);
			parser.parseString(string);
			simulationAnimation.resetAnimation(settings);
		}
	}

	/**
	 * Apply the panel settings from the text area (without resetting the simulation).
	 */
	public void applyTextAreaPanelSettings() {
		String string = fileTextArea.getText();
		if(string.length() > 0)
		{
			Settings settings = new Settings();
			YamlParser parser = new YamlParser(settings);
			parser.parseString(string);
			YamlPanels panels = settings.getYamlPanels();
			if (panels.windowWidth != null && panels.windowHeight != null) {
				parent.web.setSize(panels.windowWidth, panels.windowHeight);
				parent.web.validate();
			}
			if (panels != null) {
				Component component = panels.inflate(panelManager);
				if (component != null) {
					panelManager.replaceMainPanel(component);
					panelManager.setFocus(component);
				}
			} else {
				// ToDo: Warning message? No panel specification provided in Yaml file.
			}
		}
	}

	/**
	 * Append the current panel settings to the text area.
	 */
	public void writeTextAreaPanelSettings() {
		int width = parent.web.getWidth();
		int height = parent.web.getHeight();
		Component component = panelManager.getMainComponent();
		YamlPanels yamlPanels = new YamlPanels(component, width, height);
		YamlPanelWriter panelWriter = new YamlPanelWriter();
		String yamlString = panelWriter.getYamlString(yamlPanels);
		yamlString = "\n\n# Generated panel code:\n" + yamlString;
		fileTextArea.append(yamlString);
	}

	void putPathInPreferences(File file) {
		File directoryPath = file.getParentFile();
		Preferences preferences = UserPreferences.getUserPreferences();
		preferences.put(UserPreferences.DEFAULT_YAML_PATH,
				directoryPath.getPath());
	}

	File getPathFromPreferences() {
		Preferences preferences = UserPreferences.getUserPreferences();
		String directoryPath = preferences.get(
				UserPreferences.DEFAULT_YAML_PATH, null);
		if (directoryPath != null) {
			return new File(directoryPath);
		} else {
			return null;
		}
	}
}
