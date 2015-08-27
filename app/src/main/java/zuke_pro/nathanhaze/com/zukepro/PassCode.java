package zuke_pro.nathanhaze.com.zukepro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.analytics.tracking.android.EasyTracker;


public class PassCode extends Activity implements View.OnClickListener {

    Button btn[] = new Button[14];

    int password;
    StringBuilder attemptedPasscode;

//    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_code);
        //register the buttons
        btn[0] = (Button)findViewById(R.id.num1);
        btn[1] = (Button)findViewById(R.id.num2);
        btn[2] = (Button)findViewById(R.id.num3);
        btn[3] = (Button)findViewById(R.id.num4);
        btn[4] = (Button)findViewById(R.id.num5);
        btn[5] = (Button)findViewById(R.id.num6);
        btn[6] = (Button)findViewById(R.id.num7);
        btn[7] = (Button)findViewById(R.id.num8);
        btn[8] = (Button)findViewById(R.id.num9);
        btn[9] = (Button)findViewById(R.id.num0);
        btn[10] = (Button)findViewById(R.id.erase);


        //register onClick event
        for(int i =0;i<11;i++){
            btn[i].setOnClickListener(this);
        }


        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
        } catch (NullPointerException e) {
            sharedPrefs = null;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        password = sharedPrefs.getInt("password", 0);
        attemptedPasscode = new StringBuilder();
        (findViewById(R.id.dot1)).setBackgroundResource(R.drawable.inactive_circle);
        (findViewById(R.id.dot2)).setBackgroundResource(R.drawable.inactive_circle);
        (findViewById(R.id.dot3)).setBackgroundResource(R.drawable.inactive_circle);
        (findViewById(R.id.dot4)).setBackgroundResource(R.drawable.inactive_circle);
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.

    }
    public void onClick(View v){
        switch(v.getId()){
            case R.id.num1:
                addtoarray("1");
                break;
            case R.id.num2:
                addtoarray("2");
                break;
            case R.id.num3:
                addtoarray("3");
                break;
            case R.id.num4:
                addtoarray("4");
                break;
            case R.id.num5:
                addtoarray("5");
                break;
            case R.id.num6:
                addtoarray("6");
                break;
            case R.id.num7:
                addtoarray("7");
                break;
            case R.id.num8:
                addtoarray("8");
                break;
            case R.id.num9:
                addtoarray("9");
                break;
            case R.id.num0:
                addtoarray("0");
                break;
            case R.id.erase:
                removeLastChar();
                break;
            default:

        }
    }

    public void addtoarray(String number){
        attemptedPasscode.append(number);
        if(attemptedPasscode.length() == 4){
            //correct password
            if(password == Integer.parseInt(attemptedPasscode.toString())){
               login();
            }
            else if(Integer.parseInt(attemptedPasscode.toString()) == 1111){
                fakeDialog();
            }
            //wrong
            else{
                ((ImageButton)findViewById(R.id.dot1)).setBackgroundResource(R.drawable.inactive_circle);
                ((ImageButton)findViewById(R.id.dot2)).setBackgroundResource(R.drawable.inactive_circle);
                ((ImageButton)findViewById(R.id.dot3)).setBackgroundResource(R.drawable.inactive_circle);
                ((ImageButton)findViewById(R.id.dot4)).setBackgroundResource(R.drawable.inactive_circle);


                YoYo.with(Techniques.Bounce)
                        .duration(700)
                        .playOn(findViewById(R.id.passcode_message));
                ((TextView)findViewById(R.id.passcode_message)).setText("The pin you entered is not correct");

                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
                attemptedPasscode.setLength(0);
            }
        }

        // visual change position
           int pos = attemptedPasscode.length();
            switch(pos){
                case 1:
                    ((ImageButton)findViewById(R.id.dot1)).setBackgroundResource(R.drawable.active_circle);
                     break;
                case 2:
                    ((ImageButton)findViewById(R.id.dot2)).setBackgroundResource(R.drawable.active_circle);
                    break;
                case 3:
                    ((ImageButton)findViewById(R.id.dot3)).setBackgroundResource(R.drawable.active_circle);
                    break;
                case 4:
                    ((ImageButton)findViewById(R.id.dot4)).setBackgroundResource(R.drawable.active_circle);
                    break;
            }
    }

    public void removeLastChar(){
        int pos = attemptedPasscode.length();
        if(pos>0) {
            attemptedPasscode.deleteCharAt(attemptedPasscode.length() - 1);
            switch (pos) {
                case 1:
                    ((ImageButton) findViewById(R.id.dot1)).setBackgroundResource(R.drawable.inactive_circle);
                    break;
                case 2:
                    ((ImageButton) findViewById(R.id.dot2)).setBackgroundResource(R.drawable.inactive_circle);
                    break;
                case 3:
                    ((ImageButton) findViewById(R.id.dot3)).setBackgroundResource(R.drawable.inactive_circle);
                    break;
                case 4:
                    ((ImageButton) findViewById(R.id.dot4)).setBackgroundResource(R.drawable.inactive_circle);
                    break;
            }
        }
    }
    private  void login(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pass_code, menu);

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

}
