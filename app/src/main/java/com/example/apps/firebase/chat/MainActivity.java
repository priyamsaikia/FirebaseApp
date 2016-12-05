package com.example.apps.firebase.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apps.firebase.chat.beans.ChatMessageBean;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mUsername = "";
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("messages");
    }

    @OnClick(R.id.tv_send)
    public void onSendClicked(View v) {

        if (TextUtils.isEmpty(editText.getText()))
            return;
        else
            sendMessage(editText);
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
