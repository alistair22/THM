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
public class Users {
    String name, phone, nextofkin, email, password;

    public Users(String name, String phone, String nextofkin, String email, String password) {
        this.name = name;
        this.phone = phone;
        this.nextofkin = nextofkin;
        this.email = email;
        this.password = password;
    }
}
