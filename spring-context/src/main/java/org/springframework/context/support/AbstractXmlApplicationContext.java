/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Convenient base class for {@link org.springframework.context.ApplicationContext}
 * implementations, drawing configuration from XML documents containing bean definitions
 * understood by an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>Subclasses just have to implement the {@link #getConfigResources} and/or
 * the {@link #getConfigLocations} method. Furthermore, they might override
 * the {@link #getResourceByPath} hook to interpret relative paths in an
 * environment-specific fashion, and/or {@link #getResourcePatternResolver}
 * for extended pattern resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

	private boolean validating = true;


	/**
	 * Create a new AbstractXmlApplicationContext with no parent.
	 */
	public AbstractXmlApplicationContext() {
	}

	/**
	 * Create a new AbstractXmlApplicationContext with the given parent context.
	 *
	 * @param parent the parent context
	 */
	public AbstractXmlApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether to use XML validation. Default is {@code true}.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}


	/**
	 * Loads the bean definitions via an XmlBeanDefinitionReader.
	 *
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	//实现父类抽象的载入Bean定义方法
	//这里新建一个Bean定义读取器 然后init ，之后进入loadBeanDefinitions(beanDefinitionReader);方法
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		//建立一个新的Reader类来读取
		//创建XmlBeanDefinitionReader,即创建Bean读取器
		//并通过回调设置到容器中，容器使用该读取器读取Bean配置资源
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// 为Bean读取器设置Spring资源加载器
		// AbstractXmlApplicationContext 的祖先 AbstractApplicationContext 继承 DefaultResurceLoader
		// 因此容器本身也是一个资源加载器
		beanDefinitionReader.setEnvironment(this.getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
		//当Bean读取器读取Bean定义的XML资源文件时，启用xml校验机制
		//这里为啥要设置呢？不设置不行吗？validating参数有什么作用呢？
		initBeanDefinitionReader(beanDefinitionReader);
		//Bean读取器真正实现加载的方法
		//⭐️
		loadBeanDefinitions(beanDefinitionReader);   // 根据读取器 加载Bean定义
	}
	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context. Default implementation is empty.
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different XmlBeanDefinitionParser implementation.
	 *
	 * @param reader the bean definition reader used by this context
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
	 * method; hence this method is just supposed to load and/or register bean definitions.
	 *
	 * @param reader the XmlBeanDefinitionReader to use
	 * @throws BeansException in case of bean registration errors
	 * @throws IOException    if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */

	//xml Bean读取器加载Bean配置资源
	//在获得之前加载的Resource之后，调用reader.loadBeanDefinitions(configResources)方法加载Bean定义信息
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		//获取Bean配置资源定位
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			//xml Bean读取器调用其父类 AbstractBeanDefinitionReader 读取定位的Bean配置资源
			reader.loadBeanDefinitions(configResources);//加载Bean定义 AbstractBeanDefinitionReader
		}
		//如果子类中获取的Bean配置资源路径定位为空则获取ClassPathXmlApplicationContext 构造方法getConfigLocations方法设置的资源
		//走xml的时候，走的是以下路径
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			//xml Bean读取器调用其父类AbstractBeanDefinitionReader 读取定位的Bean配置资源
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * Return an array of Resource objects, referring to the XML bean definition
	 * files that this context should be built with.
	 * <p>The default implementation returns {@code null}. Subclasses can override
	 * this to provide pre-built Resource objects rather than location Strings.
	 *
	 * @return an array of Resource objects, or {@code null} if none
	 * @see #getConfigLocations()
	 */
	//这里又使用了一个委派模式，在ClassPathXmlApplicationContext中有该方法，调用子类的获取Bean资源的方法
	@Nullable
	protected Resource[] getConfigResources() {
		return null;
	}

}
