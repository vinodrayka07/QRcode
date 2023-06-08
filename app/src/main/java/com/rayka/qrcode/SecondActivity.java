package com.rayka.qrcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SecondActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    TextView name;
    Button signOutBtn,scan;
    public String personName;
    public String personEmail;
    public String personqrdata;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://qrcode-7878e-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().hide();

        name = findViewById(R.id.name);
        scan = findViewById(R.id.scan);
        signOutBtn = findViewById(R.id.signout);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(SecondActivity.this);
                intentIntegrator.setPrompt("Scan qr code");
                intentIntegrator.initiateScan();

            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
            name.setText(personName);
            //email.setText(personEmail);
        }

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // check if phone is not registered before
                if (snapshot.hasChild(personName)) {
                    Toast.makeText(SecondActivity.this, "Phone is already registered", Toast.LENGTH_SHORT).show();
                } else {

                    // sending data to firebase Realtime Database.
                    // we are using phone number as unique identity of every user.
                    // so all the other details of user comes under phone number
                    databaseReference.child("users").child(personName).child("fullname").setValue(personEmail);



                    // show a success message then finish the activity
                    Toast.makeText(SecondActivity.this, "pealese Wait....", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(SecondActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });



        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (intentResult!=null){

            if (intentResult.getContents()==null){

                Toast.makeText(SecondActivity.this, "cansel", Toast.LENGTH_SHORT).show();


            }
            else {

                TextView textView = findViewById(R.id.data);
                textView.setText(intentResult.getContents());
                personqrdata = (intentResult.getContents());


                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(personqrdata)));

                databaseReference.child("Qrdata").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        databaseReference.child("Qrdata").child("data").child("value").setValue(personqrdata);



                        // show a success message then finish the activity
                        Toast.makeText(SecondActivity.this, "pealese Wait....", Toast.LENGTH_SHORT).show();


                        // check if phone is not registered before
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(SecondActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


            }


        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }

    void signOut(){
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(SecondActivity.this,MainActivity.class));
            }
        });
    }


}