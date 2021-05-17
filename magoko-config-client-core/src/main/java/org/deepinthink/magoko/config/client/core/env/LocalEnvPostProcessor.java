/*
 * Copyright 2021-present DEEPINTHINK. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deepinthink.magoko.config.client.core.env;

import java.io.IOException;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Mono;

public class LocalEnvPostProcessor
    implements EnvironmentPostProcessor, ApplicationListener<ApplicationPreparedEvent>, Ordered {
  public static final String LOCAL_PROPERTY = "magoko.local.properties";
  public static final int ORDER = RemoteEnvPostProcessor.ORDER - 1;
  private static final DeferredLog logger = new DeferredLog();
  private static final String LOCAL_CONFIG = "magoko.properties";

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public void onApplicationEvent(ApplicationPreparedEvent applicationPreparedEvent) {
    logger.switchTo(LocalEnvPostProcessor.class);
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Mono.just(LOCAL_CONFIG)
        .map(ClassPathResource::new)
        .filter(ClassPathResource::exists)
        .map(this::loadLocalProperties)
        .map(lpp -> new PropertiesPropertySource(LOCAL_PROPERTY, lpp))
        .subscribe(environment.getPropertySources()::addLast);
  }

  private Properties loadLocalProperties(ClassPathResource lpr) {
    Properties lpp = new Properties();
    try {
      lpp.load(lpr.getInputStream());
    } catch (IOException e) {
      logger.error(e);
    }
    return lpp;
  }
}
