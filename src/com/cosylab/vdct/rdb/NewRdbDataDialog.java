/**
 * Copyright (c) 2008, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution. 
 * Neither the name of the Cosylab, Ltd., Control System Laboratory nor the names
 * of its contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cosylab.vdct.rdb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author ssah
 *
 */
public class NewRdbDataDialog extends JDialog implements ActionListener {

	private RdbDataMapper mapper = null;
	private boolean confirmed = false;

	private JTextField dbNameField = null;
	private JTextField versionField = null;
	private JComboBox iocComboBox = null;
	private JTextField descriptionField = null;
	
	private static final String addNewDbString = "Add new db";
	private static final String fileNameString = "File name:";
	private static final String iocString = "Ioc";
	private static final String versionString = "Version";
	private static final String descriptionString = "Description";
	private static final String addString = "Add";
	private static final String cancelString = "Cancel";
	
	/**
	 * @param arg0
	 * @param arg1
	 * @throws HeadlessException
	 */
	public NewRdbDataDialog(RdbDataMapper mapper, JDialog parent) {
		super(parent, true);
		this.mapper = mapper;
        makeGUI();
	}
	
	/**
	 * @return the confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean arg0) {
		try {
			if (arg0) {
				refreshIocCombobox(); 
				setLocationRelativeTo(getParent());			
			}
			super.setVisible(arg0);
			
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(null, exception.getMessage(), "Error loading Ioc data",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void makeGUI() {
		setTitle(addNewDbString);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		getContentPane().add(createContentPanel());
		pack();
	}

	private JPanel createContentPanel() {
		
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setPreferredSize(new Dimension(448, 128));

		JPanel newGroupPanel = createNewGroupPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(newGroupPanel, constraints);

		JPanel buttonsPanel = createButtonsPanel();
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(buttonsPanel, constraints);

		return contentPanel;
	}
	
	private JPanel createNewGroupPanel() {

    	JPanel newGroupPanel = new JPanel(new GridBagLayout());
    	
    	JLabel fileNameLabel = new JLabel(fileNameString);
    	GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(fileNameLabel, constraints);

		dbNameField = new JTextField();
        constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(dbNameField, constraints);
		
    	JLabel versionLabel = new JLabel(versionString);
    	constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(versionLabel, constraints);

		versionField = new JTextField();
        constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(versionField, constraints);
		
    	JLabel iocLabel = new JLabel(iocString);
    	constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(iocLabel, constraints);

		iocComboBox = new JComboBox();
        constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(iocComboBox, constraints);
		
    	JLabel descriptionLabel = new JLabel(descriptionString);
    	constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(descriptionLabel, constraints);

		descriptionField = new JTextField();
        constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		newGroupPanel.add(descriptionField, constraints);
		
		return newGroupPanel;
	}
	
	private JPanel createButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		JButton addNewButton = new JButton(addString);
		addNewButton.setMnemonic(KeyEvent.VK_A);
		addNewButton.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(4, 4, 4, 4);
		buttonsPanel.add(addNewButton, constraints);

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

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		
		if (action.equals(addString)) {
			try {
				RdbDataId dataId = new RdbDataId(dbNameField.getText(), versionField.getText(),
						iocComboBox.getSelectedItem().toString());
				mapper.addRdbDataId(dataId, descriptionField.getText());
				confirmed = true;
				setVisible(false);
			} catch(Exception exception) {
				JOptionPane.showMessageDialog(null, exception.getMessage(), "Database error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (action.equals(cancelString)) {
			confirmed = false;
			setVisible(false);
		} 
	}
	
	private void refreshIocCombobox() throws Exception {
		Iterator iterator = mapper.getIocs().iterator();
		iocComboBox.removeAllItems();
		while (iterator.hasNext()) {
            iocComboBox.addItem(iterator.next());
		}
	}
}
