/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.reactive.function.server;

import org.junit.Test;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author Arjen Poutsma
 */
public class NestedRouteIntegrationTests extends AbstractRouterFunctionIntegrationTests {

	private final RestTemplate restTemplate = new RestTemplate();


	@Override
	protected RouterFunction<?> routerFunction() {
		NestedHandler nestedHandler = new NestedHandler();
		return nest(path("/foo/"),
				route(GET("/bar"), nestedHandler::bar)
						.andRoute(GET("/baz"), nestedHandler::baz))
				.andNest(GET("/{foo}"),
						route(GET("/bar"), nestedHandler::variables).and(
								nest(GET("/{bar}"),
										route(GET("/{baz}"), nestedHandler::variables))))
				.andRoute(GET("/{qux}/quux"), nestedHandler::variables);
	}


	@Test
	public void bar() throws Exception {
		ResponseEntity<String> result =
				restTemplate.getForEntity("http://localhost:" + port + "/foo/bar", String.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("bar", result.getBody());
	}

	@Test
	public void baz() throws Exception {
		ResponseEntity<String> result =
				restTemplate.getForEntity("http://localhost:" + port + "/foo/baz", String.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("baz", result.getBody());
	}

	@Test
	public void variables() throws Exception {
		ResponseEntity<String> result =
				restTemplate.getForEntity("http://localhost:" + port + "/1/2/3", String.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("{foo=1, bar=2, baz=3}", result.getBody());
	}

	// SPR-16868
	@Test
	public void parentVariables() throws Exception {
		ResponseEntity<String> result =
				restTemplate.getForEntity("http://localhost:" + port + "/1/bar", String.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("{foo=1}", result.getBody());

	}

	// SPR 16692
	@Test
	public void removeFailedPathVariables() throws Exception {
		ResponseEntity<String> result =
				restTemplate.getForEntity("http://localhost:" + port + "/qux/quux", String.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("{qux=qux}", result.getBody());

	}


	private static class NestedHandler {

		public Mono<ServerResponse> bar(ServerRequest request) {
			return ServerResponse.ok().syncBody("bar");
		}

		public Mono<ServerResponse> baz(ServerRequest request) {
			return ServerResponse.ok().syncBody("baz");
		}

		public Mono<ServerResponse> variables(ServerRequest request) {
			assertEquals(request.pathVariables(),
					request.attributes().get(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE));

			Mono<String> responseBody = Mono.just(request.pathVariables().toString());
			return ServerResponse.ok().body(responseBody, String.class);
		}

	}

}
