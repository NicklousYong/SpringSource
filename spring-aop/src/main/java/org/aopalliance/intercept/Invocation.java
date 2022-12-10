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

package org.aopalliance.intercept;

/**
 * @vlog: 高于生活，源于生活
 * @desc: 类的描述:调用对象接口,一般是用来描述方法调用的
 * @author: xsls
 * @createDate: 2019/7/30 16:00
 * @version: 1.0
 */
public interface Invocation extends Joinpoint {

	/**
	 * 方法实现说明: 获取调用对象参数列表
	 *
	 * @author:xsls
	 * @return: Object[] 调用参数列表
	 * @date:2019/7/30 16:00
	 */
	Object[] getArguments();

}
