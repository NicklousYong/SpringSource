/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.scheduling.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as a candidate for <i>asynchronous</i> execution.
 * Can also be used at the type level, in which case all of the type's methods are
 * considered as asynchronous.该注解可以标记一个异步执行的方法，也可以用来标注类，表示类中的所有方法都是异步执行的。
 *
 * <p>In terms of target method signatures, any parameter types are supported.
 * However, the return type is constrained to either {@code void} or
 * {@link java.util.concurrent.Future}. In the latter case, you may declare the
 * more specific {@link org.springframework.util.concurrent.ListenableFuture} or
 * {@link java.util.concurrent.CompletableFuture} types which allow for richer
 * interaction with the asynchronous task and for immediate composition with
 * further processing steps.入参随意，但返回值只能是void或者Future.(ListenableFuture接口/CompletableFuture类)
 *
 * <p>A {@code Future} handle returned from the proxy will be an actual asynchronous
 * {@code Future} that can be used to track the result of the asynchronous method
 * execution. However, since the target method needs to implement the same signature,
 * it will have to return a temporary {@code Future} handle that just passes a value
 * through: e.g. Spring's {@link AsyncResult}, EJB 3.1's {@link javax.ejb.AsyncResult},
 * or {@link java.util.concurrent.CompletableFuture#completedFuture(Object)}.
 * Future是代理返回的切实的异步返回，用以追踪异步方法的返回值。当然也可以使用AsyncResult类（实现ListenableFuture接口）(Spring或者EJB都有)或者CompletableFuture类
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see AnnotationAsyncExecutionInterceptor
 * @see AsyncAnnotationAdvisor
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {

	/**
	 * A qualifier value for the specified asynchronous operation(s).
	 * <p>May be used to determine the target executor to be used when executing this
	 * method, matching the qualifier value (or the bean name) of a specific
	 * {@link java.util.concurrent.Executor Executor} or
	 * {@link org.springframework.core.task.TaskExecutor TaskExecutor}
	 * bean definition.用以限定执行方法的执行器名称（自定义）：Executor或者TaskExecutor
	 * <p>When specified on a class level {@code @Async} annotation, indicates that the
	 * given executor should be used for all methods within the class. Method level use
	 * of {@code Async#value} always overrides any value set at the class level.
	 *
	 * @since 3.1.2 加在类上表示整个类都使用，加在方法上会覆盖类上的设置
	 */
	String value() default "";

}
