package recaptcha.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * This class defined a new validation Annotation
 * 
 * @author orefalo
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = RecaptchaValidator.class)
@play.data.Form.Display(name = "constraint.recaptcha")
public @interface Recaptcha {

	String message() default RecaptchaValidator.message;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
