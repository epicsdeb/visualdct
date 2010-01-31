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

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.cosylab.vdct.db.DBSheetSplitCol;

/**
 * @author ssah
 */
public class CustomSplitDialog extends JDialog implements ActionListener, DocumentListener {
	
	boolean delimiterType = false;

	private JTextArea selectedInstructions = null;

	private JRadioButton delimiterButton = null;
	private JRadioButton patternButton = null;
	
	private JTextField delimiterField = null;
	private JTextField patternField = null;
	private JTextField testDataField = null;
	
	private JTextArea exampleData = null;
	private JLabel examplePatternLabel = null;
	private JTextArea examplePattern = null;
	private JTextArea exampleSplitData = null;
	
	private JTextArea splitResultArea = null;

	private static final String okString = "OK";
	private static final String cancelString = "Cancel";

	private static final String delimiterString = "Delimiter";
	private static final String patternString = "Pattern";
	
	private static final String basicInstructions =
		"Specify the delimiter or the pattern by which to split the column data.";
	
	private static final String delimiterInstructions =
		"The data will be split to parts between the delimiter. The delimiter can be specified by a regular"
		+ " expression.";

	private static final String patternInstructions =
		"The data will be split by the specified pattern. The pattern is a regular expression where parts are" +
		" marked with parentheses.";

	private static final String delimiterExampleData = "#C5 A124    @specs: 1, 6, 18"; 
	private static final String delimiterExamplePatternLabel = "Delimiter:";
	private static final String delimiterExamplePattern = "[ ,:]+";
	private static final String delimiterExampleSplitData = "<#C5> <A124> <@specs> <1> <6> <18>";

	private static final String patternExampleData = "#L5 A12   @params"; 
	private static final String patternExamplePatternLabel = "Pattern:";
	private static final String patternExamplePattern = "#(L\\d+) (A\\d+)[ ]*@(.*)";
	private static final String patternExampleSplitData = "<L5> <A12> <params>";

	private static final String noPatternTestDataInstruction = "Insert delimiter or pattern to split by.";
	private static final String noDataTestDataInstruction = "Insert test data to split.";
	private static final String patternErrorTestDataInstruction = "Syntax error in regular expression:";
	private static final String noMatchTestDataInstruction = "Test data does not match the pattern!";
	private static final String nestedGroupsTestDataInstruction = "Nested groups in pattern are not supported!";
	private static final String starTestDataInstruction = "Split data:";
	
	public CustomSplitDialog(Dialog dialog) {
        super(dialog, true);
		setTitle("Custom split");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        createGUI();
        setDelimiterType(!delimiterType);
        checkPattern();
    }
	
	/** This method is called from within the constructor to
     * initialize the form.
     */
    private void createGUI() {

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setPreferredSize(new Dimension(448, 384));

    	JPanel splitByPanel = createSplitByPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(splitByPanel, constraints);

    	JPanel exmaplePanel = createExamplePanel();
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(exmaplePanel, constraints);
		
    	JPanel testDataPanel = createTestDataPanel();
		constraints = new GridBagConstraints();
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		contentPanel.add(testDataPanel, constraints);
		
	    JPanel buttonPanel = createButtonPanel();
		
		constraints = new GridBagConstraints();
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);
        contentPanel.add(buttonPanel, constraints);
        
		getContentPane().add(contentPanel);
		pack();
    }

    private JPanel createSplitByPanel() {
    	
    	JPanel splitByPanel = new JPanel(new GridBagLayout());
    	splitByPanel.setBorder(new TitledBorder("Split by"));
    	
		JTextArea instructions = new JTextArea(basicInstructions);
		myTextAreaIsLikeAWrappingLabel(instructions);
    	GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 4, 4, 4);
		splitByPanel.add(instructions, constraints);
		
    	JPanel patternPanel = createPatternPanel(); 
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		splitByPanel.add(patternPanel, constraints);

    	selectedInstructions = new JTextArea(delimiterInstructions);
		myTextAreaIsLikeAWrappingLabel(selectedInstructions);
		constraints = new GridBagConstraints();
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);
        splitByPanel.add(selectedInstructions, constraints);
		
		return splitByPanel;
    }

    private JPanel createExamplePanel() {
    	
    	JPanel examplePanel = new JPanel(new GridBagLayout());
    	examplePanel.setBorder(new TitledBorder("Example"));

    	JLabel exampleDataLabel = new JLabel("Data:");
    	GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 4, 4, 4);
		examplePanel.add(exampleDataLabel, constraints);

		exampleData = new JTextArea();
		myTextAreaIsLikeAWrappingLabel(exampleData);
    	constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = 1.;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 4, 4, 4);
		examplePanel.add(exampleData, constraints);

		examplePatternLabel = new JLabel();
    	constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.weightx = .0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		examplePanel.add(examplePatternLabel, constraints);
		
		examplePattern = new JTextArea();
		myTextAreaIsLikeAWrappingLabel(examplePattern);
    	constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		examplePanel.add(examplePattern, constraints);

    	JLabel exampleSplitDataLabel = new JLabel("Split Data:");
    	constraints = new GridBagConstraints();
		constraints.gridy = 2;
		constraints.weightx = .0;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		examplePanel.add(exampleSplitDataLabel, constraints);

    	exampleSplitData = new JTextArea();
    	myTextAreaIsLikeAWrappingLabel(exampleSplitData);
    	constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 4, 4, 4);
		examplePanel.add(exampleSplitData, constraints);
		
		return examplePanel;
    }
    
    private JPanel createTestDataPanel() {
    	
		JPanel testDataPanel = new JPanel(new GridBagLayout());
		testDataPanel.setBorder(new TitledBorder("Test data"));
		
    	JLabel testDataLabel = new JLabel("Test data to split:");
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 4, 4, 4);
		testDataPanel.add(testDataLabel, constraints);
		
    	testDataField = new JTextField();
    	testDataField.getDocument().addDocumentListener(this);
        constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 4, 4, 4);
		testDataPanel.add(testDataField, constraints);

    	splitResultArea = new JTextArea();
    	myTextAreaIsLikeAWrappingLabel(splitResultArea);
		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		testDataPanel.add(splitResultArea, constraints);
		
		return testDataPanel;
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
    
    private JPanel createPatternPanel() {

    	JPanel patternPanel = new JPanel(new GridBagLayout());

	    ButtonGroup splitByGroup = new ButtonGroup();
		
	    delimiterButton = new JRadioButton(delimiterString);
	    delimiterButton.setSelected(true);
	    delimiterButton.addActionListener(this);
	    splitByGroup.add(delimiterButton);
    	GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 0, 4);
		patternPanel.add(delimiterButton, constraints);

	    patternButton = new JRadioButton(patternString);
	    patternButton.addActionListener(this);
	    splitByGroup.add(patternButton);
    	constraints = new GridBagConstraints();
		constraints.gridy = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		patternPanel.add(patternButton, constraints);
		
		delimiterField = new JTextField();
		delimiterField.getDocument().addDocumentListener(this);
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 0, 4);
		patternPanel.add(delimiterField, constraints);
		
		patternField = new JTextField();
		patternField.getDocument().addDocumentListener(this);
		patternField.setEditable(false);
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(4, 4, 4, 4);
		patternPanel.add(patternField, constraints);
		
		return patternPanel;
    }
    
    private void myTextAreaIsLikeAWrappingLabel(JTextArea area) {
    	area.setBackground((Color)UIManager.get("Label.background"));
    	area.setForeground((Color)UIManager.get("Label.foreground"));    	
    	area.setFont((Font)UIManager.get ("Label.font"));
		area.setEditable(false);
		area.setLineWrap(true); 
		area.setWrapStyleWord(true);
    }
    
    public void setSplitData(DBSheetSplitCol data) {
        setDelimiterType(data.isDelimiterType());
        if (delimiterType) {
            delimiterField.setText(data.getPattern());
        } else {
            patternField.setText(data.getPattern());
        }
        checkPattern(); 
    }

    public DBSheetSplitCol getSplitData() {
        String pattern = delimiterType ? delimiterField.getText() : patternField.getText();
    	return (pattern != null && pattern.length() > 0) ? new DBSheetSplitCol(delimiterType, pattern) : null;
    }

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals(okString)) {
        	setVisible(false);
		} else if (action.equals(cancelString)) {
        	delimiterField.setText(null);
        	patternField.setText(null);
        	setVisible(false);
		} else if (action.equals(delimiterString)) {
			setDelimiterType(true);
    		checkPattern();
		} else if (action.equals(patternString)) {
			setDelimiterType(false);
		    checkPattern();
		} 
	}

	public void changedUpdate(DocumentEvent event) {}
	
	public void insertUpdate(DocumentEvent event) {
		checkPattern();
	}
	public void removeUpdate(DocumentEvent event) { 
		checkPattern();
	}
	
	private void setDelimiterType(boolean delimiterType) {
		if (this.delimiterType != delimiterType) {
			this.delimiterType = delimiterType;
			delimiterField.setEditable(delimiterType);
			patternField.setEditable(!delimiterType);

			if (delimiterType) {
				selectedInstructions.setText(delimiterInstructions);
				exampleData.setText(delimiterExampleData);
				examplePatternLabel.setText(delimiterExamplePatternLabel);
				examplePattern.setText(delimiterExamplePattern);
				exampleSplitData.setText(delimiterExampleSplitData);
				delimiterButton.setSelected(true);
			} else {
				selectedInstructions.setText(patternInstructions);
				exampleData.setText(patternExampleData);
				examplePatternLabel.setText(patternExamplePatternLabel);
				examplePattern.setText(patternExamplePattern);
				exampleSplitData.setText(patternExampleSplitData);
				patternButton.setSelected(true);
			}
		}
	}
	
	private void checkPattern() {

		splitResultArea.setForeground(Color.black);
		String pattern = delimiterType ? delimiterField.getText() : patternField.getText();
		if (pattern == null || pattern.length() == 0) {
	    	splitResultArea.setText(noPatternTestDataInstruction);
	    	return;
		}
		
		String testData = testDataField.getText();
		if (testData == null) {
		    testData = "";
		}
		
		DBSheetSplitCol splitData = new DBSheetSplitCol(delimiterType, pattern);
		SplitPropertyGroup splitGroup = new SplitPropertyGroup(testData, splitData);
		
		if (splitGroup.isErrorInPattern()) {
    		splitResultArea.setForeground(Color.red);
	    	splitResultArea.setText(patternErrorTestDataInstruction + " " + splitGroup.getErrorDesc() + "!"); 
	    	return;
		}
		
		if (testData.length() == 0) {
	    	splitResultArea.setText(noDataTestDataInstruction);
	    	return;
		}

		if (!splitGroup.isPatternMatch()) {
    		splitResultArea.setForeground(Color.red);
	    	splitResultArea.setText(noMatchTestDataInstruction);
	    	return;
		}
		if (splitGroup.isNestedGroups()) {
    		splitResultArea.setForeground(Color.red);
	    	splitResultArea.setText(nestedGroupsTestDataInstruction);
	    	return;
		}
		
		StringBuffer buffer = new StringBuffer(starTestDataInstruction + "\t");
		for (int p = 0; p < splitGroup.getPatternParts(); p++) {
		    buffer.append(" <" + splitGroup.getPart(p).getValue() + ">");
		}
    	splitResultArea.setText(buffer.toString());
	}
	
	public void setTestExample(String example) {
	    testDataField.setText(example);
	    checkPattern();
	}
}
