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

import static org.deepinthink.magoko.config.client.core.ConfigClientProperties.PREFIX;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class RemoteEnvPostProcessor implements EnvironmentPostProcessor, Ordered {

  public static final String REMOTE_PROPERTY = "magoko.remote.properties";

  public static final int ORDER = Ordered.LOWEST_PRECEDENCE;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    PropertiesPropertySource pps =
        (PropertiesPropertySource)
            environment.getPropertySources().get(LocalEnvPostProcessor.LOCAL_PROPERTY);
    ConfigClientProperties ccp = buildConfigClientProperties(pps);
    if (ccp.isEnable()) {}
  }

  private ConfigClientProperties buildConfigClientProperties(PropertiesPropertySource pps) {
    ConfigClientProperties ccp = new ConfigClientProperties();
    Optional.ofNullable(pps)
        .map(p -> (String) p.getProperty(PREFIX + ".enable"))
        .map(Boolean::parseBoolean)
        .ifPresent(ccp::setEnable);
    return ccp;
  }
}
