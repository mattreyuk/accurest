/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

String console = "CONSOLE"
String logPattern = "%d{yyyy-MM-dd HH:mm:ss.SSSZ, Europe/Warsaw} | %-5level | %X{correlationId} | %thread | %logger{1} | %m%n"

appender(console, ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = logPattern
    }
}

root(INFO, [console])
logger("org.springframework.cloud.contract.verifier", DEBUG)
