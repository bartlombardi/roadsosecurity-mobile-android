package it.unibo.lam.roadsosecurity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NumberActivity extends AppCompatActivity {

    EditText phoneEditText;
    Button savePhoneButton, getContactButton;
    TextView errorText;

    Utility utility;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        utility = new Utility();
        phoneEditText = (EditText) findViewById(R.id.phoneText);
        savePhoneButton = (Button) findViewById(R.id.savePhoneButton);
        getContactButton = (Button) findViewById(R.id.getContactButton);
        errorText = (TextView) findViewById(R.id.errorText);

        sharedPreferences = getSharedPreferences("PREFERENCE", this.MODE_PRIVATE);

        savePhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(utility.validCellPhone(phoneEditText.getText().toString())) {
                    sharedPreferences.edit().putString("number", phoneEditText.getText().toString());
                    sharedPreferences.edit().commit();
                    sharedPreferences.edit().putBoolean("isFirstRun", false).commit();
                    finish();
                }
                else {
                    errorText.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    ContentResolver cr = getContentResolver();
                    Cursor cur = cr.query(contactData, null, null, null, null);
                    if (cur.getCount() > 0) {// thats mean some resutl has been found
                        if (cur.moveToNext()) {
                            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            Log.e("Names", name);
                            if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                // Query phone here. Covered next
                                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                                while (phones.moveToNext()) {
                                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    phoneEditText.setText(phoneNumber);
                                    errorText.setVisibility(View.INVISIBLE);
                                    Log.e("Number", phoneNumber);
                                }
                                phones.close();
                            }
                        }
                    }
                    cur.close();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Insert your favourite number", Toast.LENGTH_SHORT).show();
    }
}
