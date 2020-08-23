package com.example.alistair.finaltakemehome;

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
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by Alistair on 23/2/2018.
 */
public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ProgressDialog dialog;

    EditText edEmail, edPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edEmail = (MaterialEditText) findViewById(R.id.edEmail);
        edPassword = (MaterialEditText) findViewById(R.id.edPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.setMessage("Signing In, Please wait...");
                dialog.show();

                if (edEmail.getText().toString().equals("") && edPassword.getText().toString().equals("")){
                    dialog.hide();
                    Toast.makeText(LoginActivity.this, "Email or Password field is empty", Toast.LENGTH_SHORT).show();
                }else {
                    auth.signInWithEmailAndPassword(edEmail.getText().toString(),edPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                     if (task.isSuccessful()){
                                         dialog.hide();
                                         Toast.makeText(LoginActivity.this, "SignIn Successful", Toast.LENGTH_SHORT).show();
                                         Intent intent = new Intent(LoginActivity.this, MainRideActivity.class);
                                         startActivity(intent);
                                         finish();
                                     }
                                    else {
                                         dialog.hide();
                                         Toast.makeText(LoginActivity.this, "Incorrect credentials, Please check and try again", Toast.LENGTH_SHORT).show();
                                     }
                                }
                            });
                }

            }
        });

    }


}
