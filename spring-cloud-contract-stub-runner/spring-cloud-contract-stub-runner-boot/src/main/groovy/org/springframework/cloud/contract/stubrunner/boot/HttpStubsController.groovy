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

package org.springframework.cloud.contract.stubrunner.boot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.stubrunner.StubRunning
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author Marcin Grzejszczak
 */
@RestController
@RequestMapping("/stubs")
class HttpStubsController {

	private final StubRunning stubRunning

	@Autowired
	HttpStubsController(StubRunning stubRunning) {
		this.stubRunning = stubRunning
	}

	@RequestMapping
	Map<String, Integer> stubs() {
		return stubRunning.runStubs().toIvyToPortMapping()
	}

	@RequestMapping(path = "/{ivy:.*}")
	ResponseEntity<Integer> stub(@PathVariable String ivy) {
		Integer port = stubRunning.runStubs().getPort(ivy)
		if (port) {
			return ResponseEntity.ok(port)
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND)
	}
}
