package tuling.EalyAopDemo;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import tuling.Calculate;
import tuling.TulingCalculate;

/**
 * Created by xsls on 2019/6/10.
 * <p>
 * * Spring常见代理创建方式
 * * FactoryBean方式，创建单个动态代理： ProxyFactoryBean  1.指定续增强的接口:proxyInterfaces  2.指定需增强的实现类target   3.指定Advice、Advisor、Interceptor都行
 * * autoProxy方式：批量根据advisor自动创建
 * * BeanPostProcessor手动指定Advice方式  BeanNameAutoProxyCreator  1.指定Advice  2.可以使用正则来匹配 bean 的名字
 * * BeanPostProcessor自动扫描Advisor方式  DefaultAdvisorAutoProxyCreator  (不支持注解)、
 * *         AnnotationAwareAspectJAutoProxyCreator（注解）、
 * *         AspectJAwareAdvisorAutoProxyCreator（XML）
 */
//@EnableAspectJAutoProxy
public class EalyAopMainConfig {

	// 被代理对象
	@Bean
	public Calculate tulingCalculate() {
		return new TulingCalculate();
	}

	// Advice 方式
	@Bean
	public TulingLogAdvice tulingLogAdvice() {
		return new TulingLogAdvice();
	}

	// Interceptor方式 ， 类似环绕通知
	@Bean
	public TulingLogInterceptor tulingLogInterceptor() {
		return new TulingLogInterceptor();
	}

	/**
	 * Advisor 种类很多：
	 * RegexpMethodPointcutAdvisor 按正则匹配类
	 * NameMatchMethodPointcutAdvisor 按方法名匹配
	 * DefaultBeanFactoryPointcutAdvisor xml解析的Advisor   <aop:before
	 * InstantiationModelAwarePointcutAdvisorImpl  注解解析的advisor(@Before @After....)
	 *
	 * @return
	 */
	@Bean
	public NameMatchMethodPointcutAdvisor tulingLogAspectAdvisor() {
		NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
		// 通知(Advice)  ：是我们的通知类
		// 通知者(Advisor)：是经过包装后的细粒度控制方式。
		advisor.setAdvice(tulingLogAdvice());
		advisor.setMappedNames("div");
		return advisor;
	}


	// RegexpMethodPointcutAdvisor 按正则匹配类
	@Bean
	public RegexpMethodPointcutAdvisor tulingLogAspectInterceptor() {
		RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor();
		advisor.setAdvice(tulingLogInterceptor());
		advisor.setPattern("tuling.TulingCalculate.*");
		return advisor;
	}


	/**
	 * FactoryBean方式单个： ProxyFactoryBean
	 * @return
	 @Bean public ProxyFactoryBean calculateProxy(){
	 ProxyFactoryBean userService=new ProxyFactoryBean();
	 userService.setInterceptorNames("tulingLogAdvice","tulingLogInterceptor");  // 根据指定的顺序执行
	 userService.setTarget(tulingCalculate());
	 return userService;
	 }
	 */

	/**
	 * FactoryBean方式单个： ProxyFactoryBean
	 * 控制粒度到方法
	 *
	 * @return
	 */
	@Bean
	public ProxyFactoryBean calculateProxy() {
		ProxyFactoryBean userService = new ProxyFactoryBean();
		userService.setInterceptorNames("tulingLogAspect");
		userService.setTarget(tulingCalculate());
		return userService;
	}


	/**
	 *     autoProxy: BeanPostProcessor手动指定Advice方式  BeanNameAutoProxyCreator
	 * @return
	 @Bean public BeanNameAutoProxyCreator autoProxyCreator() {
	 BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
	 //设置要创建代理的那些Bean的名字
	 beanNameAutoProxyCreator.setBeanNames("tuling*");
	 //设置拦截链名字(这些拦截器是有先后顺序的)
	 beanNameAutoProxyCreator.setInterceptorNames("tulingLogInterceptor");
	 return beanNameAutoProxyCreator;
	 }
	 */

	/**
	 * BeanPostProcessor自动扫描Advisor方式  DefaultAdvisorAutoProxyCreator
	 *
	 * @return
	 */
	@Bean
	public DefaultAdvisorAutoProxyCreator autoProxyCreator() {
		return new DefaultAdvisorAutoProxyCreator();
	}


}
