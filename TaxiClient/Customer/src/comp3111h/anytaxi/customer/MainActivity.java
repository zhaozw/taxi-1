package comp3111h.anytaxi.customer;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.appspot.hk_taxi.anyTaxi.AnyTaxi;
import com.appspot.hk_taxi.anyTaxi.model.Customer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;

public class MainActivity extends ActionBarActivity {

	public static final String TAG = "MainActivity"; 
	
    protected static GoogleAccountCredential credential;  
    protected static AnyTaxi endpoint;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        credential = GoogleAccountCredential.usingAudience(this, Utils.AUDIENCE);  
        Utils.customer = Utils.getCustomer(this);
	}
	
	@Override
	protected void onStart() {
	    super.onStart();    
		if (Utils.customer == null) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		} else {			
			new CheckLoginTask(this).execute();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        Toast.makeText(this, "Checking account status, please wait...", 
        		Toast.LENGTH_LONG).show();
	}
	
	private class CheckLoginTask extends AsyncTask<Void, Void, Customer> {

		Context context;
		Exception exceptionThrown;
		
		public CheckLoginTask(Context context) {
			this.context = context.getApplicationContext();
		}
		@Override
		protected Customer doInBackground(Void... params) {	
			credential.setSelectedAccountName(Utils.customer.getEmail());
			endpoint = CloudEndpointUtils.updateBuilder(
					new AnyTaxi.Builder(
							AndroidHttp.newCompatibleTransport(),
							new JacksonFactory(),
							credential)).build();
			try {
				Customer result = endpoint.getCustomer(Utils.customer.getEmail()).execute();
				return result;
			} catch (IOException e) {
				exceptionThrown = e;
				return null;
			}			
		}
		
		@Override
		protected void onPostExecute(Customer result) {
			super.onPostExecute(result);
			if (exceptionThrown != null) {
				CloudEndpointUtils.logAndShow(MainActivity.this, TAG, exceptionThrown);
				return;
			}
			if (result == null) {
				Intent intent = new Intent(this.context, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else {
				Utils.updateCustomer(context, result);
				Intent intent = new Intent(this.context, RequestActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 // TODO: since RequestActivity currently has a bug, we display the message 
				 // from server instead.
				 // context.startActivity(intent);
				 CloudEndpointUtils.logAndShow(MainActivity.this, TAG, "Successfully logged in!");
			}
		}
		
	}
}
