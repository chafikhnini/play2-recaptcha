package recaptcha.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.validation.ConstraintValidator;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.F.Promise;
import play.mvc.Http.Request;

/**
 * This class defined a new Play validator
 * 
 * @author orefalo
 */
public class RecaptchaValidator extends play.data.validation.Constraints.Validator<Object> implements
		ConstraintValidator<Recaptcha, Object> {

	/* Default error message */
	final static public String message = "error.recaptcha";

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

		Request request = play.mvc.Http.Context.current().request();

		Map<String, String> data = requestData(request);

		String challenge = data.get("recaptcha_challenge_field");
		String uresponse = data.get("recaptcha_response_field");

		if (challenge == null || challenge.trim().length() == 0 || uresponse == null || uresponse.trim().length() == 0)
			return false;

		String remoteAddr = request.host();

		// talk to http://www.google.com/recaptcha
		Boolean result = checkAnswer(remoteAddr, challenge, uresponse).get();
		return result.booleanValue();
	}

	/**
	 * Reads the request parameters, there is no methods in Play2 as of now...
	 * 
	 * @param request
	 * @return a map with the request parameters
	 */
	private Map<String, String> requestData(Request request) {

		Map<String, String[]> urlFormEncoded = new HashMap<String, String[]>();
		if (request.body().asFormUrlEncoded() != null) {
			urlFormEncoded = request.body().asFormUrlEncoded();
		}

		Map<String, String[]> queryString = request.queryString();

		Map<String, String> data = new HashMap<String, String>();

		for (String key : urlFormEncoded.keySet()) {
			String[] value = urlFormEncoded.get(key);
			if (value.length > 0) {
				data.put(key, value[0]);
			}
		}

		for (String key : queryString.keySet()) {
			String[] value = queryString.get(key);
			if (value.length > 0) {
				data.put(key, value[0]);
			}
		}

		return data;
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