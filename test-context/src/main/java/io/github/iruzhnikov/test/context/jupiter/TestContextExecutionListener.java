package io.github.iruzhnikov.test.context.jupiter;

import io.github.iruzhnikov.test.context.jupiter.managers.ManagerFabric;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Optional;

/**
 * Listener for correctly stopping context
 */
@Slf4j
public class TestContextExecutionListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        var started = testPlan.countTestIdentifiers(this::startExecution);
        log.info("Started test context executions: {}", started);
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        startExecution(testIdentifier);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        startExecution(testIdentifier);
        getManager(testIdentifier).ifPresent(TestContextManager::startContext);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        stopExecution(testIdentifier);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        stopExecution(testIdentifier);
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        var stopped = testPlan.countTestIdentifiers(this::stopExecution);
        log.info("Stopped test context executions: {}", stopped);
    }

    private boolean startExecution(TestIdentifier testIdentifier) {
        var manager = getManager(testIdentifier);
        if (manager.isPresent()) {
            manager.get().startExecution(testIdentifier.getUniqueId());
            manager.get().registerTestIdentifier(testIdentifier);
            return true;
        }
        return false;
    }

    protected boolean stopExecution(TestIdentifier testIdentifier) {
        return getManager(testIdentifier).map(m -> m.stopExecution(testIdentifier.getUniqueId())).orElse(false);
    }

    protected Optional<TestContextManager> getManager(TestIdentifier testIdentifier) {
        return ManagerFabric.getInstance().getManager(testIdentifier);
    }
}
