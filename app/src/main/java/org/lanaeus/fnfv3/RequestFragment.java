package org.lanaeus.fnfv3;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private View mView;
    private RecyclerView mRequestList;

    private String mCurrent_user_id;
    private String list_user_id;


    private DatabaseReference mFriendReqDatabase, mUsersDatabase, mRootRef;
    private FirebaseAuth auth;

    private FirebaseRecyclerAdapter<Friend_req, RequestViewHolder> requestRecyclerAdapter;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        mView = inflater.inflate(R.layout.fragment_request, container, false);

        auth = FirebaseAuth.getInstance();
        mCurrent_user_id = auth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mFriendReqDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mRequestList = (RecyclerView) mView.findViewById(R.id.request_list);
//        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        onBindRecyclerView();

    }

    @Override
    public void onResume() {
        super.onResume();

        onBindRecyclerView();
    }



    private void onBindRecyclerView() {

        requestRecyclerAdapter = new FirebaseRecyclerAdapter<Friend_req, RequestViewHolder>(
                Friend_req.class,
                R.layout.req_single_layout,
                RequestViewHolder.class,
                mFriendReqDatabase

        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Friend_req model, int position) {

                if(model.getRequest_type().equals("received")) {
                    viewHolder.mView.setVisibility(View.VISIBLE);

                    list_user_id = getRef(position).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            viewHolder.setName(userName);
                            viewHolder.setUserImage(userThumb, getContext());

                            viewHolder.btnAcceptReq.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final String currDate = DateFormat.getDateInstance().format(new Date());

                                    Map friendMap = new HashMap();
                                    friendMap.put("Friends/" + mCurrent_user_id + "/" + list_user_id + "/date", currDate);
                                    friendMap.put("Friends/" + list_user_id + "/" + mCurrent_user_id + "/date", currDate);

                                    friendMap.put("Friend_req/" + mCurrent_user_id + "/" + list_user_id, null);
                                    friendMap.put("Friend_req/" + list_user_id + "/" + mCurrent_user_id, null);


                                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            if (databaseError == null) {

                                                mRequestList.setAdapter(requestRecyclerAdapter);
                                            } else {
                                                String error = databaseError.getMessage();
                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });

                            viewHolder.btnDeclineReq.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Map declineMap = new HashMap();

                                    declineMap.put("Friend_req/" + mCurrent_user_id + "/" + list_user_id, null);
                                    declineMap.put("Friend_req/" + list_user_id + "/" + mCurrent_user_id, null);

                                    mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            if (databaseError == null) {
                                                mRequestList.setAdapter(requestRecyclerAdapter);
                                            } else {
                                                String error = databaseError.getMessage();
                                                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    viewHolder.mView.setVisibility(View.GONE);

                }
            }

        };
        mRequestList.setAdapter(requestRecyclerAdapter);
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Button btnAcceptReq, btnDeclineReq;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            btnAcceptReq = (Button) mView.findViewById(R.id.req_accept_btn);
            btnDeclineReq = (Button) mView.findViewById(R.id.req_decline_btn);
        }

        public void setName(String userName) {

            TextView userNameView = (TextView) mView.findViewById(R.id.req_single_name_txt);
            userNameView.setText(userName);
        }

        public void setUserImage(String thumb_image, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.req_single_img);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.female).into(userImageView);

        }

    }

}
