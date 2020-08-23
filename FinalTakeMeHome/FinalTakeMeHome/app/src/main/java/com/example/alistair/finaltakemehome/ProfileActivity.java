package com.example.alistair.finaltakemehome;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Button btnUpdateUserInfo;
    private EditText edChangeEmail,edChangePassword,edChangePhone;
    private TextView edLabelForUserInfo;
    ProgressDialog dialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mRiderDatabase;
    private String userID, mEmail, mPassword,mPhone ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnUpdateUserInfo = (Button) findViewById(R.id.btnUpdateInfo);

        edLabelForUserInfo = (TextView) findViewById(R.id.edupdateUser);
        edChangeEmail = (MaterialEditText) findViewById(R.id.edPassword);
        edChangePassword = (MaterialEditText) findViewById(R.id.edPassword);
        edChangePhone = (MaterialEditText) findViewById(R.id.edPassword);

        dialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        mRiderDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        getUserInfo();

        btnUpdateUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Updating , Please wait...");
                dialog.show();

                saveUserInformation();

                dialog.hide();
                Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void getUserInfo(){
        mRiderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map.get("email")!=null){
                    mEmail = map.get("email").toString();
                    edChangeEmail.setText(mEmail);
                }
                    if (map.get("password")!=null){
                        mPassword = map.get("password").toString();
                        edChangePassword.setText(mPassword);
                    }
                    if (map.get("phone")!=null){
                        mPhone = map.get("phone").toString();
                        edChangePhone.setText(mPhone);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void saveUserInformation() {
        mEmail = edChangeEmail.getText().toString();
        mPassword =edChangePassword.getText().toString();
        mPhone = edChangePhone.getText().toString();

        Map  userInfo = new HashMap();
        userInfo.put("email", mEmail);
        userInfo.put("password", mPassword);
        userInfo.put("phone", mPhone);

        mRiderDatabase.updateChildren(userInfo);

        finish();
    }

}
