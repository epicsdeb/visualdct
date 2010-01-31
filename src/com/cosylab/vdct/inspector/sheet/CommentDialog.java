/**
 * Copyright (c) 2007, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
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

package com.cosylab.vdct.inspector.sheet;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * @author ssah
 */
public class CommentDialog extends JDialog implements ActionListener {

	private JTextArea textArea = null;
	private boolean confirmed = true;

	private static final String okString = "OK";
	private static final String cancelString = "Cancel";

	public CommentDialog(Dialog dialog) {
        super(dialog, true);
		setTitle("Comment");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        createGUI();
    }
	
	/** This method is called from within the constructor to initialize the form.
     */
    private void createGUI() {

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setPreferredSize(new Dimension(320, 240));
		
		JLabel dummyLabel = new JLabel();
		textArea = new JTextArea();
		textArea.setFont(dummyLabel.getFont());
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(scrollPane, constraints);

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

    private JPanel createButtonPanel() {
    	JPanel buttonPanel = new JPanel(new GridBagLayout());
		
    	JButton okButton = new JButton(okString); 
    	okButton.setMnemonic('O');
    	okButton.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = .5;
		constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(4, 4, 4, 4);
        buttonPanel.add(okButton, constraints);

    	JButton cancelButton = new JButton(cancelString); 
    	cancelButton.setMnemonic('C');
    	cancelButton.addActionListener(this);
    	
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = .5;
		constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 4, 4, 4);
        buttonPanel.add(cancelButton, constraints);
        
        return buttonPanel;
    }
    
    public void setComment(String comment) {
    	textArea.setText(comment);
    }

    public String getComment() {
    	return textArea.getText();
    }

	public void actionPerformed(ActionEvent event) {
		confirmed = event.getActionCommand().equals(okString);
       	setVisible(false);
	}

	/**
	 * @return the confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}
}
