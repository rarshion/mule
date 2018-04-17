/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.internal.dsl.model;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.mule.metadata.api.model.ObjectType;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.construct.ConstructModel;
import org.mule.runtime.api.meta.model.construct.HasConstructModels;
import org.mule.runtime.api.meta.model.operation.HasOperationModels;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.source.HasSourceModels;
import org.mule.runtime.api.meta.model.source.SourceModel;
import org.mule.runtime.api.meta.model.util.ExtensionWalker;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.extension.api.dsl.syntax.DslElementSyntax;
import org.mule.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;

public class ExtensionsHelper {

  private final DslResolvingContext resolvingContext;
  private final Map<ExtensionModel, DslSyntaxResolver> resolvers;
  private Set<ExtensionModel> extensionModels;

  public ExtensionsHelper(Set<ExtensionModel> extensionModels) {
    this.extensionModels = extensionModels;
    this.resolvingContext = DslResolvingContext.getDefault(extensionModels);
    this.resolvers = resolvingContext.getExtensions().stream()
        .collect(toMap(e -> e, e -> DslSyntaxResolver.getDefault(e, resolvingContext)));
  }

  public Object findModel(ComponentIdentifier identifier) {

    // TODO this code is duplicated from ConfigurationBasedElementModelFactory

    Optional<Map.Entry<ExtensionModel, DslSyntaxResolver>> entry =
        resolvers.entrySet().stream()
            .filter(e -> e.getKey().getXmlDslModel().getPrefix().equals(identifier.getNamespace()))
            .findFirst();

    if (!entry.isPresent()) {
      return null;
    }

    ExtensionModel currentExtension = entry.get().getKey();
    DslSyntaxResolver dsl = entry.get().getValue();

    Reference<Object> elementModel = new Reference<>();
    new ExtensionWalker() {

      @Override
      protected void onConfiguration(ConfigurationModel model) {
        final DslElementSyntax elementDsl = dsl.resolve(model);
        getIdentifier(elementDsl).ifPresent(elementId -> {
          if (elementId.equals(identifier)) {
            elementModel.set(model);
            stop();
          }
        });
      }

      @Override
      protected void onConstruct(HasConstructModels owner, ConstructModel model) {
        final DslElementSyntax elementDsl = dsl.resolve(model);
        getIdentifier(elementDsl).ifPresent(elementId -> {
          if (elementId.equals(identifier)) {
            elementModel.set(model);
            stop();
          }
        });
      }

      @Override
      protected void onOperation(HasOperationModels owner, OperationModel model) {
        final DslElementSyntax elementDsl = dsl.resolve(model);
        getIdentifier(elementDsl).ifPresent(elementId -> {
          if (elementId.equals(identifier)) {
            elementModel.set(model);
            stop();
          }
        });
      }

      @Override
      protected void onSource(HasSourceModels owner, SourceModel model) {
        final DslElementSyntax elementDsl = dsl.resolve(model);
        getIdentifier(elementDsl).ifPresent(elementId -> {
          if (elementId.equals(identifier)) {
            elementModel.set(model);
            stop();
          }
        });
      }

    }.walk(currentExtension);

    if (elementModel.get() == null) {
      resolveBasedOnTypes(currentExtension, identifier, dsl)
          .ifPresent(elementModel::set);
    }

    return elementModel.get();

  }

  private Optional<ComponentIdentifier> getIdentifier(DslElementSyntax dsl) {
    if (isNotBlank(dsl.getElementName()) && isNotBlank(dsl.getPrefix())) {
      return Optional.of(builder()
          .name(dsl.getElementName())
          .namespace(dsl.getPrefix())
          .build());
    }

    return empty();
  }

  private Optional<ObjectType> resolveBasedOnTypes(ExtensionModel extensionModel, ComponentIdentifier identifier,
                                                   DslSyntaxResolver dsl) {
    return extensionModel.getTypes().stream()
        .map(type -> resolveBasedOnType(dsl, type, identifier))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }


  private Optional<ObjectType> resolveBasedOnType(DslSyntaxResolver dsl,
                                                  ObjectType type,
                                                  ComponentIdentifier componentIdentifier) {
    Optional<DslElementSyntax> typeDsl = dsl.resolve(type);
    if (typeDsl.isPresent()) {
      Optional<ComponentIdentifier> elementIdentifier = getIdentifier(typeDsl.get());
      if (elementIdentifier.isPresent() && elementIdentifier.get().equals(componentIdentifier)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }


}