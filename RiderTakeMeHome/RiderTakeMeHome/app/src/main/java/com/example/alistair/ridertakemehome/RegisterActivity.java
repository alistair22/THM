package com.example.alistair.ridertakemehome;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by Alistair on 23/2/2018.
 */
public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ProgressDialog dialog;
    DatabaseReference databaseReference;

    EditText edName, edPhone, edNextOfKin, edEmail, edPassword;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        edName = (MaterialEditText) findViewById(R.id.edName);
        edPhone = (MaterialEditText) findViewById(R.id.edPhone);
        edNextOfKin = (MaterialEditText) findViewById(R.id.edNextOfKin);
        edEmail = (MaterialEditText) findViewById(R.id.edEmail);
        edPassword = (MaterialEditText) findViewById(R.id.edPassword);

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Registering , Please wait...");
                dialog.show();

                final String name = edName.getText().toString();
                String phone = edPhone.getText().toString();
                String nextofkin = edNextOfKin.getText().toString();
                String email = edEmail.getText().toString();
                String password = edPassword.getText().toString();


                if (name.equals("") || phone.equals("") || nextofkin.equals("") || email.equals("") && password.equals("")) {

                    dialog.hide();
                    Toast.makeText(RegisterActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }else {
                    auth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        dialog.hide();
                                        Toast.makeText(RegisterActivity.this, "User registration successful!!", Toast.LENGTH_SHORT).show();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders");
                                        Users user_object = new Users(edName.getText().toString(), edPhone.getText().toString(), edNextOfKin.getText().toString(), edEmail.getText().toString(), edPassword.getText().toString());
                                        FirebaseUser firebaseUser = auth.getCurrentUser();

                                        databaseReference.child(firebaseUser.getUid()).setValue(user_object)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(RegisterActivity.this, "User data stored successfully!!", Toast.LENGTH_LONG).show();
                                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }else {
                                                            Toast.makeText(RegisterActivity.this, "User could not be saved", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    }else {
                                        dialog.hide();
                                        Toast.makeText(RegisterActivity.this, "User could not be registered", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}


