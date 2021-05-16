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

import feign.Feign;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import reactor.core.publisher.Mono;

public class RemoteEnvPostProcessor
    implements EnvironmentPostProcessor, ApplicationListener<ApplicationPreparedEvent>, Ordered {
  private static final DeferredLog logger = new DeferredLog();
  public static final String REMOTE_PROPERTY = "magoko.remote.properties";
  public static final int ORDER = Ordered.LOWEST_PRECEDENCE;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public void onApplicationEvent(ApplicationPreparedEvent applicationPreparedEvent) {
    logger.switchTo(RemoteEnvPostProcessor.class);
  }

  interface RemoveConfigApi {
    @RequestLine("GET /config")
    Map<String, String> requestConfig();
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Mono.just(LocalEnvPostProcessor.LOCAL_PROPERTY)
        .filter(environment.getSystemEnvironment()::containsKey)
        .map(environment.getPropertySources()::get)
        .cast(PropertiesPropertySource.class)
        .map(this::buildConfigClientProperties)
        .filter(ConfigClientProperties::isEnable)
        .map(this::buildRemoveConfigApi)
        .map(RemoveConfigApi::requestConfig)
        .doOnError(logger::error)
        .filter(Map::isEmpty)
        .map(m -> Mono.fromSupplier(Properties::new).doOnNext(p -> p.putAll(m)).block())
        .map(p -> new PropertiesPropertySource(REMOTE_PROPERTY, p))
        .subscribe(environment.getPropertySources()::addLast);
  }

  private RemoveConfigApi buildRemoveConfigApi(ConfigClientProperties ccp) {
    return Feign.builder()
        .encoder(new JacksonEncoder())
        .decoder(new JacksonDecoder())
        .retryer(Retryer.NEVER_RETRY)
        .target(RemoveConfigApi.class, "http://localhost");
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
