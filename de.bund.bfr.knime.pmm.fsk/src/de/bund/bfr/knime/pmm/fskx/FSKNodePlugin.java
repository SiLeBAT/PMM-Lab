/***************************************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors: Department Biological Safety - BfR
 **************************************************************************************************/
package de.bund.bfr.knime.pmm.fskx;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.core.util.FileUtil;
import org.osgi.framework.BundleContext;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

import de.bund.bfr.knime.pmm.fskx.controller.IRController.RException;
import de.bund.bfr.knime.pmm.fskx.rbin.preferences.RPreferenceInitializer;
import de.bund.bfr.knime.pmm.fskx.controller.RController;

public class FSKNodePlugin extends AbstractUIPlugin {

	// The shared instance.
	private static FSKNodePlugin plugin;

	private static LibRegistry libRegistry;

	/**
	 * This method is called upon plug-in activation.
	 *
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		final String rHome = RPreferenceInitializer.getR3Provider().getRHome();
		if (rHome != null && !rHome.isEmpty()) {
			startLibRegistry();
		}
	}

	public boolean isSetLibRegistry() {
		return libRegistry == null;
	}

	public void startLibRegistry() throws IOException, RException {
		libRegistry = new LibRegistry();
	}

	/**
	 * This method is called when the plug-in is stopped.
	 *
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return Singleton instance of the Plugin
	 */
	public static FSKNodePlugin getDefault() {
		return plugin;
	}

	public boolean isInstalled(final String libraryName) {
		return libRegistry.isInstalled(libraryName);
	}

	public void installLibs(List<String> libNames) throws REXPMismatchException, RException {
		libRegistry.installLibs(libNames);
	}

	public Set<Path> getPaths(List<String> libNames) throws REXPMismatchException, RException {
		return libRegistry.getPaths(libNames);
	}

	private class LibRegistry {

		/**
		 * Installation path
		 */
		private final Path installPath;

		/**
		 * R Path attribute: holds installation path
		 */
		private final String pathAttr;

		/**
		 * R repos attribute: holds remote repository
		 */
		private final String reposAttr;

		/**
		 * R type attribute: holds repository type
		 */
		private final String typeAttr;

		/**
		 * miniCRAN repository path
		 */
		private final Path repoPath;

		private final RController rController;

		/** Utility set to keep count of installed libraries */
		private final Set<String> installedLibs;

		LibRegistry() throws IOException, RException {
			// Create directories
			installPath = FileUtil.createTempDir("install").toPath();
			repoPath = FileUtil.createTempDir("repo").toPath();

			// Create common R attributes
			pathAttr = "path ='" + repoPath.toString().replace("\\", "/") + "'";
			reposAttr = "repos = 'http://cran.us.r-project.org'";
			typeAttr = "type = 'win.binary'";

			// Utility
			installedLibs = new HashSet<>();

			rController = new RController();
			rController.eval("install.packages('miniCRAN'," + reposAttr + ", " + typeAttr + ")");
			rController.eval("library(miniCRAN)");
			rController.eval("makeRepo(c(), " + pathAttr + ", " + reposAttr + ", " + typeAttr + ")");
		}

		private boolean isInstalled(final String libraryName) {
			return installedLibs.contains(libraryName);
		}

		/**
		 * Install a list of libraries into the repository.
		 * 
		 * @param libs
		 *            list of names of R libraries
		 * @throws RException
		 * @throws REXPMismatchException
		 */
		private void installLibs(List<String> libs) throws RException, REXPMismatchException {

			UnaryOperator<String> quoteOperator = astring -> "'" + astring + "'";

			// Creates R package list: c("pkg1", "pkg2", ..., "pkgN")
			List<String> quotedLibs = libs.stream().map(quoteOperator).collect(Collectors.toList());
			String pkgList = "c(" + String.join(",", quotedLibs) + ")";

			// Gets list of R dependencies of libs: c("dep1", "dep2", ...,
			// "depN")
			REXP rexp = rController.eval("pkgDep(" + pkgList + ", availPkgs = cranJuly2014, " + typeAttr + ")");
			List<String> deps = Arrays.asList(rexp.asStrings());
			List<String> quotedDeps = deps.stream().map(quoteOperator).collect(Collectors.toList());
			String depList = "c(" + String.join(",", quotedDeps) + ")";

			// Adds the dependencies to the miniCRAN repository
			rController.eval("addPackage(" + depList + ", " + pathAttr + ", " + reposAttr + "," + typeAttr + ")");

			// Gets the paths to the binaries of these dependencies
			rexp = rController.eval("checkVersions(" + depList + ", " + pathAttr + ", " + typeAttr + ")");
			List<String> paths = Arrays.stream(rexp.asStrings()).map(quoteOperator).collect(Collectors.toList());
			String fileList = "c(" + String.join(",", paths) + ")";

			// Install binaries
			String cmd = "install.packages(" + fileList + ", repos = NULL, lib = '"
					+ installPath.toString().replace("\\", "/") + "', " + typeAttr + ")";
			rController.eval(cmd);

			// Adds names of installed libraries to utility set
			installedLibs.addAll(deps);
		}

		/**
		 * Gets list of paths to the binaries of the desired libraries.
		 * 
		 * @param libs
		 * @return list of paths to the binaries of the desired libraries
		 * @throws RException
		 * @throws REXPMismatchException
		 */
		private Set<Path> getPaths(List<String> libs) throws RException, REXPMismatchException {

			UnaryOperator<String> quoteOperator = astring -> "'" + astring + "'";

			// Gets list of R dependencies of libs
			List<String> quotedLibs = libs.stream().map(quoteOperator).collect(Collectors.toList());
			String libList = "c(" + String.join(",", quotedLibs) + ")";
			REXP rexp = rController.eval("pkgDep(" + libList + ", availPkgs = cranJuly2014, " + typeAttr + ")");

			// Gets the paths to the binaries of these dependencies
			List<String> deps = Arrays.stream(rexp.asStrings()).map(quoteOperator).collect(Collectors.toList());
			String depList = "c(" + String.join(",", deps) + ")";
			rexp = rController.eval("checkVersions(" + depList + ", " + pathAttr + ", " + typeAttr + ")");

			return Arrays.stream(rexp.asStrings()).map(path -> Paths.get(path)).collect(Collectors.toSet());
		}
	}

	public Path getInstallationPath() {
		return libRegistry.installPath;
	}
}
