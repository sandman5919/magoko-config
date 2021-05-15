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
package org.deepinthink.magoko.config.client.core;

import java.io.IOException;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;

public class LocalEnvPostProcessor implements EnvironmentPostProcessor, Ordered {
  private static final DeferredLog logger = new DeferredLog();
  public static final String LOCAL_PROPERTY = "magoko.local.properties";
  public static final int ORDER = RemoteEnvPostProcessor.ORDER - 1;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    try {
      ClassPathResource lpr = new ClassPathResource("magoko.properties");
      if (lpr.exists()) {
        Properties lpp = new Properties();
        lpp.load(lpr.getInputStream());
        PropertiesPropertySource pps = new PropertiesPropertySource(LOCAL_PROPERTY, lpp);
        environment.getPropertySources().addLast(pps);
      }
    } catch (IOException e) {
      logger.error(e);
    }
  }
}
