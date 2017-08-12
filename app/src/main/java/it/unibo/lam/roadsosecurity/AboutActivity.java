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
        //setContentView(R.layout.activity_about);

        Element versionElement = new Element();
        versionElement.setTitle("Version 1.0");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("prova prova prova")
                .setImage(R.drawable.icon_app)
                .addItem(versionElement)
                .addGroup("Connect with us")
                .addEmail("bartolombardi@gmail.com")
                .addFacebook("bartolomeo.lombardi")
                .addInstagram("bartlombardi")
                .addGitHub("bartlombardi/roadsosecurity-mobile-android")
                .create();

        setContentView(aboutPage);
    }
}
