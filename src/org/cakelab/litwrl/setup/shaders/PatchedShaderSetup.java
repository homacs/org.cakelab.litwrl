package org.cakelab.litwrl.setup.shaders;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.PackageDownloadSetup;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.tasks.PatchArchiveTask;
import org.cakelab.omcl.taskman.TaskManager;

/**
 * This setup service implements the setup of a shader
 * which is going to be patched. The package descriptor
 * of the shader must have an entry <code>"hasPatch"</code> 
 * with value <code>"true"</code>.
 * 
 * @author homac
 *
 */
public class PatchedShaderSetup extends ShaderSetup {

	private SetupService litwrPatch;
	private SetupService shader;

	protected PatchedShaderSetup(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		super(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		super.init();
		for (String dependency : descriptor.required) {
			PackageDescriptor dependencyDescriptor = repository.getLocalPackageDescriptorFromLocation(dependency);
			if (dependency.contains("litwr-patch")) {
				litwrPatch = PackageDownloadSetup.getSetupService(setupParams, dependencyDescriptor, repository);
			} else {
				shader = PackageDownloadSetup.getSetupService(setupParams, dependencyDescriptor, repository);
			}
		}
	}

	@Override
	public boolean isDownloaded() {
		return super.isDownloaded() || litwrPatch.isDownloaded() && shader.isDownloaded();
	}

	@Override
	public boolean isBaseInstalled() {
		return super.isBaseInstalled();
	}

	@Override
	public boolean hasUpgrade() {
		return super.hasUpgrade();
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced)
			throws Throwable {
		if (!super.isLocalPackageAvailable()) {
			litwrPatch.scheduleDownloads(taskman, forced);
			shader.scheduleDownloads(taskman, forced);
		}
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force)
			throws Throwable {
		if (!super.isLocalPackageAvailable()) {
			taskman.addSingleTask(new PatchArchiveTask("installing shaders", shader.getPackageRepositoryFile(), litwrPatch.getPackageRepositoryFile(), this.getPackageRepositoryFile()));
		}
		super.scheduleInstalls(taskman, force);
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman,
			SetupService formerVersionSetup) throws Throwable {
		// TODO Auto-generated method stub
		super.scheduleUpgrades(taskman, formerVersionSetup);
	}

	@Override
	public void scheduleRemove(TaskManager taskman) {
		// TODO Auto-generated method stub
		super.scheduleRemove(taskman);
	}

	
}
