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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import com.cosylab.vdct.XmlSettings;

/**
 * @author ssah
 *
 */
public class ConnectionDialog extends JDialog implements ActionListener {

	private RdbDataMapper mapper = null;
	
	private JTextField hostField = null;
	private JTextField databaseField = null;
	private JTextField userField = null;
	private JPasswordField passwordField = null;
	
	private static final String connectString = "Connect";
	private static final String cancelString = "Cancel";

	private static final String hostString = "Host:";
	private static final String databaseString = "Database:";
	private static final String userString = "User:";
	private static final String passwordString = "Password:";

	/**
	 * @param arg0
	 */
	public ConnectionDialog(JFrame arg0, RdbDataMapper mapper) {
        super(arg0, true);

		this.mapper = mapper;
        
        setTitle("Connect to database");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        createGUI();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		if (b) {
			setLocationRelativeTo(getParent());			
		}
		super.setVisible(b);
	}

	/** This method is called from within the constructor to
     * initialize the form.
     */
    private void createGUI() {

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setPreferredSize(new Dimension(448, 128));

    	JPanel connectionPannel = createConnectionPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(connectionPannel, constraints);

    	JPanel buttonPanel = createButtonPanel();
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(buttonPanel, constraints);
        
		getContentPane().add(contentPanel);
		pack();
    }

    private JPanel createConnectionPanel() {
    	
    	JPanel connectionPanel = new JPanel(new GridBagLayout());
    	connectionPanel.setBorder(new TitledBorder("Connect to database"));
    	
    	JLabel hostLabel = new JLabel(hostString);
    	GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(hostLabel, constraints);

    	hostField = new JTextField();
        constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(hostField, constraints);
		
    	JLabel databaseLabel = new JLabel(databaseString);
    	constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(databaseLabel, constraints);

    	databaseField = new JTextField();
        constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(databaseField, constraints);
		
    	JLabel userLabel = new JLabel(userString);
    	constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(userLabel, constraints);

    	userField = new JTextField();
        constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(userField, constraints);
		
    	JLabel passwordLabel = new JLabel(passwordString);
    	constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(passwordLabel, constraints);

    	passwordField = new JPasswordField();
        constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.weightx = .5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		connectionPanel.add(passwordField, constraints);
		
        XmlSettings xmlSettings = XmlSettings.getInstance();
        hostField.setText(xmlSettings.getRdbHost());
        databaseField.setText(xmlSettings.getRdbDatabase());
        userField.setText(xmlSettings.getRdbUser());
        passwordField.setText(xmlSettings.getRdbPassword());
		
		return connectionPanel;
    }


    private JPanel createButtonPanel() {
    	JPanel buttonPanel = new JPanel(new GridBagLayout());
		
    	JButton connectButton = new JButton(connectString); 
    	connectButton.setMnemonic('C');
    	connectButton.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = .5;
		constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(4, 4, 4, 4);
        buttonPanel.add(connectButton, constraints);

    	JButton cancelButton = new JButton(cancelString); 
    	cancelButton.setMnemonic('N');
    	cancelButton.addActionListener(this);
    	
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = .5;
		constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 4, 4, 4);
        buttonPanel.add(cancelButton, constraints);
        
        return buttonPanel;
    }

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals(connectString)) {
			try {
				if (mapper.isConnection()) {
					mapper.closeConnection();
				}
				mapper.setConnectionParameters(hostField.getText(), databaseField.getText(),
						userField.getText(), String.valueOf(passwordField.getPassword()));
				mapper.createNewConnection();
				
				saveSettings();
	        	setVisible(false);
				
			} catch (Exception exception) {
	            JOptionPane.showMessageDialog(this, exception.getMessage(), "Connect error",
	            		JOptionPane.ERROR_MESSAGE);
			}
		} else if (action.equals(cancelString)) {
        	setVisible(false);
		} 
	}
	
	private void saveSettings() {
        XmlSettings xmlSettings = XmlSettings.getInstance();
        xmlSettings.setRdbHost(hostField.getText());
        xmlSettings.setRdbDatabase(databaseField.getText());
        xmlSettings.setRdbUser(userField.getText());
        xmlSettings.setRdbPassword(String.valueOf(passwordField.getPassword()));
	}
}
