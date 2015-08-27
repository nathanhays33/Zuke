package zuke_pro.nathanhaze.com.zukepro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


public class MainActivity extends ActionBarActivity {

    private CustomAdapter adapter;

    ListView lv;
    final static int PICK_CONTACT = 1;

    static ArrayList<String> contacts;
    //List<Sms> deleteSMS;

    SharedPreferences sharedPrefs;
  //  public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences.Editor editor;

    ProgressDialog pd;
    volatile  Handler handler = new Handler();

    EditText pField;

    static int count = 0; //rr

    static String country = "";
    static String countryCode = "";

    private InterstitialAd interstitial;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        //set up list
        contacts = new ArrayList<String>();
        setList();

        lv = (ListView) findViewById(R.id.listView);

        adapter = new CustomAdapter(this,
                R.id.listView,
                contacts);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                deleteContactDialog(arg2);
            }
        });

        pField = (EditText)findViewById(R.id.phoneNumber);
        pField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard();
                }

            }
        });


   //     TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
  //      country  = tm.getNetworkCountryIso();
/*
        if(country.equalsIgnoreCase("us")){
           countryCode = "+1";
        }
        */


        // Prepare the Interstitial Ad
        interstitial = new InterstitialAd(MainActivity.this);
        // Insert the Ad Unit ID
        interstitial.setAdUnitId("ca-app-pub-2377934805759836/3457807766");

        // Request for Ads
        AdRequest adRequest = new AdRequest.Builder()
        // Add a test device to show Test Ads
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("5E39C82DA23AB651436D5DA0866A484D")
                .build();

        // Load ads into Interstitial Ads
        interstitial.loadAd(adRequest);


    }

    /*** Updating the contact list ****/


    /*
     *Add a contact to the sharedPref and to the list
     *
     * @param contact the contact to be added
     */
    public void setContact(String contact){
       addStringPref(contact);
       setList();
       updateList();
    }

    /*
    * Remove a contact from list
    *
    * @ i index of the person to be removed
     */

    public void removeContact(int i){
      //  String test = contacts.get(i);
        removeStringPref(contacts.get(i));
        setList();
        updateList();
    }

    public void removeStringPref(String contact){
        Set<String> temp = new HashSet<String>(contacts);
        temp.remove(contact);
        editor.putStringSet("contacts", temp);
        editor.commit();
    }


    /*
     *Refreshes the contacts from sharedPref set to be inserted into the list view
     */
    public void setList(){
        Set<String> temp = sharedPrefs.getStringSet("contacts", null);
        if(temp != null) {
            contacts = new ArrayList<String>(temp);
        }
    }

    /*
     * Adds a string to the shared pref
     */
    public void addStringPref(String contact){
        Set<String> temp = new HashSet<String>(contacts);
        temp.add(contact);
        editor.putStringSet("contacts", temp);
        editor.commit();
    }


    /*
     * Updates the visual listview
     */
    public void updateList() {
        adapter.clear();
        adapter.addAll(contacts);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.everything:
                deleteEverythingDialog();
                return true;

            case R.id.SMS:
                deleteSMSDialog();
                return true;
            case R.id.MMS:
                deleteMMSDialog();
                return true;
            case R.id.key:
                Intent intent = new Intent(this, SetPassword.class);
                startActivity(intent);
                return true;
  /*
            case R.id.PRO:
                String url = "https://play.google.com/store/apps/details?id=zuke.nathanhaze.com.zuke";
                //   String url = "amzn://apps/android?asin=B00LQAG7CI";
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));
                startActivity(intent);
                return true;

                */
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public List<Sms> deleteList(String type, boolean all) {
        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse(type);
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        this.startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
               for (int i = 0; i < totalSMS; i++) {
                String address = null;
                String name = null;

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));

                String sent = c.getString(c.getColumnIndexOrThrow("type"));
                if(!all) {
                    if (type.equalsIgnoreCase("content://mms/")) {
                        //address = getANumber(Integer.parseInt(objSms.getId()));
                        address = getMMSAddressTEST(Integer.parseInt(objSms.getId()));

                    }

                    if (type.equalsIgnoreCase("content://sms/")) {
                        objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                        address = objSms.getAddress();
                    }

                    if (address != null) {
                        name = getContactNameFromNumber(address);
                         /* Check to see if it equals name */
                        String substring = "";
                        if(address.length() >9) {
                            substring = address.substring(address.length() - 10 , address.length() );
                        }
                        if (contacts.contains(name) || contacts.contains(address) || contacts.contains(substring)   ) {
                            deleteSMS(objSms.getId(), type);
                            count++;
                        }
                    }
                }
                else{
                    count++;
                    deleteSMS(objSms.getId(), type);
                }
                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
    //    if(c != null)c.close();
        return lstSms;
    }

private String getANumber(int id) {
        String addx = "";
     //   String type = "";
        final String[] projection = new String[] {"address","contact_id","charset","type"};
        final String selection = "type=137 or type=151"; // PduHeaders
        Uri.Builder builder = Uri.parse("content://mms").buildUpon();
        builder.appendPath(String.valueOf(id)).appendPath("addr");

        Cursor cursor = this.getContentResolver().query(
                builder.build(),
                projection,
                selection,
                null, null);

        if (cursor.moveToFirst()) {
            do {
                addx = cursor.getString(cursor.getColumnIndex("address"));
             //   type = cursor.getString(cursor.getColumnIndex("type"));
            } while(cursor.moveToNext());
        }
        /*
        Outbound messages address type=137 and the value will be 'insert-address-token'
        Outbound messages address type=151 and the value will be the address
        Additional checking can be done here to return the correct address.
        if(cursor != null)cursor.close();
        */

    return addx;
    }

public String getMMSNumber(int id){
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }

        if (cAdd != null) {
         //   cAdd.close();
        }
        return name;
    }


   /****** Buttons *******/


    public void deleteTexts(){
        count= 0;
        pd = ProgressDialog.show(this, "Zuking List" , "");
        Thread t = new Thread() {
            public void run() {
               deleteList("content://sms/", false);
               deleteList("content://mms/", false);
               pd.dismiss();
               handler.post(Success);
            }
        };
        t.start();
    }

    public void deleteAll(){
        count= 0;
        pd = ProgressDialog.show(this, "Zuking All" , "");
        Thread t = new Thread() {
            public void run() {
                deleteList("content://sms/", true);
                deleteList("content://mms/", true);
                pd.dismiss();
                handler.post(Success);
            }
        };
        t.start();

        // Prepare an Interstitial Ad Listener
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                // Call displayInterstitial() function
                displayInterstitial();
            }
        });

    }

    public void deleteAllSMS(){
        count= 0;
        pd = ProgressDialog.show(this, "Zuking All" , "");
        Thread t = new Thread() {
            public void run() {
                deleteList("content://sms/", true);
                pd.dismiss();
                handler.post(Success);
            }
        };
        t.start();

        // Prepare an Interstitial Ad Listener
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                // Call displayInterstitial() function
                displayInterstitial();
            }
        });


    }

    public void deleteAllMMS(){
        count= 0;
        pd = ProgressDialog.show(this, "Zuking All" , "");
        Thread t = new Thread() {
            public void run() {
                deleteList("content://mms/", true);
                pd.dismiss();
                handler.post(Success);
            }
        };
        t.start();

        // Prepare an Interstitial Ad Listener
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
    //            // Call displayInterstitial() function
                displayInterstitial();
            }
        });

    }

    public void addNumber(View v){
       String pNumber = ((EditText)findViewById(R.id.phoneNumber)).getText().toString();
       setContact(pNumber);
       ((EditText)findViewById(R.id.phoneNumber)).setText("");
    }

    public void getContacts(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    final Runnable Success = new Runnable() {
        public void run() {
            Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(count) + " texts have been deleted", Toast.LENGTH_LONG);
            toast.show();
        }
    };

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        contacts.add(name);
                        setContact(name);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Cannot use contacts imported from " +
                                        "Facebook or similar apps at this time. Type the number in instead",
                                Toast.LENGTH_LONG).show();
                    }

                //    if(c != null) c.close();
                }
                updateList();

                break;
        }
    }

    public boolean deleteSMS(String id, String type){
        this.getContentResolver().delete(
                Uri.parse(type + id), null, null);
        Log.d("SMS", "Deleted");
        return true;
    }

    private String getContactNameFromNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name ="not found";
        Cursor cursor = this.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},null,null,null);
        if (cursor.moveToFirst())
        {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
       // if(cursor != null)cursor.close();
        return name;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pField.getWindowToken(), 0);
    }
    public void onDestroy() {
        super.onDestroy();
        /*
        if (cursor != null) {
            c.close();
        }
        if (db != null) {
            db.close();
        }
        */
    }

    /*** Dialogs ***/

    /*
    * Dialog to delete all texts from the contact list
    *
    */
    public void deleteTexts(View v){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Are you sure you want to Zuke the List");

        // Setting Dialog Message
        alertDialog.setMessage("You can not go back!!");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Zuke List", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                deleteTexts();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /*
     * Delete a contact from the list
     *
     * @param i index from the list
     */
    public void deleteContactDialog(final int i){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Do you want to erase this contact?");

        // Setting Dialog Message
        alertDialog.setMessage("You can not go back!!");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Delete Contact", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                removeContact(i);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    /*
     * Delete Everything dialog
    */
    public void deleteEverythingDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Are you sure you want to Zuke everything");

        // Setting Dialog Message
        alertDialog.setMessage("This includes stuff not on your list");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Zuke Everything", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                deleteAll();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    /*
 * Delete all MMS dialog
*/
    public void deleteMMSDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Are you sure you want to Zuke all Media");

        // Setting Dialog Message
        alertDialog.setMessage("This includes stuff not on your list");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Zuke All MMS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                deleteAllMMS();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
    /*
     * Delete all SMS dialog
    */
    public void deleteSMSDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Are you sure you want to Zuke all text ");

        // Setting Dialog Message
        alertDialog.setMessage("This includes ones not on your list");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Zuke All SMS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                deleteAllSMS();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
    public void help(View v){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Help");
/*
        if(country.equalsIgnoreCase("us")){
            alertDialog.setMessage(
                "The app detected you live in the USA so no need to add a country code." +
                " If a number is outside the country you need to add the country code and " +
                "the number without the country code (Greece: +304045551234 and 4045551234). Hold down zero to get the '+' symbol. " +
                        " Use only digits for the phone number"
            );
        }
        else {
            // Setting Dialog Message
            alertDialog.setMessage(
                    "Use only digits and attach your country's code. USA is +1 (example +14045551234). Hold down the zero button" +
                            "to get the '+' symbol. You should be able to view the full number with area code when the person messages you." +
                            "You will also need to add the number without"
            );
        }
        */

        alertDialog.setMessage("Only use the last 10 numbers of the number. For the United States and for most countries this " +
                "world this would be just the phone number.  Example for 404-123-4567 " +
                "would just be 4041234567. So no need for country code (i.e. +1)");
        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Thanks", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            }
        });

        alertDialog.setNegativeButton("View country codes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            String url = "http://en.wikipedia.org/wiki/List_of_country_calling_codes";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    protected void onStart(){
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
        setList();
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    public String getMMSAddressTEST(int i){
        String add="";
        final String[] projection =  new String[] { "address", "contact_id", "charset", "type" };
        final String selection = "type=137"; // "type="+ PduHeaders.FROM,

        Uri.Builder builder = Uri.parse("content://mms").buildUpon();
        builder.appendPath(String.valueOf(i)).appendPath("addr");

        Cursor cursor = this.getContentResolver().query(
                builder.build(),
                projection,
                selection,
                null, null);

        if (cursor.moveToFirst()) {
            add =  cursor.getString(0);
        }
        return add;
    }
}