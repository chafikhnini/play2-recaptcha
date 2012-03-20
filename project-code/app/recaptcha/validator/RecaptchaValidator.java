package recaptcha.validator;

import javax.validation.ConstraintValidator;

import play.api.libs.Crypto;
import play.mvc.Http.Session;

/**
 * This class defined a new Play validator
 * 
 * @author orefalo
 */
public class RecaptchaValidator extends play.data.validation.Constraints.Validator<Object> implements
		ConstraintValidator<Recaptcha, Object> {

	/* Default error message */
	final static public String message = "error.browserid";

	/**
	 * Validator init Can be used to initialize the validation based on
	 * parameters passed to the annotation
	 */
	public void initialize(Recaptcha constraintAnnotation) {
	}

	/**
	 * The validation itself
	 */
	public boolean isValid(Object uuid) {

		Session session = play.mvc.Http.Context.current().session();
		

		if (atoken == null || uuid == null)
			return false;

		
		String sign = Crypto.sign(uuid.toString());
		return atoken.equals(sign);
	}

	/**
	 * Constructs a validator instance.
	 */
	public static play.data.validation.Constraints.Validator<Object> authenticationToken() {
		return new RecaptchaValidator();
	}
}