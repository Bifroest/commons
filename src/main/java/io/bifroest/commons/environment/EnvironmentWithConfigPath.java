package io.bifroest.commons.environment;

import java.nio.file.Path;

import io.bifroest.commons.boot.interfaces.Environment;

public interface EnvironmentWithConfigPath extends Environment {

	Path getConfigPath();

}
