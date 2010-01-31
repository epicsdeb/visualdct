package com.cosylab.vdct.rdb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.cosylab.vdct.db.DBData;

/** SQLTableGUI
 * GUI for SQLTableModel,
 */
public class RdbDataChooserDialog extends JDialog implements ActionListener {

	// Simple apps call run(), see below
	
	private boolean loadMode = true;
	private RdbDataMapper mapper = null;

	private Object dsId = null;
	private RdbDataId rdbDataId = null;
	private boolean success = false;
	
	private String selectedIoc = null;
	private String selectedGroup = null;
	private String selectedVersion = null;
	private DBData data = null;
	private NewRdbDataDialog newGroupDialog = null;
	
	private JPopupMenu popupMenu = null; 
	
	protected RdbDataTreeModel model;
	public RdbDataTree tree;
	private JButton groupAction = null;

	private static final String addNewString = "Add new";
	private static final String addNewDbString = "Add new db";
	private static final String loadString = "Load";
	private static final String saveString = "Save";
	private static final String loadGroupString = "Load Group";
	private static final String saveGroupString = "Save Group";
	private static final String cancelString = "Cancel";
	
	/**
	 * @param arg0
	 * @param arg1
	 * @throws HeadlessException
	 */
	public RdbDataChooserDialog(RdbDataMapper mapper, JFrame guiContext) {
		super(guiContext, true);
		this.mapper = mapper;
        makeGUI(guiContext);
	}

	/**
	 * @param loadMode the loadMode to set
	 */
	public void setLoadMode(boolean loadMode) {
		this.loadMode = loadMode;
		setTitle(loadMode ? loadGroupString : saveGroupString);
		groupAction.setText(loadMode ? loadString : saveString);
		groupAction.setMnemonic(loadMode ? KeyEvent.VK_L : KeyEvent.VK_S);
	}
	
	/**
	 * @return the data
	 */
	public DBData getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean arg0) {
		if (arg0) {
			createTableModel();
			setLocationRelativeTo(getParent());			
		}
		super.setVisible(arg0);
	}

	public void setDsId(Object dsId) {
		this.dsId = dsId;
	}
	
	public void setRdbDataId(RdbDataId rdbDataId) {
		this.rdbDataId = rdbDataId;
	}
	
	public boolean isSuccess() {
		return success;
	}

	private void makeGUI(JFrame guiContext) {
		setTitle(loadMode ? loadGroupString : saveGroupString);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		getContentPane().add(createContentPanel());
		
		newGroupDialog = new NewRdbDataDialog(mapper, this);
		pack();
	}
	
	private JPanel createContentPanel() {
		
		JPanel contentPanel = new JPanel(new GridBagLayout());

		JPanel groupPanel = createGroupPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(groupPanel, constraints);

		JPanel buttonsPanel = createButtonsPanel();
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(buttonsPanel, constraints);

		return contentPanel;
	}
	
	private JPanel createGroupPanel() {

		JPanel groupPanel = new JPanel(new GridBagLayout());

		JPanel groupButtonsPanel = createGroupButtonsPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(4, 4, 4, 0);
		groupPanel.add(groupButtonsPanel, constraints);
		
		JScrollPane createTablePane = createTreePane();
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 4, 4, 4);
		groupPanel.add(createTablePane, constraints);

		return groupPanel;
	}

	private JPanel createGroupButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		JButton button = new JButton(addNewString);
		button.setMnemonic(KeyEvent.VK_R);
		button.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4, 0, 4, 0);
		buttonsPanel.add(button, constraints);

		return buttonsPanel;
	}
	
	private JScrollPane createTreePane() {
		
		tree = new RdbDataTree();
		tree.addListener(new RdbDataTreeListener() {
			
			public void iocSelected(String iocId) {
				selectedIoc = iocId;
				selectedGroup = null;
				selectedVersion = null;
				groupAction.setEnabled(false);
			}
			public void groupSelected(String iocId, String groupId) {
				selectedIoc = iocId;
				selectedGroup = groupId;
				selectedVersion = null;
				groupAction.setEnabled(false);
			}
			/* (non-Javadoc)
			 * @see com.cosylab.vdct.rdb.group.EpicsGroupTreeListener#versionSelected(java.lang.String, java.lang.String, java.lang.String)
			 */
			public void versionSelected(String iocId, String groupId, String version) {
				selectedIoc = iocId;
				selectedGroup = groupId;
				selectedVersion = version;
				groupAction.setEnabled(true);
			}
		});

		createPopupMenu();
		
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON1) {
					// Double-click performs the main action, load or save.
					if (event.getClickCount() == 2 && selectedGroup != null
							&& selectedIoc != null && selectedVersion != null) {
						performGroupAction();
					}
				} else if (event.getButton() == MouseEvent.BUTTON3) {
					popupMenu.show(tree, event.getX(), event.getY());
				}
			}
		});
	
		JScrollPane pane = new JScrollPane(tree);
		
		pane.setPreferredSize(new Dimension(640, 384));
		return pane;
	}
	
	private JPanel createButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		groupAction = new JButton(loadMode ? loadString : saveString);
		groupAction.setMnemonic(loadMode ? KeyEvent.VK_L : KeyEvent.VK_S);
		groupAction.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(4, 4, 4, 4);
		buttonsPanel.add(groupAction, constraints);

		JButton button = new JButton(cancelString);
		button.setMnemonic(KeyEvent.VK_C);
		button.addActionListener(this);
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(4, 4, 4, 4);
		buttonsPanel.add(button, constraints);

		return buttonsPanel;
	}
	
	private void createPopupMenu() {
		popupMenu = new JPopupMenu();
		
		JMenuItem addItem = new JMenuItem(addNewDbString);
		addItem.addActionListener(this);
		popupMenu.add(addItem);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		
		if (action.equals(addNewString) || action.equals(addNewDbString)) {
			addNewGroup();
		} else if (action.equals(loadString) || action.equals(saveString)) {
			performGroupAction();
		} else if (action.equals(cancelString)) {
			data = null;
			success = false;
			setVisible(false);
		} 
	}
	
	public void createTableModel() {
        
		model = new RdbDataTreeModel(mapper);
		tree.setModel(model);
		groupAction.setEnabled(false);
	}
	
	private void performGroupAction() {
		try {
			rdbDataId.setFileName(selectedGroup);
			rdbDataId.setVersion(selectedVersion);
			rdbDataId.setIoc(selectedIoc);
			
			if (loadMode) {
				data = mapper.loadRdbData(dsId, rdbDataId);
			} else {
				success = mapper.saveRdbData(dsId, rdbDataId);
			}
			setVisible(false);
		} catch (Exception exception) {
			exception.printStackTrace();

			JOptionPane.showMessageDialog(null, exception.getMessage(), "Database error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void addNewGroup() {
		newGroupDialog.setVisible(true);
		if (newGroupDialog.isConfirmed()) {
			createTableModel();
		}
	}
};
