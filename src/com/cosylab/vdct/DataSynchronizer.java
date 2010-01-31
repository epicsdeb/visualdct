/**
 * Copyright (c) 2009, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
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

package com.cosylab.vdct;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.GetDsManager;
import com.cosylab.vdct.events.commands.GetMainComponent;
import com.cosylab.vdct.events.commands.SaveCommand;
import com.cosylab.vdct.graphics.DrawingSurfaceInterface;
import com.cosylab.vdct.graphics.DsManagerInterface;
import com.cosylab.vdct.vdb.VDBData;
import com.cosylab.vdct.vdb.VDBTemplate;

/**
 * <code>DataSynchronizer</code> handles synchronization between loaded files and files on the file
 * system. It checks for unsaved changes or file system changes and brings up the appropriate dialogs.    
 * 
 * @author ssah
 */
public class DataSynchronizer {

	private static DataSynchronizer instance = null;
	private Component dialogParent = null;
	private DsManagerInterface dsManager = null;

	public static DataSynchronizer getInstance() {
		if (instance == null) {
			instance = new DataSynchronizer();
		}
		return instance;
	}

	public DataSynchronizer() {
		super();
		dialogParent = ((GetMainComponent)CommandManager.getInstance().getCommand("GetMainComponent")).getComponent();
		dsManager = ((GetDsManager)CommandManager.getInstance().getCommand("GetDsManager")).getManager();
	}

	public VDBTemplate getTemplate(Object dsId, String templateId) {

		VDBTemplate template = (VDBTemplate)VDBData.getInstance(dsId).getTemplates().get(templateId);
		if (template == null) {
			// Try to find template in another drawing surface.
			DrawingSurfaceInterface[] surfaces = dsManager.getDrawingSurfaces();
			for (int i = 0; i < surfaces.length && template == null; i++) {
				template = (VDBTemplate)VDBData.getInstance(surfaces[i].getDsId()).getTemplates().get(templateId);
			}
			if (template != null) {
				File file = new File(template.getFileName());
				boolean loadSuccessful = false;
				try {
					loadSuccessful = dsManager.getDrawingSurfaceById(dsId).open(file, true);
				} catch (Exception exception) {
					Console.getInstance().println("Failed to load template file '" + template.getFileName() + "'.");	
				}
				if (loadSuccessful) {
					template = (VDBTemplate)VDBData.getInstance(dsId).getTemplates().get(templateId);
				}
			}
		}
		if (template == null) {
			Console.getInstance().println("Could not load template '" + templateId + "'.");
		}
		return template;
	}

	public boolean confirmFileClose(Object dsId, boolean exit) {
		boolean confirmed = true;
		if (dsId != null) {
			if (confirmUnsavedChangesDialog(dsManager.getDrawingSurfaceById(dsId))) {
				showChangedMacrosPortsDialog(dsId);
			} else {
				confirmed = false;
			}
		} else {
			DrawingSurfaceInterface[] surfaces = dsManager.getDrawingSurfaces();
			for (int i = 0; i < surfaces.length && confirmed; i++) {
				confirmed = confirmUnsavedChangesDialog(surfaces[i]);
			}

			if (confirmed) {
				surfaces = dsManager.getDrawingSurfaces();
				String changedTemplateTitles = new String();
				for (int i = 0; i < surfaces.length && confirmed; i++) {
					if (surfaces[i].isTemplateChanged()) {
						changedTemplateTitles = changedTemplateTitles + surfaces[i].getTitle() + "\n";
					}
				}

				if (changedTemplateTitles.length() > 0) {
					String description = "Macros and/or ports in the following templates have changed.\n" + changedTemplateTitles + "Reload and save files that include these templates to apply changes.";
					String title = "Templates changed!";

					if (exit) {
						int selection = JOptionPane.showConfirmDialog(
								dialogParent,
								description + "\n\nAre you sure you want to exit VisualDCT?\n",
								title, JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
						confirmed = selection == JOptionPane.YES_OPTION;
					} else {
						JOptionPane.showMessageDialog(dialogParent,
								description, title, JOptionPane.WARNING_MESSAGE);
					} 
				}
			}
		}
		return confirmed;
	}

	public void checkFilesystemChanges(Object dsId) {

		VDBTemplate template = null; 

		if (dsId != null) {
			template = dsManager.getDrawingSurfaceById(dsId).getTemplate();
		}

		DrawingSurfaceInterface[] surfaces = dsManager.getDrawingSurfaces();
		for (int i = 0; i < surfaces.length; i++) {
			Object id = surfaces[i].getDsId();
			template = dsManager.getDrawingSurfaceById(id).getTemplate();

			if (template != null && newerFileExists(template)) {
				dsManager.setFocusedDrawingSurface(id);
				int selection = JOptionPane.showConfirmDialog(dialogParent,
						"There is a newer file on the file system. Reload?",
						surfaces[i].getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (selection == JOptionPane.YES_OPTION) {
					String fileName = template.getFileName();
					boolean success = false;
					try {
						success = surfaces[i].open(new File(fileName), false);
					} catch (IOException exception) {
						Console.getInstance().println(exception);
					}
					if (!success) {
						Console.getInstance().println("Failed to reload '" + fileName + "'.");
					}
				}
			}

			Vector templatesVector = new Vector(VDBData.getInstance(id).getTemplates().values());
			Iterator templatesIterator = templatesVector.iterator();
			while (templatesIterator.hasNext()) {
				template = (VDBTemplate)templatesIterator.next();
				if (newerFileExists(template)) {
					surfaces[i].reloadTemplate(template);
				}
			}
		}
	}

	private boolean confirmUnsavedChangesDialog(DrawingSurfaceInterface drawingSurface) {

		boolean confirmed = true;

		if (drawingSurface != null && drawingSurface.isModified()) {
			dsManager.setFocusedDrawingSurface(drawingSurface.getDsId());
			int selection = JOptionPane.showConfirmDialog(dialogParent, "The file has been modified. Save changes?",
					drawingSurface.getTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			confirmed = selection != JOptionPane.YES_OPTION;

			if (selection == JOptionPane.YES_OPTION) {

				dialogParent = ((GetMainComponent)CommandManager.getInstance().getCommand("GetMainComponent")).getComponent();

				SaveCommand saveCommand = (SaveCommand)CommandManager.getInstance().getCommand("SaveCommand");
				saveCommand.execute();
				confirmed = saveCommand.isSuccess();
			} else if (selection == JOptionPane.NO_OPTION) {
			} else {
				confirmed = false;
			}
		}
		return confirmed;
	}

	private void showChangedMacrosPortsDialog(Object dsId) {
		if (dsManager.getDrawingSurfaceById(dsId).isTemplateChanged()) {
			dsManager.setFocusedDrawingSurface(dsId);
			JOptionPane.showMessageDialog(dialogParent,
					"Macros and/or ports in this template have changed. \nReload and save files that include this template to apply changes.",
					"Template '" + dsManager.getDrawingSurfaceById(dsId).getTitle() + "' changed!",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private boolean newerFileExists(VDBTemplate template) {
		long storedModificationTime = template.getModificationTime();
		long currentModificationTime = new File(template.getFileName()).lastModified();

		// set the modified date so that the dialog is brought up at most once per file system change
		template.setModificationTime(currentModificationTime);
		return storedModificationTime != 0 && storedModificationTime < currentModificationTime;
	}
}
