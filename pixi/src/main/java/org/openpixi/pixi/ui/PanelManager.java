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

import org.openpixi.pixi.ui.panel.AnimationPanel;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.panel.Particle3DPanel;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;

/**
 * Manage various types of panels, including horizontal and vertical splitting of panels.
 *
 */
public class PanelManager {

	private MainControlApplet mainControlApplet;

	private Particle2DPanel particlePanel;
	private Particle3DPanel particle3DPanel;
	private PhaseSpacePanel phaseSpacePanel;
	private ElectricFieldPanel electricFieldPanel;

	private AnimationPanel lastFocusPanel;

	PopupClickListener popupClickListener;

	JMenuItem itemSplitHorizontally;
	JMenuItem itemSplitVertically;
	JMenuItem itemClosePanel;
	JMenuItem itemParticle2DPanel;
	JMenuItem itemParticle3DPanel;
	JMenuItem itemPhaseSpacePanel;
	JMenuItem itemElectricFieldPanel;

	PanelManager(MainControlApplet m) {
		mainControlApplet = m;
	}

	/* Create default panel */
	Component getDefaultPanel() {
		particlePanel = new Particle2DPanel(mainControlApplet.simulationAnimation);
		popupClickListener = new PopupClickListener();
		particlePanel.addMouseListener(popupClickListener);
		setFocus(particlePanel);
		return particlePanel;
	}

	/**
	 * Set focus to particular panel and remove focus from
	 * previously focused panel.
	 * @param component
	 */
	private void setFocus(Component component) {
		if (component instanceof AnimationPanel) {
			AnimationPanel panel = (AnimationPanel) component;
			if (!panel.isFocused()) {
				if (lastFocusPanel != null) {
					lastFocusPanel.setFocus(false);
				}
				panel.setFocus(true);
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
		}
	}

	Component clickComponent;

	class PopupClickListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			// Set focus
			setFocus(e.getComponent());

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
				particlePanel = new Particle2DPanel(mainControlApplet.simulationAnimation);
				component = particlePanel;
			} else if (event.getSource() == itemParticle3DPanel) {
				particle3DPanel = new Particle3DPanel(mainControlApplet.simulationAnimation);
				component = particle3DPanel;
			} else if (event.getSource() == itemPhaseSpacePanel) {
				phaseSpacePanel = new PhaseSpacePanel(mainControlApplet.simulationAnimation);
				component = phaseSpacePanel;
			} else if (event.getSource() == itemElectricFieldPanel) {
				electricFieldPanel = new ElectricFieldPanel(mainControlApplet.simulationAnimation);
				component = electricFieldPanel;
			}
			if (component != null) {
				replacePanel(component);
			}
		}

		private void replacePanel(Component component) {
			component.addMouseListener(popupClickListener);
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
					mainControlApplet.remove(clickComponent);
					mainControlApplet.add(component, BorderLayout.CENTER);
					mainControlApplet.validate();
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

			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();

					JSplitPane s = new JSplitPane(orientation,
								clickComponent, newcomponent);
					s.setOneTouchExpandable(true);
					s.setContinuousLayout(true);
					s.setResizeWeight(0.5);

					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(s);
					} else {
						parentsplitpane.setRightComponent(s);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					JSplitPane s = new JSplitPane(orientation,
							clickComponent, newcomponent);
					s.setOneTouchExpandable(true);
					s.setContinuousLayout(true);
					s.setResizeWeight(0.5);

					mainControlApplet.remove(clickComponent);
					mainControlApplet.add(s, BorderLayout.CENTER);
					mainControlApplet.validate();
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
							mainControlApplet.remove(parentsplitpane);
							mainControlApplet.add(othercomponent, BorderLayout.CENTER);
							mainControlApplet.validate();
						}
						setFocus(othercomponent);
						clickComponent.removeMouseListener(popupClickListener);
						if (clickComponent instanceof AnimationPanel) {
							((AnimationPanel) clickComponent).destruct();
						}
					}
				}
			}
		}
	}

}
