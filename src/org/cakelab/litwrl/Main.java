package org.cakelab.litwrl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.omcl.ContextClassLoader;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.setup.Version;
import org.cakelab.omcl.setup.VersionStd;
import org.cakelab.omcl.update.UpdateServer;
import org.cakelab.omcl.utils.Classpath;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.Md5Sum;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.log.ConsoleLog;
import org.cakelab.omcl.utils.log.Log;

public class Main {
	
	public static void main(String [] args) {
		checkJava();
		
		checkClassLoader(args);
		
		Log.addLogListener(new ConsoleLog());
		
		// Bootstrap into latest available launcher if not already running
		launchCurrent();
		
		// if this is not the bootstrapping process then proceed with initialisation of the launcher
		Launcher launcher = Launcher.create();
		launcher.run();
	}
	
	private static void checkJava() {
		String errorPrefix ="The mod-pack v1.1 requires Oracle Java SE v1.8 or higher.<br/>";
		String downloadSuggestion = "<br/><p>Please locate an appropriate Java installation<br/>"
				+ "on your system or download it from:</p><br/>"
				+ "<a href=\"http://java.oracle.com\">http://java.oracle.com/</a>";
		String userhome = System.getProperty("user.home");
		System.out.println("user.home: "+ userhome);
		String vendor = System.getProperty("java.vendor");
		String version = System.getProperty("java.version");
		System.out.println("vendor: " + vendor);
		
		version = version.split("[^0-9\\.]")[0];
		System.out.println("version: " + version);
		Version v = VersionStd.decode(version);
		
		Version required = new VersionStd((byte)1,(byte)8,(byte)0);
		boolean hasBug = false;
		try {
			new JFileChooser();
		} catch (Throwable e) {
			errorPrefix ="Unfortunately, there is a bug in Java JDK with user names<br/>"
					+ "containing special characters like yours.<br/><br/>"
					+ "Use Oracle Java SE 1.8 <b>JRE</b> instead.";
			hasBug = true;
			System.err.println("version has JDK bug");
		}

		String javahome = System.getProperty("java.home");
		System.out.println("java.home: " + javahome);

		boolean passed = !hasBug;
		File javaexefile = null;
		String javaexe = "not found";
		try {
			javaexefile = OS.getJavaExecutable();
			javaexe = javaexefile.getAbsolutePath();
		} catch (IOException e) {
			passed = false;
		}
		System.out.println("java: " + javaexe);

		if (!passed || !vendor.toLowerCase().contains("oracle") || !v.isGreaterEqual(required)) {
			int reply = JOptionPane.showConfirmDialog(null, "<html>" + errorPrefix + "<br/><br/>"
					+ downloadSuggestion 
					+ "<br/><br/>Ignore warning and proceed anyway?"
					+ "</html>", Launcher.APPLICATION_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (reply != JOptionPane.YES_OPTION) System.exit(-3);
		}

	}

	private static void checkClassLoader(String[] args) {
		if (!(Main.class.getClassLoader() instanceof ContextClassLoader)) {
			System.err.println("You are not intended to call this class's main method directly.\nUse org.cakelab.litwrl.Boot.main(..) instead.");
			
			System.exit(1);
		}
	}

	private static void launchCurrent() {
		// TODO: this is all crap, but we keep it for now
		File location;
		try {
			location = Classpath.determineClasspathEntry(Main.class).getDirectory();
			
			String str = FileSystem.getStandardisedPath(location);
			
			File configDir = getConfigDir();
			if (!configDir.exists()) {
				configDir.mkdirs();
			}
			
			if (str.endsWith("bin")) {
				// debugging option
				return;
			} else if (str.endsWith(Launcher.LAUNCHER_REPOSITORY_LOCATION)) {
				// running as launcher
				return;
			} else {
				// running as bootstrap
				Log.info("bootstrap executing in " + location.getAbsolutePath());
				File runningJar = new File(location,  Launcher.LAUNCHER_JAR_FILE);
				File repositoryDir = new File(configDir, Launcher.REPOSITORY_FOLDER);
				
				File jar = null;
				if (repositoryDir.exists()) {
					try {
						Log.info("looking up latest launcher version");
						VersionStd myVersion = VersionStd.decode(Launcher.LAUNCHER_VERSION);
						LitWRLRepository repository = new LitWRLRepository(new UpdateServer(), repositoryDir);
						File versionsFile = repository.getLocalVersionsFile(Launcher.getPackageDescriptor());
						Versions versions = Versions.load(versionsFile);
						String latest = versions.getLatestVersionString();
						/*
						 * Do not boot into older launchers.
						 */
						if (latest != null && VersionStd.decode(latest).isGreaterEqual(myVersion)) {
							File pf = repository.getLocalPackageDescriptorFileFromLocation(versions.getLatestVersionLocation());
							jar = new File (pf.getParentFile(), Launcher.LAUNCHER_JAR_FILE);
							//
							// check integrity and accessibility
							//
							PackageDescriptor descriptor = PackageDescriptor.load(pf);
							if (descriptor != null && descriptor.checksum != null && descriptor.checksum.length() > 0) {
								if (!Md5Sum.check(jar, descriptor.checksum)) {
									Log.info("checksum check on " + jar.getAbsolutePath() + "' failed.");
									jar = null;
								}
							}
						} else {
							Log.info("Bootstrap launcher is newer then the one in repository");
						}
					} catch (Throwable e) {
						// fixing later
						jar = null;
					}
				}

				if (jar == null || !jar.exists()) {
					Log.info("initialising working directory at " + configDir.getAbsolutePath());
					File versionDir = new File(repositoryDir, Launcher.LAUNCHER_REPOSITORY_LOCATION);
					Log.info("creating first version at " + versionDir.getAbsolutePath());
					jar = new File(versionDir, Launcher.LAUNCHER_JAR_FILE);
					versionDir.mkdirs();
					FileSystem.cp(runningJar, jar);
				}
				
				/* 
				 * The following lines have been introduced to fix a bug in launchers of versions <= 1.2.0.
				 * 
				 * NOW IT IS PERMANENT and actually a feature in case someone unintentionally 
				 * removed the file.
				 */
				File versionDir = new File(repositoryDir, Launcher.LAUNCHER_REPOSITORY_LOCATION);
				File versionsFile = new File(versionDir.getParentFile(), LitWRLRepository.VERSIONS_FILE);
				if (!versionsFile.exists()) {
					Log.info("adding versions file for launcher");
					Versions versions = new Versions();
					int index = versions.addAvailable(Launcher.LAUNCHER_REPOSITORY_LOCATION);
					versions.setLatest(index);
					versions.save(versionsFile);
				}

				launch(configDir, jar);
			}
		} catch (IOException e) {
			Log.fatal("Failed to determine launcher directory.", e);
		} catch (JSONCodecException e) {
			Log.fatal("Failed to create initial versions file.", e);
		}
	}


	private static void launch(File workDir, File jar) {
		Log.info("booting into " + jar.getAbsolutePath());
		int result = 255;
		try {
			File javaCmd = OS.getJavaExecutable();
	
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO();
			ArrayList<String> commandline = new ArrayList<String>();
			commandline.add(javaCmd.toString());
			commandline.add("-jar");
			commandline.add(jar.getAbsolutePath());
			pb.command(commandline);
			pb.directory(workDir);
			pb.inheritIO();
			Log.info("starting " + commandline);
			Process p = pb.start();
			result  = p.waitFor();
			if (result != 0) {
				Log.error("Launcher exited with " + result);
			}
		} catch (Throwable e) {
			Log.error("Error executing launcher", e);
		}
		System.exit(result);
	}





	public static File getConfigDir() {
		return new File(FileSystem.getUserHome(), Launcher.CONFIG_DIR_NAME);
	}

}
