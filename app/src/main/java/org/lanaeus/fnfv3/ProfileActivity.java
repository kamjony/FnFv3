package org.lanaeus.fnfv3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.SQLOutput;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImg;
    private TextView profileStatus, profileName, profileFriend, profileEmail;
    private Button btnFriendReq, btnDelReq;
    private Toolbar mToolbar;

    private DatabaseReference mDatabaseRef, mFriendReqDatabase, mFriendListDatabase, mNotificationDatabase, mRootRef;

    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgressDialog;

    private int mCurr_state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.pro_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String user_id = getIntent().getStringExtra("user_id");//this is the user id of the profile the current user of the app clicks on


        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        profileImg = (ImageView) findViewById(R.id.pro_image);
        profileName = (TextView) findViewById(R.id.pro_name_txt);
        profileStatus = (TextView) findViewById(R.id.pro_status_txt);
        profileEmail = (TextView) findViewById(R.id.pro_email);
        profileFriend = (TextView) findViewById(R.id.pro_totalfriends_txt);
        btnFriendReq = (Button) findViewById(R.id.pro_add_btn);
        btnDelReq = (Button) findViewById(R.id.pro_del_btn);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the profile");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        btnDelReq.setEnabled(false);
        btnDelReq.setVisibility(View.INVISIBLE);


        mFriendListDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numChildren = dataSnapshot.getChildrenCount();
                profileFriend.setText("Number of Friends: "+ numChildren);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                profileEmail.setText(email);
                getSupportActionBar().setTitle(name);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.female).into(profileImg);


                //****************************************** FRIEND LIST/REQUEST STATE********************************************//

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurr_state = 2; //"2" means that the users request was received
                                btnFriendReq.setText("Accept Friend Request");

                                btnDelReq.setVisibility(View.VISIBLE);
                                btnDelReq.setEnabled(true);

                            }else if(req_type.equals("sent")){

                                mCurr_state = 1;
                                btnFriendReq.setText("Cancel Friend Request");

                                btnDelReq.setVisibility(View.INVISIBLE);
                                btnDelReq.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        } else{

                            mFriendListDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurr_state = 3; //"3" means that the users are friends
                                        btnFriendReq.setText("Delete Friend");

                                        btnDelReq.setVisibility(View.INVISIBLE);
                                        btnDelReq.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnFriendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnFriendReq.setEnabled(false);

                //******************************* NOT FRIENDS STATE / ADD A FRIEND ******************************************************************************//

                if (mCurr_state == 0) {
                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type","received");

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this,"There was an error while sending request", Toast.LENGTH_SHORT).show();
                            } else {
                                mCurr_state = 1;
                                btnFriendReq.setText("Cancel Friend Request");
                            }
                            btnFriendReq.setEnabled(true);
                        }
                    });


                }
                //******************************* CANCEL FRIEND REQUEST STATE ******************************************************************************//
                if (mCurr_state == 1) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btnFriendReq.setEnabled(true);
                                    mCurr_state = 0; //"0" means that the users are not friends
                                    btnFriendReq.setText("Add Friend");

                                    btnDelReq.setVisibility(View.INVISIBLE);
                                    btnDelReq.setEnabled(false);

                                }
                            });

                        }
                    });
                }

                //************************************* REQUEST RECEIVED STATE ************************************************************///////

                if (mCurr_state == 2){

                    final String currDate = DateFormat.getDateInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currDate);
                    friendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currDate);

                    friendMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                btnFriendReq.setEnabled(true);
                                mCurr_state = 3;
                                btnFriendReq.setText("Delete Friend");

                                btnDelReq.setVisibility(View.INVISIBLE);
                                btnDelReq.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                //******************************DELETE FRIEND ************************************************************************//
                if (mCurr_state == 3){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurr_state = 0;
                                btnFriendReq.setText("Add Friend");

                                btnDelReq.setVisibility(View.INVISIBLE);
                                btnDelReq.setEnabled(false);

                            } else {
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                            btnFriendReq.setEnabled(true);
                        }
                    });

                }

            }
        });

        btnDelReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map declineMap = new HashMap();

                declineMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                declineMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError == null)
                        {

                            mCurr_state = 0;
                            btnFriendReq.setText("Send Friend Request");

                            btnDelReq.setVisibility(View.INVISIBLE);
                            btnDelReq.setEnabled(false);
                        }else{
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                        }

                        btnFriendReq.setEnabled(true);
                    }
                });

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue("true"); //to make the user online -- do this for all activity
//        if (mCurrent_user == null) {
//            sendToLogin();
//        } else {
//
//        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if (mCurrent_user != null) {
//            mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue("true");
//        }
//
//    }

    private void sendToLogin() {
        startActivity(new Intent(ProfileActivity.this, LogInActivity.class));
        finish();
    }
}
