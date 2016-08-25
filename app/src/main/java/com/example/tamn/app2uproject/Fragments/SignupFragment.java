package com.example.tamn.app2uproject.Fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.example.tamn.app2uproject.Constants;
import com.example.tamn.app2uproject.IOHelper;
import com.example.tamn.app2uproject.MainActivity;
import com.example.tamn.app2uproject.Model.UserDetails;
import com.example.tamn.app2uproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    private static final int GALLERY_REQUEST = 15;
    private static final int REQUEST_STORAGE_CODE = 10;
    EditText etSignupEmail, etSignupPassword, etUsername;
    ImageView ivProfileImage;
    Button btnSignup;

    View inflate;
    String username;
    String imageUrl;
    GalleryPhoto galleryPhoto;

    DatabaseReference reference;


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(Constants.STORAGE_URL);

    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflate = inflater.inflate(R.layout.fragment_signup, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.create_account));
        //show/hide toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        initLayout();
        checkIfPermissionNeeded();
        initEvents();

        return inflate;
    }

    private void checkIfPermissionNeeded() {
        int result = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED){

            //ask for permission
            showPermissionSystemDialog();
        }
    }

    private void showPermissionSystemDialog() {
        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(getActivity(),permission,REQUEST_STORAGE_CODE);
    }

    private void initLayout() {
        etSignupEmail = (EditText) inflate.findViewById(R.id.etSignupEmail);
        etSignupPassword = (EditText) inflate.findViewById(R.id.etSignupPassword);
        etUsername = (EditText) inflate.findViewById(R.id.etUsername);
        ivProfileImage = (ImageView) inflate.findViewById(R.id.ivProfileImage);
        btnSignup = (Button) inflate.findViewById(R.id.btnSignup);
        galleryPhoto = new GalleryPhoto(getContext()/*getActivity()*/); //getContext();
    }

    /*removeeeeeeeeeeeeeeeeeeeeeee*/

    public void uploadImageToServer( ) {
        ivProfileImage.setDrawingCacheEnabled(true);
        ivProfileImage.buildDrawingCache();
        //Convert ImageView to Bytes
        Bitmap bitmap = ivProfileImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,60,baos);
        byte[] data = baos.toByteArray();

        //Epoch time File name
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        //File name in Storages + directory
        StorageReference imageRef = storageRef.child("profile/" + ts);
        //Upload to storage
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                imageUrl = downloadUrl.toString();
                //uploadEverythingToDataBase();
                //uploadDataToFirebase(title,content, imageUrl);
            }
        });

    }
    /*removeeeeeeeeeeeeeeeeeeee*/

    private void initEvents() {
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(galleryPhoto.openGalleryIntent(),GALLERY_REQUEST);
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
 //               username = etUsername.getText().toString();
                uploadImageToServer();
               // Log.d("tam231", imageUrl);
                //uploadToDataBase(imageUrl);
                createUserEmailAndPass();
            }
        });
    }

    /*private void uploadToDataBase(String imageUrl) {
        //username = etUsername.getText().toString();
        Log.d("tam",username );
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        DatabaseReference reference = instance.getReference("users")*//*.child(FirebaseAuth.getInstance().getCurrentUser().getUid())*//*;
        UserDetails userDetails = new UserDetails(username,imageUrl);
        //Tam
        reference.push().setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getActivity(), "Uploaded" , Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    private void createNewUser(FirebaseUser user) {
        String username = etUsername.getText().toString();

        // Write new user
        writeNewUser(user.getUid(), username);
    }

    private void writeNewUser(String userId, String name) {
        UserDetails user = new UserDetails(name, imageUrl);

        reference= FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(userId).push().setValue(user);
    }

    //Sign Up with email and password
    private void createUserEmailAndPass() {
        String email = etSignupEmail.getText().toString();
        String pass = etSignupPassword.getText().toString();
        //String username = etUsername.getText().toString(); //todo:remove

            try {
                //todo: add support for username and image
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                        // add a listener in case of success
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    createNewUser(task.getResult().getUser());
                                    // succeed in creating a user
                                    moveToMainActivity();
                                }
                            }
                            // add a listener in case of failure
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e != null) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                etSignupEmail.setError(getResources().getString(R.string.required_field));
               IOHelper.getAnimation(etSignupEmail, Techniques.Shake);

                etSignupPassword.setError(getResources().getString(R.string.required_field));
                IOHelper.getAnimation(etSignupPassword ,Techniques.Shake );

                etUsername.setError(getResources().getString(R.string.required_field));
                IOHelper.getAnimation(etUsername,Techniques.Shake);

                Toast.makeText(getActivity(), getResources().getString(R.string.Email_Password_username_cannot_be_empty), Toast.LENGTH_SHORT).show();
            }

    }

    private void moveToMainActivity() {
        startActivity(new Intent(getActivity(), MainActivity.class));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case GALLERY_REQUEST:
                    Uri uri = data.getData();
                    galleryPhoto.setPhotoUri(uri);

                    String path = galleryPhoto.getPath();

                    try {
                        Bitmap bitmap = ImageLoader.init()
                                .from(path).requestSize(400,400).getBitmap();

                        ivProfileImage.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


            }
        }

    }
}
