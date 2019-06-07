// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.dicomwebfuse.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class Log4j2LoggerConfigurator {

  public void configureLogger() {
    ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory
        .newConfigurationBuilder();

    builder.setStatusLevel(Level.ERROR);
    LayoutComponentBuilder consoleLayoutBuilder = builder.newLayout("PatternLayout")
        .addAttribute("pattern", "%d{DEFAULT} %-5level - %msg%n%throwable");
    LayoutComponentBuilder fileLayoutBuilder = builder.newLayout("PatternLayout")
        .addAttribute("pattern", "%d{DEFAULT} %-5level %logger{36} - %msg%n%throwable");
    // create a console appender
    AppenderComponentBuilder consoleAppenderBuilder = builder.newAppender("Stdout", "CONSOLE")
        .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
    consoleAppenderBuilder.add(consoleLayoutBuilder);
    builder.add(consoleAppenderBuilder);
    // create a rolling file appender
    ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
        .addComponent(
            builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "15MB"));
    ComponentBuilder defaultStrat = builder.newComponent("DefaultRolloverStrategy")
        .addAttribute("max", 5);
    // create a debug rolling file appender
    AppenderComponentBuilder debugFileAppenderBuilder = builder
        .newAppender("debugRolling", "RollingFile")
        .addAttribute("fileName", "logs/debug.log")
        .addAttribute("filePattern", "logs/debug-%i-%d{dd-MM-yyyy}.log")
        .add(fileLayoutBuilder)
        .addComponent(triggeringPolicy)
        .addComponent(defaultStrat);
    builder.add(debugFileAppenderBuilder);
    // create a info rolling file appender
    AppenderComponentBuilder infoFileAppenderBuilder = builder
        .newAppender("infoRolling", "RollingFile")
        .addAttribute("fileName", "logs/info.log")
        .addAttribute("filePattern", "logs/info-%i-%d{dd-MM-yyyy}.log")
        .add(fileLayoutBuilder)
        .addComponent(triggeringPolicy)
        .addComponent(defaultStrat);
    builder.add(infoFileAppenderBuilder);
    // create the new debug and info logger
    builder.add(builder.newLogger("com.google.dicomwebfuse", Level.DEBUG)
        .add(builder.newAppenderRef("debugRolling"))
        .add(builder.newAppenderRef("infoRolling").addAttribute("Level", Level.INFO)));
    // create root logger
    builder.add(builder.newRootLogger()
        .add(builder.newAppenderRef("Stdout").addAttribute("Level", Level.INFO)));

    Configurator.initialize(builder.build());
  }
}
