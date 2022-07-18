package io.github.iruzhnikov.test.context.jupiter;

import io.github.iruzhnikov.test.context.jupiter.context.TestContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultTestContextExtension<T extends TestContext, I extends Injector>
        implements TestContextExtension<T, I> {

}
