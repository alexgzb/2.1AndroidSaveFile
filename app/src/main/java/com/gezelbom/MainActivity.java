package com.gezelbom;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

/**
 * Main Activity for the prime finder. Prime finder is a super simple app that
 * checks for prime numbers and prints last found prime to the screen and also
 * saves that value in the shared preferences (sp) file so that it can continue
 * from this number when the app starts the next time. When the maximum value
 * has been reached the sp file is cleared and the finder stops looking for more
 * primes.
 * Uses Handler to print to the GUI thread.
 * 
 * @author Alex Gezelbom
 * 
 */
public class MainActivity extends ActionBarActivity {

	long current;
	SharedPreferences sb = null;
	Editor spedit = null;
	final String TAG = "primeMain";
	TextView tv = null;
	TextView tv2 = null;
	Handler handler;
	final long MAX = Long.MAX_VALUE;
	Thread thread;

	/**
	 * OnCreate method starts the app
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialise the variables
		tv = (TextView) findViewById(R.id.TextView1);
		tv2 = (TextView) findViewById(R.id.TextView2);
		sb = getPreferences(MODE_PRIVATE);
		spedit = sb.edit();

		// Initialise the handler and override the handleMessage method
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				// If the bundle received does not contain a String with the key
				// max
				if (msg.getData().get("max") == null) {

					// Store the value received in Shared prefs sp file
					// and print it to the the logcat debug and the textView
					Long current;
                    current = (Long) msg.obj;
                    spedit.putLong("lastPrime", current);
					spedit.commit();
					tv.setText(current + " is the latest prime found");
					Log.d(TAG, "Storing " + current + " as the last prime");

					// If the the bundle does contain a String with the key "max"
					// Clear the sp with the key "lastPrime", and inform the
					// user by Writing to the logcat file and the textview.
				} else {
					Log.d(TAG, "Max reached, clearing shared prefs");
					String message = msg.getData().getString("max");
					spedit.remove("lastPrime");
					spedit.commit();
					tv2.setText(message);
					Log.d(TAG, message);
				}

			}
		};

		// If lastPrime is found in the sp then load the value
		current = sb.getLong("lastPrime", 1);
		if (current > 1) {
			Log.d(TAG, "Found a prime in the sharedPrefs file");
		}

		// Create and tart a new Thread and send the current value as argument
		thread = new Thread(new PrimeCounter(current));
		thread.start();

	}

	/**
	 * Override the OnDestroy method to interrupt the thread when the view is
	 * destroyed
	 */
	@Override
	protected void onDestroy() {
		thread.interrupt();
		super.onDestroy();
	}

	/**
	 * Inner class that implements runnable to be able to run in a separate
	 * thread Checks a given value if it is a prime and if so, it will create a
	 * Message and send it to the handler and sleep for a short while so that
	 * the GUI Thread has time to update the view Constructor takes a long
	 * 
	 * @author Alex
	 * 
	 */
	class PrimeCounter implements Runnable {

		long num = 1;

		/**
		 * Constructor takes a long value as the starting number
		 * 
		 * @param startValue
		 */
		public PrimeCounter(long startValue) {
			num = startValue;
		}

		/**
		 * Constructor that does uses the current value 1 as startValue
		 */
		public PrimeCounter() {
		}

		/**
		 * Start checking when the Runnable is started
		 */
		@Override
		public void run() {
			check();
		}

		/**
		 * The Check method uses the current long and checks if it is less than
		 * the MAX value in a while loop, if the number is a prime. If it is a
		 * prime it sends the number back to the handler in a message object
		 * (bundle) and sleeps for a short while. If it is not a prime the
		 * method increments the current number by 2 (Since even numbers except 2 cant be prime) and loops. When MAX has
		 * been reached. The method sends back a string with key "max" to the
		 * handler
		 */
		public void check() {
			try {
				while (num < MAX) {
					Message msg = Message.obtain();
					long workingNum = num;
					if (isPrime(workingNum)) {
						if (workingNum == 1)
							workingNum = 2;
						msg.obj = workingNum;
						handler.sendMessage(msg);

						Thread.sleep(500);
					}

					num += 2;

				}

				Message msg = Message.obtain();
				Log.d(TAG, "Counter has reached Max");
				Bundle bundle = new Bundle();
				bundle.putString("max", "Counter has reached Max");
				msg.setData(bundle);
				handler.sendMessage(msg);

				// If the Thread is interrupted.
			} catch (InterruptedException e) {
				Log.d(TAG, "Thread interrupted");
			}

		}

		/**
		 * Method that checks a candidate value whether it is a prime or not
		 * 
		 * @param candidate
		 *            the value to check
		 * @return returns true if the value is a prime and false if not.
		 */
		private boolean isPrime(long candidate) {

			long sqrt = (long) Math.sqrt(candidate);

			for (long i = 3; i <= sqrt; i += 2) {
				if (candidate % i == 0) {
					return false;
				}
			}
			return true;
		}

	}
}