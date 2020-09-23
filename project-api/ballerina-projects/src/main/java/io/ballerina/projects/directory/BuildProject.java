/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.directory;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.env.BuildEnvContext;
import io.ballerina.projects.environment.EnvironmentContext;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.BallerinaTomlProcessor;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;
import org.ballerinalang.toml.exceptions.TomlException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * {@code BuildProject} represents Ballerina project instance created from the project directory.
 *
 * @since 2.0.0
 */
public class BuildProject extends Project {

    /**
     * Loads a BuildProject from the provided path.
     *
     * @param projectPath Ballerina project path
     * @return build project
     */
    public static BuildProject loadProject(Path projectPath) {
        Path absProjectPath = Optional.of(projectPath.toAbsolutePath()).get();
        if (!absProjectPath.toFile().exists()) {
            throw new RuntimeException("project path does not exist:" + projectPath);
        }
        if (!isBallerinaProject(absProjectPath)) {
            throw new RuntimeException("provided path is not a valid Ballerina project: " + projectPath);
        }
        return new BuildProject(BuildEnvContext.getInstance(), absProjectPath);
    }

    private BuildProject(EnvironmentContext environmentContext, Path projectPath) {
        super(environmentContext);
        this.sourceRoot = projectPath;

        // load Ballerina.toml
        Path ballerinaTomlPath = this.sourceRoot.resolve(ProjectConstants.BALLERINA_TOML);
        BallerinaToml ballerinaToml;
        try {
            ballerinaToml = BallerinaTomlProcessor.parse(ballerinaTomlPath);
        } catch (IOException | TomlException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // Set default build options
        if (ballerinaToml.getBuildOptions() != null) {
            this.setBuildOptions(ballerinaToml.getBuildOptions());
        } else {
            this.setBuildOptions(new BuildOptions());
        }

        addPackage(projectPath.toString());
    }

    public BuildOptions getBuildOptions() {
        return (BuildOptions) super.getBuildOptions();
    }

    @Nullable
    public Path modulePath(ModuleId moduleId) {
        if (currentPackage().moduleIds().contains(moduleId)) {
            if (currentPackage().getDefaultModule().moduleId() == moduleId) {
                return sourceRoot;
            } else {
                return sourceRoot.resolve(ProjectConstants.MODULES_ROOT).resolve(
                        currentPackage().module(moduleId).moduleName().moduleNamePart());
            }
        }
        return null;
    }

    @Nullable
    public Path documentPath(DocumentId documentId) {
        for (ModuleId moduleId : currentPackage().moduleIds()) {
            Module module = currentPackage().module(moduleId);
            if (module.documentIds().contains(documentId)) {
                return Objects.requireNonNull(modulePath(moduleId)).resolve(module.document(documentId).name());
            }
            if (module.testDocumentIds().contains(documentId)) {
                return Objects.requireNonNull(modulePath(moduleId))
                        .resolve(ProjectConstants.TEST_DIR_NAME).resolve(module.document(documentId).name());
            }
        }
        return null;
    }

    /**
     * Loads a package in the provided project path.
     *
     * @param projectPath project path
     */
    private void addPackage(String projectPath) {
        final PackageConfig packageConfig = PackageLoader.loadPackage(projectPath, false);
        this.addPackage(packageConfig);
    }

    /**
     * Checks if the path is a Ballerina project.
     *
     * @param sourceRoot source root of the project.
     * @return true if the directory is a project repo, false if its the home repo
     */
    private static boolean isBallerinaProject(Path sourceRoot) {
        Path ballerinaToml = sourceRoot.resolve(ProjectConstants.BALLERINA_TOML);
        return Files.isDirectory(sourceRoot)
                && Files.exists(ballerinaToml)
                && Files.isRegularFile(ballerinaToml)
                && (ProjectUtils.findProjectRoot(sourceRoot) == null);
    }

    /**
     * {@code BuildOptions} represents build options specific to a build project.
     */
    public static class BuildOptions extends io.ballerina.projects.BuildOptions {

        private BuildOptions() {}

        public void setObservabilityEnabled(boolean observabilityEnabled) {
            observabilityIncluded = observabilityEnabled;
        }

        public void setSkipLock(boolean skipLock) {
            this.skipLock = skipLock;
        }

        public void setCodeCoverage(boolean codeCoverage) {
            this.codeCoverage = codeCoverage;
        }
    }
}
