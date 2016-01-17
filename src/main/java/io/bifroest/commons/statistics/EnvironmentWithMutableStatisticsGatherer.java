package io.bifroest.commons.statistics;

import io.bifroest.commons.statistics.gathering.StatisticGatherer;

public interface EnvironmentWithMutableStatisticsGatherer extends EnvironmentWithStatisticsGatherer {
	void setStatisticGatherer( StatisticGatherer gatherer );
}
