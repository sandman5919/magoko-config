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
package org.deepinthink.magoko.config.client.core.config;

public class ConfigClientProperties {
  public static final String PREFIX = "magoko.config.client";
  public static final boolean DEFAULT_ENABLE = false;
  private static final String DEFAULT_SERVER_HOST = "localhost";
  public static final int DEFAULT_SERVER_PORT = 8001;
  public static final String DEFAULT_CONFIG_ROUTE = "config";
  private String serverHost = DEFAULT_SERVER_HOST;
  private int serverPort = DEFAULT_SERVER_PORT;
  private String configRoute = DEFAULT_CONFIG_ROUTE;

  private boolean enable = DEFAULT_ENABLE;

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public String getServerHost() {
    return serverHost;
  }

  public void setServerHost(String serverHost) {
    this.serverHost = serverHost;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public String getConfigRoute() {
    return configRoute;
  }

  public void setConfigRoute(String configRoute) {
    this.configRoute = configRoute;
  }
}
