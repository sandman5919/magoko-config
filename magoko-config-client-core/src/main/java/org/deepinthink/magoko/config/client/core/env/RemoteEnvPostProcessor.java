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

import static org.deepinthink.magoko.config.client.core.config.ConfigClientProperties.PREFIX;
import static org.deepinthink.magoko.config.client.core.env.LocalEnvPostProcessor.LOCAL_PROPERTY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.deepinthink.magoko.config.client.core.config.ConfigClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
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

  private static final MediaType[] SUPPORTED_TYPES = {
    MediaType.APPLICATION_JSON, new MediaType("application", "*+json")
  };

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Mono.just(LOCAL_PROPERTY)
        .filter(environment.getPropertySources()::contains)
        .map(environment.getPropertySources()::get)
        .cast(PropertiesPropertySource.class)
        .map(this::buildConfigClientProperties)
        .filter(ConfigClientProperties::isEnable)
        .map(this::rSocketRequestConfig)
        .map(m -> Mono.fromSupplier(Properties::new).doOnNext(p -> p.putAll(m)).block())
        .map(p -> new PropertiesPropertySource(REMOTE_PROPERTY, p))
        .doOnError(logger::error)
        .subscribe(pps -> environment.getPropertySources().addBefore(LOCAL_PROPERTY, pps));
  }

  private Map<String, String> rSocketRequestConfig(ConfigClientProperties ccp) {
    ObjectMapper objectMapper = new ObjectMapper();
    return RSocketRequester.builder()
        .rsocketStrategies(
            RSocketStrategies.builder()
                .routeMatcher(new PathPatternRouteMatcher())
                .decoder(new Jackson2JsonDecoder(objectMapper, SUPPORTED_TYPES))
                .encoder(new Jackson2JsonEncoder(objectMapper, SUPPORTED_TYPES))
                .build())
        .connectTcp(ccp.getServerHost(), ccp.getServerPort())
        .block()
        .route(ccp.getConfigRoute())
        .retrieveMono(new ParameterizedTypeReference<Map<String, String>>() {})
        .doOnError(logger::error)
        .block();
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
