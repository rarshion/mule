/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.deployment.impl.internal.plugin;

import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.PLUGIN;
import static org.mule.runtime.deployment.model.api.artifact.ArtifactDescriptorConstants.MULE_LOADER_ID;
import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderModel;
import org.mule.runtime.module.deployment.impl.internal.maven.AbstractMavenClassLoaderModelLoader;
import org.mule.runtime.module.deployment.impl.internal.maven.ArtifactClassLoaderModelBuilder;
import org.mule.runtime.module.deployment.impl.internal.maven.LightweightClassLoaderModelBuilder;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible of returning the {@link BundleDescriptor} of a given plugin's location and also creating a
 * {@link ClassLoaderModel}
 *
 * @since 4.0
 */
public class PluginMavenClassLoaderModelLoader extends AbstractMavenClassLoaderModelLoader {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  public PluginMavenClassLoaderModelLoader(MavenClient mavenClient) {
    super(mavenClient);
  }

  @Override
  public String getId() {
    return MULE_LOADER_ID;
  }

  @Override
  protected void addArtifactSpecificClassloaderConfiguration(ArtifactClassLoaderModelBuilder classLoaderModelBuilder) {
    classLoaderModelBuilder.includeAdditionPluginDependencies();
  }

  @Override
  public boolean supportsArtifactType(ArtifactType artifactType) {
    return artifactType.equals(PLUGIN);
  }

  @Override
  protected boolean includeProvidedDependencies(ArtifactType artifactType) {
    return false;
  }

  @Override
  protected File getClassLoaderModelDescriptor(File artifactFile) {
    return new File(artifactFile.getParent(), CLASSLOADER_MODEL_JSON_DESCRIPTOR);
  }

  @Override
  protected LightweightClassLoaderModelBuilder newLightweightClassLoaderModelBuilder(File artifactFile, MavenClient mavenClient,
                                                                                     Map<String, Object> attributes) {
    final LightweightClassLoaderModelBuilder lightweightClassLoaderModelBuilder =
        new LightweightClassLoaderModelBuilder(artifactFile, mavenClient);
    if (attributes instanceof PluginExtendedClassLoaderModelAttributes) {
      lightweightClassLoaderModelBuilder.setDeployableArtifactDescriptor(((PluginExtendedClassLoaderModelAttributes) attributes)
          .getDeployableArtifactDescriptor());
    }
    return lightweightClassLoaderModelBuilder;
  }
}
