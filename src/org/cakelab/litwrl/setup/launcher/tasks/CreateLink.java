package org.cakelab.litwrl.setup.launcher.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;

import org.cakelab.litwrl.Launcher;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.log.Log;

public class CreateLink extends RunnableTask {

	private String link;
	private String targetDir;

	public CreateLink(File link, File targetDir) {
		super("creation of link '" + link.getPath() + "' pointing to '" + targetDir.getPath() + "'", "creating a link");
		this.link = link.getAbsolutePath();
		this.targetDir = targetDir.getAbsolutePath();
	}

	@Override
	public void run() {
		try {
			createLink();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create a link to the new launcher version", e);
		}
	}

	
	

	protected void createLink() throws IOException {
		File l = new File(link);
		File t = new File(targetDir);
		
		
		if (OS.isWindows()) {
			// don't even try to create a symbolic link on windows!
			// years of development and still ..
			copyAndMoveWorkaround(l, t);
		} else {
			boolean trySymbolicLink = false;
			if (l.exists()) {
				if (Files.isSymbolicLink(l.toPath())) {
					trySymbolicLink = true;
				}
				if (!l.delete()) {
					Log.warn("Unable to delete symbolic link " + l + ". We may be in trouble!");
				}
			} else {
				trySymbolicLink = true;
			}
			
			if (trySymbolicLink){
				try {
					Files.createSymbolicLink(l.toPath(), t.toPath());
				} catch (FileSystemException | UnsupportedOperationException e) {
					Log.warn("System denied creation of symbolic link. Trying copy and move instead.");
					// try copy instead
					copyAndMoveWorkaround(l, t);
				}
			} else {
				// we seem to be on a system that does not support symbolic links
				// and it is not Windows.
				// Don't bother the user again with this fact and take the workaround path.
				copyAndMoveWorkaround(l, t);
			}
		}
	}


	private void copyAndMoveWorkaround(File targetDir, File sourceDir) throws IOException {
		File tmpDir = Launcher.INSTANCE.getTempDir();
		FileSystem.cp(sourceDir, tmpDir);
		tmpDir = new File(tmpDir, FileSystem.basename(sourceDir.toString()));
		FileSystem.delete(targetDir);
		FileSystem.mv(tmpDir, targetDir, true);
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
