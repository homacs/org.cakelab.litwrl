package org.cakelab.litwrl.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.config.Config;
import org.cakelab.litwrl.gui.FileEdit.FileVerifier;
import org.cakelab.omcl.gui.ExternalBrowser;
import org.cakelab.omcl.gui.IExternalDownloadDialog;
import org.cakelab.omcl.utils.Md5Sum;
import org.cakelab.omcl.utils.log.Log;

public class ExternalDownloadDialog extends JDialog implements IExternalDownloadDialog, ActionListener, FileVerifier {
	private static final long serialVersionUID = 1L;
	private boolean result;
	private FileEdit file;
	private JButton downloadButton;
	private JButton cancelButton;
	private URL url;
	private String filename;
	private String checksum;
	private ParallelGroup column;
	private SequentialGroup rows;
	private Timer timer;
	private File downloadFolder;
	private LitWRLGUI gui;
	private JButton helpButton;

	public ExternalDownloadDialog(LitWRLGUI gui) {
		super(gui.getFrame());
		this.gui = gui;
		this.setTitle(Launcher.APPLICATION_NAME);
		this.setResizable(false);

	}


	@Override
	synchronized
	public void init(String packageName, String filename, URL _url,
			String checksum) {
		this.url = _url;
		this.filename = filename;
		this.checksum = checksum;
		this.downloadFolder = Launcher.INSTANCE.getDownloadFolder();
		this.timer = new Timer(500, this);

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		column = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
		rows = layout.createSequentialGroup();
		
		rows.addGap(30);
		
		JLabel text = new JLabel();
		
		text.setText("<html><p align=\"center\">We need your assistance to install<br/>"
				+ "<br/><b>" + packageName + "</b><br/><br/>"
				+ "You need to manually download the file<br/>"
				+ "<br/><b>" + filename + "</b><br/><br/>"
				+ "through your internet browser. We will open the page<br/>"
				+ "for you, but you have to find the correct link on the<br/>"
				+ "page and download it. Then you select the file in<br/>"
				+ "the file choser below and we will proceed with installation.<br/>"
				+ "</p><br/></html>");
		addRow(text);
		rows.addGap(30);
		
		text = new JLabel();
		text.setText("1. Click on 'Download' to open the external browser.");
		addRow(text, Alignment.LEADING);

		downloadButton = new JButton("Download");
		Dimension buttonSize = new Dimension(100,30);
		downloadButton.setMinimumSize(buttonSize);
		downloadButton.setPreferredSize(buttonSize);
		downloadButton.addActionListener(this);
		
		addRow(downloadButton);
		
		text = new JLabel();
		text.setText("2. Select the file in your download folder.");
		addRow(text, Alignment.LEADING);
		
		file = FileEdit.create(Launcher.APPLICATION_NAME);
		file.init(downloadFolder.getPath(), this, true);
		addRow(file);
		
		rows.addGap(30);
		helpButton = new JButton("Help!");
		helpButton.setPreferredSize(buttonSize);
		helpButton.setMinimumSize(buttonSize);
		helpButton.addActionListener(this);
		addRow(helpButton);

		
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(buttonSize);
		cancelButton.setMinimumSize(buttonSize);
		cancelButton.addActionListener(this);
		addRow(cancelButton);

		rows.addGap(30);
		
		SequentialGroup horizontal = layout.createSequentialGroup().addGap(30).addGroup(column).addGap(30);
		
		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(rows);

		
		
		add(panel);
		pack();
		
		// center window
		Dimension dim = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
		
		setAutoRequestFocus(true);
		setModal(true);


		result = false;
	}

	private void addRow(JComponent label) {
		column.addComponent(label);
		rows.addComponent(label);
	}

	private void addRow(JComponent label, Alignment horizontalAlignment) {
		column.addComponent(label, horizontalAlignment);
		rows.addComponent(label);
	}

	
	
	public boolean getResult() {
		return result;
	}

	
	private void showWarning(boolean help) {
		Config config = Launcher.INSTANCE.getConfig();
		if (!config.isDontShowAdWarning() || help) {
			config.setDontShowAdWarning(gui.showAdWarning());
			if (config.isModified()) config.save();
		}
		
	}



	/**
	 * This event processing method has to be synchronised 
	 * to protect from concurrent access through timer thread.
	 */
	@Override
	synchronized 
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(downloadButton)) {
			showWarning(false);
			ExternalBrowser.open(url);
		} else if (e.getSource().equals(cancelButton)) {
			result = false;
			setVisible(false);
		} else if (e.getSource().equals(helpButton)) {
			showWarning(true);
		} else if (e.getSource().equals(timer)) {
			File selected = new File(downloadFolder, filename);
			if (selected.exists() && selected.isFile() && Md5Sum.check(selected, checksum)) {
				result = true;
				timer.stop();
				file.setInputVerifier(null);
				file.setSelectedFile(selected.getAbsolutePath());
				file.setInvalid(false);
				Launcher.INSTANCE.updateDownloadFolder(selected.getParent());
				toFront();
				JOptionPane.showMessageDialog(this, "Found it - thank you!", Launcher.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
				setVisible(false);
			}
		}
	}


	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			timer.stop();
		} else {
			timer.start();
		}
		super.setVisible(visible);
	}


	@Override
	public boolean verify(FileEdit f) {
		Log.info("user selected a file");
		result = true;
		File selected = file.getSelectedFile();
		String errorMessage = null;
		if (!selected.exists()) {
			errorMessage = "File or directory does not exist";
			result = false;
		} else if (!selected.isFile()) {
			Launcher.INSTANCE.updateDownloadFolder(selected.getAbsolutePath());
			return true;
		} else if (checksum == null || checksum.length() == 0 || !Md5Sum.check(selected, checksum)) {
			if (!selected.getName().equals(filename)) {
				errorMessage = "Checksum check failed or file has different name.";
			} else if (checksum != null && checksum.length() > 0){
				errorMessage = "Checksum check failed.";
			}
			if (errorMessage != null) {
				result = showChecksumMismatchDialog(errorMessage);
			} else {
				result = true;
			}
		}
		if (result) {
			Launcher.INSTANCE.updateDownloadFolder(selected.getParent());
			timer.stop();
			setVisible(false);
		} else {
			f.setErrorMessage(errorMessage);
		}
		return result;
	}
	
	private boolean showChecksumMismatchDialog(String errorMessage) {
		

		errorMessage = errorMessage + "\n\n" 
				+ "Either this file is corrupted or it was updated and\n"
				+ "the checksum is not yet updated.\n"
				+ "You can retry to download or ignore\n"
				+ "this message and use the downloaded file.";

		
		JButton retry = OptionalPane.createButton("Cancel");
		JButton ignore = OptionalPane.createButton("Ignore");

		JPanel buttons = new JPanel();
		buttons.add(retry);
		buttons.add(ignore);
		
		final OptionalPane pane = new OptionalPane((JFrame)this.getParent(), 
				errorMessage, 
				Launcher.APPLICATION_NAME, 
				OptionalPane.BARE_OPTION, 
				JOptionPane.WARNING_MESSAGE, new JComponent[]{buttons});
		
		retry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setResult(JOptionPane.CANCEL_OPTION);
				pane.setVisible(false);
			}
		});
		
		ignore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setResult(JOptionPane.OK_OPTION);
				pane.setVisible(false);
			}
		});
		
		pane.setVisible(true);
		return (pane.getResult() == JOptionPane.OK_OPTION);
	}


	public File getFile() {
		return file.getSelectedFile();
	}



	
}
