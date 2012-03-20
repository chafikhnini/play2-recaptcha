package recaptcha.validator;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.validation.ConstraintValidator;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.F.Promise;

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
	public boolean isValid(Object obj) {

		Map<String, Object> args = play.mvc.Http.Context.current().request().;

		String challenge = (String) args.get("recaptcha_challenge_field");
		String uresponse = (String) args.get("recaptcha_response_field");
		play.mvc.Http.Context.current();

		String remoteAddr = play.mvc.Http.Context.current().request().host();

		Boolean result = checkAnswer(remoteAddr, challenge, uresponse).get();
		return result.booleanValue();
	}

	/**
	 * Constructs a Validator instance.
	 */
	public static play.data.validation.Constraints.Validator<Object> authenticationToken() {
		return new RecaptchaValidator();
	}

	public static Promise<Boolean> checkAnswer(final String remoteAddr, final String challenge, final String uresponse) {

		return Akka.future(new Callable<Boolean>() {
			public Boolean call() {
				ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
				String privatekey = Play.application().configuration().getString(Constants.PRIVATE_KEY);
				if (privatekey == null || privatekey.trim().length() == 0) {
					String msg = "In application.conf, please set property " + Constants.PRIVATE_KEY
							+ " to your site recapcha private key";
					Logger.error(msg);
					return Boolean.FALSE;
				}

				reCaptcha.setPrivateKey(privatekey);

				ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
				return new Boolean(reCaptchaResponse.isValid());
			}
		});

	}

}