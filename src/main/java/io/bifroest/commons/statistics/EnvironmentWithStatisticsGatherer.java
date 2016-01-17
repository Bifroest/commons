package io.bifroest.commons.statistics;

import io.bifroest.commons.boot.interfaces.Environment;
import io.bifroest.commons.statistics.gathering.StatisticGatherer;

public interface EnvironmentWithStatisticsGatherer extends Environment {
	StatisticGatherer statisticGatherer();
}
