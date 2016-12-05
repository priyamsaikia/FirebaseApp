package com.example.apps.firebase.chat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apps.firebase.chat.adapter.MessageAdapter;
import com.example.apps.firebase.chat.beans.ChatMessageBean;
import com.example.apps.firebase.chat.utils.AppConstants;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String DEAFAULT_MSG_LENGTH = "DEFAULT_CHAR_COUNT";
    private static final String TAG = MainActivity.class.getSimpleName();
    @Bind(R.id.tv_error_rv)
    TextView tv_error_rv;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.recycler_view)
    RecyclerView recycerview;

    @Bind(R.id.edt_msg)
    EditText editText;
    @Bind(R.id.tv_send)
    TextView tv_send;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    private String mUsername;
    private ArrayList<ChatMessageBean> mMessageList;
    private MessageAdapter mMessageAdapter;
    ChildEventListener mChildEventListener;

    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    FirebaseStorage mFirebaseStorage;
    StorageReference mStorageReference;

    long DEFAULT_CHARACTER_COUNT = 140;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mUsername = "";
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        mMessageList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, mMessageList);
        recycerview.setAdapter(mMessageAdapter);
        recycerview.setLayoutManager(new LinearLayoutManager(this));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //signed in
                    mUsername = user.getDisplayName();
                    getChatMessages();
                } else {
                    mMessageList.clear();
                    mMessageAdapter.notifyDataSetChanged();
                    if (mChildEventListener != null) {
                        mDatabaseReference.removeEventListener(mChildEventListener);
                        mChildEventListener = null;
                    }

                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                                    .setProviders(AuthUI.GOOGLE_PROVIDER).build(),
                            AppConstants.RC_SIGNIN);
                }
            }
        };

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        Map<String, Object> defultConfigMap = new HashMap<>();
        defultConfigMap.put(DEAFAULT_MSG_LENGTH, DEFAULT_CHARACTER_COUNT);
        fetchConfig();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("photo_msgs");
    }

    private void fetchConfig() {
        mFirebaseRemoteConfig.fetch(0).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        mFirebaseRemoteConfig.activateFetched();
                        long msg_length = mFirebaseRemoteConfig.getLong(DEAFAULT_MSG_LENGTH);
                        DEFAULT_CHARACTER_COUNT = msg_length;
                    }
                }

        ).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                long msg_length = mFirebaseRemoteConfig.getLong(DEAFAULT_MSG_LENGTH);
                DEFAULT_CHARACTER_COUNT = msg_length;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.RC_SIGNIN) {
            if (resultCode == RESULT_CANCELED)
                finish();
        } else if (requestCode == AppConstants.RC_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                StorageReference storageReference = mStorageReference.child(uri.getLastPathSegment());//getting specific image
                storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri photoUrl = taskSnapshot.getDownloadUrl();
                        ChatMessageBean bean = new ChatMessageBean(null, mUsername, photoUrl.toString());

                        mDatabaseReference.push().setValue(bean);
                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void getChatMessages() {
        mMessageList.clear();
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessageBean bean = dataSnapshot.getValue(ChatMessageBean.class);
                mMessageList.add(bean);
                mMessageAdapter.notifyDataSetChanged();

                if (mMessageList.size() > 0) {
                    progressBar.setVisibility(View.GONE);
                    tv_error_rv.setVisibility(View.GONE);
                    recycerview.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    tv_error_rv.setVisibility(View.VISIBLE);
                    recycerview.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @OnClick(R.id.tv_send)
    public void onSendClicked(View v) {

        if (TextUtils.isEmpty(editText.getText()))
            return;
        else if (editText.getText().length() > DEFAULT_CHARACTER_COUNT) {
            Log.d(TAG, "length = " + editText.getText().toString().length() + " allowed length = " + DEFAULT_CHARACTER_COUNT);
            Toast.makeText(this, getText(R.string.error_long_msg), Toast.LENGTH_SHORT).show();
            return;
        } else
            sendMessage(editText);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
            mMessageList.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
            mMessageList.clear();
        }
    }

    @OnClick(R.id.imv_attach)
    public void attachImageClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Choose image"), AppConstants.RC_IMAGE);
    }

    private void sendMessage(EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString())) {
            Toast.makeText(this, getText(R.string.error_empty_msg), Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            return;
        }
        ChatMessageBean chatMessageBean = new ChatMessageBean(editText.getText().toString(), mUsername, null);
        mDatabaseReference.push().setValue(chatMessageBean);

        editText.setText("");
    }
}
