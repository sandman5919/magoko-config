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
package org.deepinthink.magoko.config.server.single;

import org.deepinthink.magoko.boot.core.MagOKOBanner;
import org.deepinthink.magoko.config.server.core.EnableConfigServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
  public static void main(String[] args) throws Exception {
    new SpringApplicationBuilder()
        .sources(ConfigServerApplication.class)
        .banner(
            MagOKOBanner.builder()
                .tag("Config Server Standalone Application")
                .tag("For more information, please visit our website:")
                .tag("\thttps://maogko.deepinthink.org/magoko-config")
                .build())
        .run(args);
  }
}
