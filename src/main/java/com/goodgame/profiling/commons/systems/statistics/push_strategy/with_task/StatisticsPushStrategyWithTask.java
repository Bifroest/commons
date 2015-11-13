package com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.goodgame.profiling.commons.systems.cron.TaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner.TaskID;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategies;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;


public abstract class StatisticsPushStrategyWithTask<E extends EnvironmentWithStatisticsGatherer>
        implements StatisticsPushStrategy<E> {

    protected E environment;
    private TaskID taskId;
    private final Duration each;
    private final String metricPrefix;
    private final String strategyName;

    public StatisticsPushStrategyWithTask( String metricPrefix, Duration each, String strategyName ) {
        this.each = each;
        this.metricPrefix = metricPrefix;
        this.strategyName = strategyName;
    }

    public void setTaskId(TaskID taskId) {
        this.taskId = taskId;
    }

    @Override
    public final void addRequirements(List<String> destination) {
        addMoreRequirements( destination );
    }

    public void addMoreRequirements(List<String> destination) {
    }

    @Override
    public final void boot(E environment) {
        this.environment = environment;
        StatisticsPushStrategies.<E>enablePeriodicPush( environment, this, metricPrefix, each, strategyName );
    }

    @Override
    public void close() throws IOException {
        TaskRunner.stopTask(taskId);
    }
}
