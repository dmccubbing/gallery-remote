package com.gallery.GalleryRemote.prefs;

import com.gallery.GalleryRemote.PropertiesFile;
import com.gallery.GalleryRemote.Log;
import com.gallery.GalleryRemote.GalleryRemote;
import com.gallery.GalleryRemote.MainFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.IOException;

/**
 * The Preferences dialog
 * User: paour
 * Date: May 8, 2003
 */

public class PreferencesDialog extends JDialog implements ListSelectionListener, ActionListener {
	public static final String MODULE = "PrefsDlog";

	DefaultListModel panels = new DefaultListModel();
	PropertiesFile oldProperties = null;

	JPanel jPanel1 = new JPanel();
	JScrollPane jScrollPane1 = new JScrollPane();
	JList jIcons = new JList();
	JPanel jPanels = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	CardLayout jPanelsLayout = new CardLayout();
	JPanel jPanel2 = new JPanel();
	JButton jOK = new JButton();
	GridLayout gridLayout1 = new GridLayout();
	JButton jRevert = new JButton();
	JButton jCancel = new JButton();

	public PreferencesDialog(Frame owner) {
		super(owner, true);
		oldProperties = (PropertiesFile) GalleryRemote.getInstance().properties.clone();

		try {
			jbInit();
		} catch(Exception e) {
			Log.logException(Log.ERROR, MODULE, e);
		}

		loadPanes();

		jIcons.setSelectedIndex(0);

		pack();
		Dimension s = owner.getSize();
		setLocation( (int) ( s.getWidth() - getWidth() ) / 2, (int) ( s.getHeight() - getHeight() ) / 2 );

		setVisible( true );
	}

	private void loadPanes() {
		Properties panes = new Properties();
		try {
			panes.load(getClass().getResourceAsStream("panes.properties"));

			int i = 1;
			String className = null;
			while ((className = panes.getProperty("pane." + i++)) != null) {
				try {
					PreferencePanel pp = (PreferencePanel) Class.forName(className).newInstance();

					pp.buildUI();
					pp.readProperties(GalleryRemote.getInstance().properties);

					panels.addElement(pp);

					jPanels.add(className, pp);
				} catch (Exception e) {
					Log.log(Log.ERROR, MODULE, "Bad panel: " + className);
					Log.logException(Log.ERROR, MODULE, e);
				}
			}

			jIcons.setModel(panels);
		} catch (IOException e) {
			Log.logException(Log.ERROR, MODULE, e);
		}
	}

	private void jbInit() throws Exception {
		jPanel1.setLayout(gridBagLayout1);
		jScrollPane1.setAlignmentY((float) 0.5);
		jScrollPane1.setPreferredSize(new Dimension(100, 200));
		jPanels.setLayout(jPanelsLayout);
		this.setTitle("Gallery Remote preferences");
		jOK.setMnemonic('0');
		jOK.setText("OK");
		jPanel2.setLayout(gridLayout1);
		jRevert.setText("Revert");
		jCancel.setText("Cancel");
		gridLayout1.setHgap(5);
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jScrollPane1,      new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jPanels,      new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 0, 10), 0, 0));
		jPanel1.add(jPanel2,   new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		jPanel2.add(jOK, null);
		jScrollPane1.getViewport().add(jIcons, null);
		jPanel2.add(jCancel, null);
		jPanel2.add(jRevert, null);

		jIcons.setCellRenderer(new IconsCellRenderer());
		jIcons.addListSelectionListener(this);
		jOK.addActionListener(this);
		jCancel.addActionListener(this);
		jRevert.addActionListener(this);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	public void valueChanged(ListSelectionEvent e) {
		String className = jIcons.getSelectedValue().getClass().getName();
		Log.log(Log.TRACE, MODULE, "Showing panel: " + className);

		jPanelsLayout.show(jPanels, className);

		pack();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Log.log(Log.INFO, MODULE, "Command selected " + cmd);

		if (cmd.equals("OK")) {
			GalleryRemote.getInstance().properties.uncache();
			
			Enumeration enum = panels.elements();
			while (enum.hasMoreElements()) {
				PreferencePanel pp = (PreferencePanel) enum.nextElement();

				pp.writeProperties(GalleryRemote.getInstance().properties);
			}

			setVisible(false);

			Log.log(Log.TRACE, MODULE, "Updating preferences");
			((MainFrame) getOwner()).readPreferences(oldProperties);
		} else if (cmd.equals("Cancel")) {
			setVisible(false);
		} else if (cmd.equals("Revert")) {
			Enumeration enum = panels.elements();
			while (enum.hasMoreElements()) {
				PreferencePanel pp = (PreferencePanel) enum.nextElement();

				pp.readProperties(GalleryRemote.getInstance().properties);
			}
		}
	}

	/**
	 *  Cell renderer
	 *
	 *@author     paour
	 *@created    11 ao�t 2002
	 */
	public class IconsCellRenderer extends DefaultListCellRenderer
	{
		public IconsCellRenderer() {
			super();
			setHorizontalTextPosition(JLabel.CENTER);
			setVerticalTextPosition(JLabel.BOTTOM);
			setHorizontalAlignment(JLabel.CENTER);
		}

		/**
		 *  Gets the listCellRendererComponent attribute of the FileCellRenderer
		 *  object
		 *
		 *@param  list      Description of Parameter
		 *@param  value     Description of Parameter
		 *@param  index     Description of Parameter
		 *@param  selected  Description of Parameter
		 *@param  hasFocus  Description of Parameter
		 *@return           The listCellRendererComponent value
		 *@since
		 */
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean selected, boolean hasFocus ) {
			super.getListCellRendererComponent( list, value, index, selected, hasFocus );

			if (value != null && index != -1) {
				PreferencePanel pp = (PreferencePanel) value;
				setText(pp.getIcon().getText());
				setIcon(pp.getIcon().getIcon());
			} else {
				setText("dummy");
			}

			return this;
		}
	}
}