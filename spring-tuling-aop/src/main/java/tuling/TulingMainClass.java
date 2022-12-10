package tuling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import tuling.Introductions.ProgramCalculate;

/**
 * Created by xsls on 2019/6/10.
 */
public class TulingMainClass {

	public static void main(String[] args) {

//    	AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MainConfig.class);
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("");
		Calculate calculate = (Calculate) ctx.getBean("tulingCalculate");
		int retVal = calculate.add(2, 4);

        /*ProgramCalculate calculate = (ProgramCalculate) ctx.getBean("tulingCalculate");
        System.out.println(calculate.toBinary(100));*/
	}
}
