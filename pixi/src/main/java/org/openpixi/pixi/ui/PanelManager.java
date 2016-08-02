package org.openpixi.pixi.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.panel.EnergyDensity1DPanel;
import org.openpixi.pixi.ui.panel.EnergyDensity2DPanel;
import org.openpixi.pixi.ui.panel.FocusablePanel;
import org.openpixi.pixi.ui.panel.InfoPanel;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.panel.Particle3DPanel;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;
import org.openpixi.pixi.ui.panel.chart.Chart2DPanel;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity2DGLPanel;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity3DGLPanel;
import org.openpixi.pixi.ui.panel.gl.EnergyDensityVoxelGLPanel;
import org.openpixi.pixi.ui.panel.gl.GaussViolation2DGLPanel;
import org.openpixi.pixi.ui.panel.gl.OccupationNumbers2DGLPanel;
import org.openpixi.pixi.ui.tab.PropertiesTab;

/**
 * Manage various types of panels, including horizontal and vertical splitting of panels.
 *
 */
public class PanelManager {

	private MainControlApplet mainControlApplet;

	private FocusablePanel lastFocusPanel;
	private PropertiesTab propertiesTab;

	private Component mainComponent;

	PopupClickListener popupClickListener = new PopupClickListener();

	JMenuItem itemSplitHorizontally;
	JMenuItem itemSplitVertically;
	JMenuItem itemClosePanel;
	JMenuItem itemParticle2DPanel;
	JMenuItem itemParticle3DPanel;
	JMenuItem itemPhaseSpacePanel;
	JMenuItem itemElectricFieldPanel;
	JMenuItem itemEnergyDensity1DPanel;
	JMenuItem itemEnergyDensity2DPanel;
	JMenuItem itemEnergyDensity2DGLPanel;
	JMenuItem itemEnergyDensity3DGLPanel;
	JMenuItem itemEnergyDensityVoxelGLPanel;
	JMenuItem itemOccupationNumbers2DGLPanel;
	JMenuItem itemChart2DPanel;
	JMenuItem itemGaussViolation2DGLPanel;
	JMenuItem itemInfoPanel;

	public PanelManager(MainControlApplet m) {
		mainControlApplet = m;
	}

	/* Create default panel */
	Component getDefaultPanel() {
		Component component = new Particle2DPanel(mainControlApplet.simulationAnimation);
		setFocus(component);
		return component;
	}

	public Component getMainComponent() {
		return mainComponent;
	}

	/**
	 * Set properties tab which should replace content upon focus.
	 * @param propertiesTab
	 */
	public void setPropertiesTab(PropertiesTab propertiesTab) {
		this.propertiesTab = propertiesTab;
	}

	public SimulationAnimation getSimulationAnimation() {
		return mainControlApplet.simulationAnimation;
	}

	/**
	 * Replace the main content area by a new component.
	 * @param component
	 */
	public void replaceMainPanel(Component component) {
		if (mainComponent != null) {
			mainControlApplet.remove(mainComponent);
		}
		attachMouseListener(component);
		mainControlApplet.add(component, BorderLayout.CENTER);
		mainControlApplet.validate();
		mainComponent = component;
	}

	/**
	 * Split current panel either horizontally or vertically
	 *
	 * @param orientation
	 *            Either JSplitPane.HORIZONTAL_SPLIT or
	 *            JSplitPane.VERTICAL_SPLIT.
	 */
	public Component getSplitPanel(Component leftPanel, Component rightPanel, Integer orientation, Integer dividerLocation) {
		attachMouseListener(leftPanel);
		attachMouseListener(rightPanel);
		JSplitPane splitpane = new JSplitPane(orientation,
				leftPanel, rightPanel);
		splitpane.setOneTouchExpandable(true);
		splitpane.setContinuousLayout(true);
		splitpane.setResizeWeight(0.5);
		if (dividerLocation != null) {
			splitpane.setDividerLocation(dividerLocation);
		}
		return splitpane;
	}

	/**
	 * Attaching the default right mouse button listener,
	 * unless it is a split pane.
	 * @param component
	 */
	private void attachMouseListener(Component component) {
		if (!(component instanceof JSplitPane)) {
			// Only attache mouse listener to "real" panels.
			// Remove listener first, so that it does not get attached twice.
			component.removeMouseListener(popupClickListener);
			component.addMouseListener(popupClickListener);
		}
	}

	/**
	 * Set focus to particular panel and remove focus from
	 * previously focused panel.
	 * @param component
	 */
	public void setFocus(Component component) {
		if (component instanceof JSplitPane) {
			// Set focus on left descendant:
			JSplitPane splitpanel = (JSplitPane) component;
			Component leftComponent = splitpanel.getLeftComponent();
			setFocus(leftComponent);
		} else if (component instanceof FocusablePanel) {
			FocusablePanel panel = (FocusablePanel) component;
			if (!panel.isFocused()) {
				if (lastFocusPanel != null) {
					lastFocusPanel.setFocus(false);
				}
				panel.setFocus(true);
				propertiesTab.refreshContent(panel);
				lastFocusPanel = panel;
			}
		}
	}

	class PopupMenu extends JPopupMenu {

		public PopupMenu() {
			itemSplitHorizontally = new JMenuItem("Split horizontally");
			itemSplitHorizontally.addActionListener(new MenuSelected());
			add(itemSplitHorizontally);

			itemSplitVertically = new JMenuItem("Split vertically");
			itemSplitVertically.addActionListener(new MenuSelected());
			add(itemSplitVertically);

			if (clickComponent != null && clickComponent.getParent() instanceof JSplitPane) {
				itemClosePanel = new JMenuItem("Close panel");
				itemClosePanel.addActionListener(new MenuSelected());
				add(itemClosePanel);
			}

			add(new JSeparator());

			itemParticle2DPanel = new JMenuItem("Particles");
			itemParticle2DPanel.addActionListener(new MenuSelected());
			add(itemParticle2DPanel);

			itemParticle3DPanel = new JMenuItem("Particles 3D");
			itemParticle3DPanel.addActionListener(new MenuSelected());
			add(itemParticle3DPanel);

			itemPhaseSpacePanel = new JMenuItem("Phase space");
			itemPhaseSpacePanel.addActionListener(new MenuSelected());
			add(itemPhaseSpacePanel);

			itemElectricFieldPanel = new JMenuItem("Electric field");
			itemElectricFieldPanel.addActionListener(new MenuSelected());
			add(itemElectricFieldPanel);
			
			itemEnergyDensity1DPanel = new JMenuItem("Energy density");
			itemEnergyDensity1DPanel.addActionListener(new MenuSelected());
			add(itemEnergyDensity1DPanel);

			itemEnergyDensity2DPanel = new JMenuItem("Energy density 2D");
			itemEnergyDensity2DPanel.addActionListener(new MenuSelected());
			add(itemEnergyDensity2DPanel);

			itemEnergyDensity2DGLPanel = new JMenuItem("Energy density 2D (Open GL)");
			itemEnergyDensity2DGLPanel.addActionListener(new MenuSelected());
			add(itemEnergyDensity2DGLPanel);

			itemEnergyDensity3DGLPanel = new JMenuItem("Energy density 3D (Open GL)");
			itemEnergyDensity3DGLPanel.addActionListener(new MenuSelected());
			add(itemEnergyDensity3DGLPanel);

			itemEnergyDensityVoxelGLPanel = new JMenuItem("Energy density Voxel (Open GL)");
			itemEnergyDensityVoxelGLPanel.addActionListener(new MenuSelected());
			add(itemEnergyDensityVoxelGLPanel);

			itemOccupationNumbers2DGLPanel = new JMenuItem("Occupation numbers 2D (Open GL)");
			itemOccupationNumbers2DGLPanel.addActionListener(new MenuSelected());
			add(itemOccupationNumbers2DGLPanel);

			itemChart2DPanel = new JMenuItem("Chart panel");
			itemChart2DPanel.addActionListener(new MenuSelected());
			add(itemChart2DPanel);

			itemGaussViolation2DGLPanel = new JMenuItem("Gauss Violation 2D (Open GL)");
			itemGaussViolation2DGLPanel.addActionListener(new MenuSelected());
			add(itemGaussViolation2DGLPanel);

			itemInfoPanel = new JMenuItem("Info");
			itemInfoPanel.addActionListener(new MenuSelected());
			add(itemInfoPanel);
		}
	}

	Component clickComponent;

	class PopupClickListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			// Set focus
			setFocus(e.getComponent());

			// For convenience, select the properties tab:
			mainControlApplet.tabs.setSelectedComponent(mainControlApplet.propertiesTab);

			// Check for right mouse button
			if (e.isPopupTrigger())
				doPop(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		private void doPop(MouseEvent e) {
			clickComponent = e.getComponent();
			PopupMenu menu = new PopupMenu();
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	class MenuSelected implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			// TODO: This method creates new instances of the panels
			// (which is nice so there can be two identical panels next
			// to each other), but it does not delete previous panels.
			// They should be unregistered in simulationAnimation if not
			// in use anymore.

			Component component = null;

			if (event.getSource() == itemSplitHorizontally) {
				splitPanel(JSplitPane.HORIZONTAL_SPLIT);
			} else if (event.getSource() == itemSplitVertically) {
				splitPanel(JSplitPane.VERTICAL_SPLIT);
			} else if (event.getSource() == itemClosePanel) {
				closePanel();
			} else if (event.getSource() == itemParticle2DPanel) {
				component = new Particle2DPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemParticle3DPanel) {
				component = new Particle3DPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemPhaseSpacePanel) {
				component = new PhaseSpacePanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemElectricFieldPanel) {
				component = new ElectricFieldPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemEnergyDensity1DPanel) {
				component = new EnergyDensity1DPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemEnergyDensity2DPanel) {
				component = new EnergyDensity2DPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemEnergyDensity2DGLPanel) {
				component = new EnergyDensity2DGLPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemEnergyDensity3DGLPanel) {
				component = new EnergyDensity3DGLPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemEnergyDensityVoxelGLPanel) {
				component = new EnergyDensityVoxelGLPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemOccupationNumbers2DGLPanel) {
				component = new OccupationNumbers2DGLPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemChart2DPanel) {
				component = new Chart2DPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemGaussViolation2DGLPanel) {
				component = new GaussViolation2DGLPanel(mainControlApplet.simulationAnimation);
			} else if (event.getSource() == itemInfoPanel) {
				component = new InfoPanel(mainControlApplet.simulationAnimation);
			}

			if (component != null) {
				replacePanel(component);
			}
		}

		private void replacePanel(Component component) {
			attachMouseListener(component);
			Component parent = clickComponent.getParent();
			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();
					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(component);
					} else {
						parentsplitpane.setRightComponent(component);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					replaceMainPanel(component);
				}
			}
			setFocus(component);
		}

		/**
		 * Split current panel either horizontally or vertically
		 *
		 * @param orientation
		 *            Either JSplitPane.HORIZONTAL_SPLIT or
		 *            JSplitPane.VERTICAL_SPLIT.
		 */
		private void splitPanel(int orientation) {
			Component parent = clickComponent.getParent();

			Component newcomponent = getDefaultPanel();
			attachMouseListener(newcomponent);

			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();

					Component splitpane = getSplitPanel(clickComponent, newcomponent, orientation, null);

					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(splitpane);
					} else {
						parentsplitpane.setRightComponent(splitpane);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					Component splitpane = getSplitPanel(clickComponent, newcomponent, orientation, null);

					replaceMainPanel(splitpane);
				}
			}
		}

		private void closePanel() {
			Component parent = clickComponent.getParent();
			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();
					Component parentright = parentsplitpane.getRightComponent();
					Component grandparent = parent.getParent();

					Component othercomponent = parentleft;
					if (parentleft == clickComponent) {
						othercomponent = parentright;
					}

					if (grandparent != null) {
						if (grandparent instanceof JSplitPane) {
							JSplitPane grandparentsplitpane = (JSplitPane) grandparent;
							Component left = grandparentsplitpane.getLeftComponent();
							if (left == parentsplitpane) {
								grandparentsplitpane.setLeftComponent(othercomponent);
							} else {
								grandparentsplitpane.setRightComponent(othercomponent);
							}
						} else if (grandparent instanceof JPanel) {
							parentsplitpane.removeAll();
							replaceMainPanel(othercomponent);
						}
						setFocus(othercomponent);
						clickComponent.removeMouseListener(popupClickListener);
						if (clickComponent instanceof FocusablePanel) {
							((FocusablePanel) clickComponent).destruct();
						}
					}
				}
			}
		}
	}

}
