package com.ahamedrahim.myeye;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class DeveloperProfileActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        
        // Initialize views
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView developerName = findViewById(R.id.developer_name);
        TextView developerBio = findViewById(R.id.developer_bio);
        Button btnWebsite = findViewById(R.id.btn_website);
        Button btnBack = findViewById(R.id.btn_back);
        
        // Set developer info
        developerName.setText("Ahamed Rahim");
        developerBio.setText(getString(R.string.developer_bio));
        
        // Load profile image from URL
        String photoUrl = "https://ibb.co/tpJ02rMC";
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(profileImage);
        
        // Set button listeners
        btnWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite();
            }
        });
        
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void openWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://ahamed-rahim.pages.dev/"));
        startActivity(browserIntent);
    }
}
