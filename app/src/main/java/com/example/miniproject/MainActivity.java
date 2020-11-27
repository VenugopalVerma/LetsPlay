package com.example.miniproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private Button signIn;
    private TextView displayName;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signIn = findViewById(R.id.signIn);
        displayName = findViewById(R.id.textView);

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createSignInIntent();
                }
            });
        }
        else{
//            Log.e("DisplayName", "onCreate: ",new Exception("Some Error") );

            CharSequence some = displayName.getText();
            Log.d("DisplayName", "onCreate: " + some);
            displayName.setText(user.getDisplayName());
            signIn.setText("Sign out");
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signOut();
                }
            });
        }
        Log.d("firestore instance",db.toString());


    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
//                new AuthUI.IdpConfig.TwitterBuilder().build()
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (data != null) {
            if (requestCode == RC_SIGN_IN) {
                // Successfully signed in
                IdpResponse response = IdpResponse.fromResultIntent(data);
                Log.d("Response Data", response.toString());

                if (resultCode == RESULT_OK) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if (response.isNewUser()) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        CustomUser customUser = new CustomUser(user);
                        db.collection("users").add(customUser).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("User", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })

                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("User", "Error adding document", e);
                                    }
                                });
                    }

//                    user = FirebaseAuth.getInstance().getCurrentUser();
                    displayName.setText(user.getDisplayName());
                    signIn.setText("Sign Out");
                    signIn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signOut();
                        }
                    });

                    Toast.makeText(this, "Signing In Success!",
                            Toast.LENGTH_LONG).show();
                    // ...
                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    // ...

                    Toast.makeText(MainActivity.this, "Error Signing In! Please SignIn again.",
                        Toast.LENGTH_LONG).show();

                    Log.d("Error in Signin", "Some error in sign in method"+ response.getError().getMessage());
                }
            }
        }
        else {
            Toast.makeText(this,"Sign In request Cancled",Toast.LENGTH_LONG).show();
        }
    }
    // [END auth_fui_result]

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("Sign Out","Signed Out successful");
                    }
                });
        // [END auth_fui_signout]
        Toast.makeText(this,"User signed out",Toast.LENGTH_LONG).show();
        displayName.setText("Login to continue");
        signIn.setText("Sign In");
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSignInIntent();
            }
        });
    }

}