/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.deployment.impl.internal.maven;

import static com.vdurmont.semver4j.Semver.SemverType.LOOSE;
import org.mule.tools.api.classloader.model.Artifact;

import com.vdurmont.semver4j.Semver;

import java.io.File;

import org.apache.maven.model.Plugin;

/**
 * Builder for a {@link org.mule.runtime.module.artifact.api.descriptor.ClassLoaderModel} with information from a
 * {@link org.mule.tools.api.classloader.model.ClassLoaderModel} included when packaging the artifact in a heavyweight
 * manner.
 *
 * @since 4.2.0
 */
public class HeavyweightClassLoaderModelBuilder extends ArtifactClassLoaderModelBuilder {

  private static final Semver CLASS_LOADER_MODEL_VERSION_110 = new Semver("1.1.0", LOOSE);

  private org.mule.tools.api.classloader.model.ClassLoaderModel packagerClassLoaderModel;

  public HeavyweightClassLoaderModelBuilder(org.mule.tools.api.classloader.model.ClassLoaderModel packagerClassLoaderModel,
                                            File applicationFolder) {
    super(applicationFolder);
    this.packagerClassLoaderModel = packagerClassLoaderModel;
  }

  /**
   * Exports the shared libraries resources and packages.
   */
  @Override
  protected void doExportSharedLibrariesResourcesAndPackages(Plugin packagingPlugin) {
    if (new Semver(packagerClassLoaderModel.getVersion(), LOOSE).isLowerThan(CLASS_LOADER_MODEL_VERSION_110)) {
      super.doExportSharedLibrariesResourcesAndPackages(packagingPlugin);
    } else {
      exportSharedLibrariesResourcesAndPackages();
    }
  }

  @Override
  protected void doProcessAdditionalPluginLibraries(Plugin packagingPlugin) {
    // Nothing to do as additional plugin libraries are already being defined in the packager class loader model.
  }

  @Override
  public void includeAdditionPluginDependencies() {
    //TODO MULE-15657 use classloader model to get the dependencies by plugin
  }

  /**
   * Exports shared libraries resources and packages getting the information from the packager {@link org.mule.tools.api.classloader.model.ClassLoaderModel}.
   */
  private void exportSharedLibrariesResourcesAndPackages() {
    packagerClassLoaderModel.getDependencies().stream()
        .filter(Artifact::isShared)
        .forEach(
                 sharedDep -> findAndExportSharedLibrary(
                                                         sharedDep.getArtifactCoordinates().getGroupId(),
                                                         sharedDep.getArtifactCoordinates().getArtifactId()));
  }
}
