package cho.carbon.cfg.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.constraints.Min;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 *    标记为 FunctionGroup 规则
 * @author lhb
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface FunctionGroup {
	
	
	@AliasFor(annotation = Component.class)
	String value() default "";
	
	@Min(value = 1, message = "level 必须大于等于1")
	int level() default 1;
}
