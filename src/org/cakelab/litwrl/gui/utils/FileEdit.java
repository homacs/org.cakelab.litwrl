package org.cakelab.litwrl.gui.utils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cakelab.litwrl.gui.resources.GUIResources;
import org.cakelab.litwrl.gui.utils.notification.DelayedNotificationReceiver;
import org.cakelab.litwrl.gui.utils.notification.JTextFieldChangeNotificationService;
import org.cakelab.omcl.utils.log.Log;

public class FileEdit extends JPanel implements ActionListener, DelayedNotificationReceiver {
	
	public interface FileVerifier {

		boolean verify(FileEdit fileEdit);

	}

	private static final long serialVersionUID = 1149303417732884685L;

	private JTextField folder;
	private JButton button;
	final JFileChooser fc = new JFileChooser();

	private String fileChooserTitle;

	private int selectionMode;
	private String previousFile;

	private JTextFieldChangeNotificationService notificationService;

	private String errorMessage;

	private JLabel errorLabel;

	private SequentialGroup horizontalGroup;

	private ParallelGroup verticalGroup;


	public static FileEdit create(String fileChooserTitle) {
		FileEdit filefield = new FileEdit(fileChooserTitle);
		return filefield;
	}
	
	private FileEdit(String fileChooserTitle) {
		this.fileChooserTitle = fileChooserTitle;
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		
		horizontalGroup = layout.createSequentialGroup();
		verticalGroup = layout.createParallelGroup(Alignment.CENTER);
		
		setLayout(layout);
		
		
		folder = new JTextField();
		
		folder.addActionListener(this);
		
		addRow(folder);
		
		Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
		errorLabel = new JErrorLabel(errorIcon);
		errorLabel.setVisible(false);
		addRow(errorLabel);
		
		button = new JButton();
		try {
			button = GUIUtils.createIconButton(GUIResources.asIcon(GUIResources.FOLDER_ICON));
		} catch (IOException e) {
			button = new JButton();
			button.setText("..");
			button.setFocusable(true);
		}
		button.setToolTipText(fileChooserTitle);
		addRow(button);

		button.addActionListener(this);
		
		

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
		
		notificationService = new JTextFieldChangeNotificationService(this, folder, 500);

	}
	

	
	public void setVisible(boolean visible) {
		notificationService.setEnabled(visible);
		setVisible(visible);
	}
	
	
	private void addRow(JComponent component) {
		horizontalGroup.addComponent(component);
		verticalGroup.addComponent(component);
	}
	
	
	
	@Override
	public void setToolTipText(String text) {
		folder.setToolTipText(text);
		super.setToolTipText(text);
	}

	public void init(String initialFolder, final FileVerifier verifier, boolean files) {
		folder.setText(initialFolder);
		if (!files) {
			selectionMode = JFileChooser.DIRECTORIES_ONLY;
		} else {
			selectionMode = JFileChooser.FILES_ONLY;
		}
		InputVerifier inputVerifier = new InputVerifier(){
			@Override
			public boolean verify(JComponent input) {
				try {
					boolean result = !errorLabel.isVisible();
					if (!folder.getText().equals(previousFile)) {
						previousFile = folder.getText();
						result = verifier.verify(FileEdit.this);
						setInvalid(!result);
					}
					return result;
				} catch (Throwable t) {
					Log.error("received exception while validating directory " + folder.getText(), t);
					setErrorMessage(t.getMessage());
					setInvalid(true);
					return false;
				}
			}
		};
		
		setInputVerifier(inputVerifier);
	}

	@Override
	public void setInputVerifier(InputVerifier inputVerifier) {
		folder.setInputVerifier(inputVerifier);
		super.setInputVerifier(inputVerifier);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(button)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					fc.setDialogTitle(fileChooserTitle);
					fc.setCurrentDirectory(new File(folder.getText()));
					fc.setFileHidingEnabled(false);
					fc.setFileSelectionMode(selectionMode);
					int result = fc.showOpenDialog(FileEdit.this);
					switch(result) {
					case JFileChooser.APPROVE_OPTION:
						setSelectedFile(fc.getSelectedFile().getAbsolutePath());
						getInputVerifier().verify(folder);
						break;
					case JFileChooser.ERROR_OPTION:
						Log.warn("Folder selection dialog returned error code.");
					case JFileChooser.CANCEL_OPTION:
						break;
					}
				}
				
			});
		} else if (e.getSource().equals(folder)) {
			getInputVerifier().verify(this);
		}
	}

	public void setInvalid(boolean invalid) {
		if (invalid) {
			folder.setForeground(Color.red);
			showErrorIcon();
		} else {
			folder.setForeground(Color.black);
			hideErrorIcon();
		}
	}

	private void hideErrorIcon() {
		errorLabel.setToolTipText(null);
		errorLabel.setVisible(false);
	}

	private void showErrorIcon() {
		errorLabel.setToolTipText(GUIUtils.createMultilineTooltip(errorMessage));
		errorLabel.setVisible(true);
	}

	public File getSelectedFile() {
		return new File(folder.getText());
	}

	public void setEditable(boolean editable) {
		folder.setEditable(editable);
		button.setEnabled(editable);
		notificationService.setEnabled(editable);
	}

	public void setSelectedFile(String selectedFile) {
		if (selectedFile != null && !selectedFile.equals(folder.getText())) {
			folder.setText(selectedFile);
		}
	}

	@Override
	public void delayedNotification(JComponent component) {
		InputVerifier verifier = getInputVerifier();
		if (verifier != null) verifier.verify(this);
	}

	public void setErrorMessage(String error) {
		this.errorMessage = error;
	}


}
