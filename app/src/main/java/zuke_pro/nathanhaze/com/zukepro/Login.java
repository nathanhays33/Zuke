package zuke_pro.nathanhaze.com.zukepro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class Login extends Activity {


 //   public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences.Editor editor;
    SharedPreferences sharedPrefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        //set up preference
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
        } catch (NullPointerException e) {

            sharedPrefs = null;
        }
        if(sharedPrefs != null) {
            editor = sharedPrefs.edit();
        }

    }


 //   public Dialog onCreateDialog(Bundle savedInstanceState) {
    public void showPassword(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Login");

        final EditText input = new EditText(this);
        input.setHint("password");
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
        alert.setView(input);

        alert.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int value = Integer.parseInt(input.getText().toString());
                int password = sharedPrefs.getInt("password", 0);
                if(value == password ){
                    login();
                }
                else if(11111 == value){
                    fakeDialog();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT);
                    toast.show();
                }
                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void fakeDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Sorry our system's are down. Can not retrieve your passwords");

        // Setting Dialog Message
        alertDialog.setMessage("Please try again later");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }



    public void  instructions(){
        int count = sharedPrefs.getInt("count", 0);
        count++;
        editor.putInt("count", count);
        editor.commit();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Hold down the screen to unlock");

        // Setting Dialog Message
        alertDialog.setMessage("This will be displayed for the first two times after installing the app. Set a password for a better protection");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    private  void login(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private  void showPasscode(){
        Intent intent = new Intent(this, PassCode.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    protected void onStart(){
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.

        if(sharedPrefs.getInt("password", 0) != 0){
            showPasscode();
        }

        else {
            if (sharedPrefs.getInt("count", 0) < 2) {
                instructions();
            }

            Button login = (Button)findViewById(R.id.button);

            login.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (sharedPrefs.getInt("password", 0) != 0) {
                        // showPassword();
                        showPasscode();
                    } else {
                        login();
                    }
                    return true;
                }
            });
        }

    }
}
