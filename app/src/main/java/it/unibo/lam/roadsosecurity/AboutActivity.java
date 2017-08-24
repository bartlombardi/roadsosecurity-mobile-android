package it.unibo.lam.roadsosecurity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element versionElement = new Element();
        versionElement.setTitle("Version 1.0");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("Mobile Application Laboratory course,\n" +
                        "University of Bologna - Masters in Computer Science. \n\n" +
                        "This app helps in accident cases immediately informing your emergency " +
                        "contact about your latitude and longitude through sending SMSes. \n" +
                        "Furthermore, this road anomaly mapping system that is able to detect " +
                        "road potholes with high accuracy.")
                .setImage(R.drawable.icon_about)
                .addItem(versionElement)
                .addGroup("Connect with us")
                .addEmail("bartolombardi@gmail.com")
                .addGitHub("bartlombardi/roadsosecurity-mobile-android")
                .create();

        setContentView(aboutPage);
    }
}
