package recaptcha.validator;

import static play.libs.Akka.future;

import java.util.concurrent.Callable;

import javax.validation.ConstraintValidator;

import play.Logger;
import play.Play;
import play.api.libs.Crypto;
import play.api.mvc.Request;
import play.libs.Akka;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.templates.ScalaTemplateCompiler.Params;

/**
 * This class defined a new Play validator
 * 
 * @author orefalo
 */
public class RecaptchaValidator extends
		play.data.validation.Constraints.Validator<Object> implements
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
	 * Constructs a Validator instance.
	 */
	public static play.data.validation.Constraints.Validator<Object> authenticationToken() {
		return new RecaptchaValidator();
	}

	public static Promise<Boolean> checkAnswer(final String remoteAddr, final String challenge, final String uresponse) {
		
		
		return		Akka.future(
				  new Callable<Boolean>() {
				    public Boolean call() {
				    	ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
						String privatekey = Play.application().configuration().getString(Constants.PRIVATE_KEY );
						if (privatekey == null || privatekey.trim().length() == 0 ) {
							String msg = "In application.conf, please set property "+Constants.PRIVATE_KEY+" to your site recapcha private key";
							Logger.error(msg);
							return Boolean.FALSE;
						}
							
						reCaptcha.setPrivateKey(privatekey);

						ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
						return new Boolean(reCaptchaResponse.isValid());
				    }
				  }
		
		
	
		
	}

	public static Boolean checkAnswer() {
		return async(future(new Callable<Boolean>() {
			public Boolean call() {
				ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(
						remoteAddr, challenge, uresponse);
				return longComputation();
			}
		}).map(new Function<Integer, Result>() {
			public Result apply(Integer i) {
				return ok("Got " + i);
			}
		}));
	}

	public static boolean checkAnswer(Request request, Params params) {

		ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
		String privatekey = Play.configuration.getProperty(
				"ugot.recaptcha.privateKey", YOUR_RECAPTCHA_PRIVATE_KEY);
		if (privatekey == null || privatekey.trim().length() == 0
				|| YOUR_RECAPTCHA_PRIVATE_KEY.equals(privatekey))
			return false;

		reCaptcha.setPrivateKey(privatekey);
		String challenge = params.get("recaptcha_challenge_field");
		String uresponse = params.get("recaptcha_response_field");

		ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr,
				challenge, uresponse);
		return reCaptchaResponse.isValid();
	}

}