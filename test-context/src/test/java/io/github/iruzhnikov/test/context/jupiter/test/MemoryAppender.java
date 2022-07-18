/*
 * Copyright (c) 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.iruzhnikov.test.context.jupiter.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class MemoryAppender extends ListAppender<ILoggingEvent> {
    public volatile List<ILoggingEvent> list = new CopyOnWriteArrayList<>();

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        this.list.add(iLoggingEvent);
    }

    public void reset() {
        this.list.clear();
    }

    public boolean contains(String string, Level level) {
        return this.list.stream()
                .anyMatch(event -> event.toString().contains(string)
                        && event.getLevel().equals(level));
    }

    public int countEventsForLogger(String loggerName) {
        return (int) this.list.stream()
                .filter(event -> event.getLoggerName().contains(loggerName))
                .count();
    }

    public List<ILoggingEvent> search(String string) {
        return this.list.stream()
                .filter(event -> event.toString().contains(string))
                .collect(Collectors.toList());
    }

    public List<ILoggingEvent> search(String string, Level level) {
        return this.list.stream()
                .filter(event -> event.toString().contains(string)
                        && event.getLevel().equals(level))
                .collect(Collectors.toList());
    }

    public int getSize() {
        return this.list.size();
    }

    public List<ILoggingEvent> getLoggedEvents() {
        return Collections.unmodifiableList(this.list);
    }

    public static MemoryAppender createInstance(Class<?>... loggedClasses) {
        var logAppender = new MemoryAppender();
        var factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        logAppender.setContext(factory);
        Set<Logger> exists = new HashSet<>();
        for (Logger logger : (factory).getLoggerList()) {
            if (exists.add(logger)) {
                logger.setLevel(Level.DEBUG);
                logger.addAppender(logAppender);
            }
        }
        for (Class<?> loggedClass : loggedClasses) {
            var logger = factory.getLogger(loggedClass);
            if (exists.add(logger)) {
                logger.setLevel(Level.DEBUG);
                logger.addAppender(logAppender);
            }
        }
        logAppender.start();
        return logAppender;
    }
}
