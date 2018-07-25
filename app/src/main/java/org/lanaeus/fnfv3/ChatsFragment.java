package org.lanaeus.fnfv3;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View mView;
    private RecyclerView mChatsList;

    private DatabaseReference mChatsDatabase, mUsersDatabase, mMsgDatabase;
    private FirebaseAuth auth;

    private String mCurrentUserID;

    private FloatingActionButton fab;

    FirebaseRecyclerAdapter<Chats, ChatsViewHolder> chatsRecyclerAdapter;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mView = inflater.inflate(R.layout.fragment_chats, container, false);

        fab = (FloatingActionButton) mView.findViewById(R.id.fabChats);


        auth = FirebaseAuth.getInstance();
        mCurrentUserID = auth.getCurrentUser().getUid();

        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserID);
        mChatsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mMsgDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserID);
        mMsgDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mChatsList = (RecyclerView) mView.findViewById(R.id.chats_list);
        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final MaterialSearchView searchView = (MaterialSearchView) getActivity().findViewById(R.id.search_view);
        final MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                MainActivity.hideTabLayout();

            }

            @Override
            public void onSearchViewClosed() {
                MainActivity.showTabLayout();

                Query query = mChatsDatabase.orderByChild("timestamp");
                onBindRecyclerView(query);


            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null & !newText.isEmpty()) {
                    final Query searchQuery = mUsersDatabase.orderByChild("name_lowercase").startAt(newText).endAt(newText + "\uf8ff");
                    onBindRecyclerView(searchQuery);

                }

                return true;
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();

        fabOnClick();

        Query query = mChatsDatabase.orderByChild("timestamp");
        onBindRecyclerView(query);
    }

    private void onBindRecyclerView(Query query) {

        chatsRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(
                Chats.class,
                R.layout.user_single_layout,
                ChatsViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, final Chats model, int position) {

                final String list_user_id = getRef(position).getKey();

                Query lastMsgQuery = mMsgDatabase.child(list_user_id).limitToLast(1);

                lastMsgQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String msg = dataSnapshot.child("msg").getValue().toString();
                        viewHolder.setMessage(msg, model.isSeen());
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
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String thumb_img = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.setName(name);
                        viewHolder.setImage(thumb_img, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("user_id", list_user_id);
                                intent.putExtra("user_name", name);
                                intent.putExtra("user_img", thumb_img);
                                startActivity(intent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        };

        mChatsList.setAdapter(chatsRecyclerAdapter);
    }

    private void fabOnClick() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserActivity.class);
                intent.putExtra("flag", "ChatFragment");
                startActivity(intent);
            }
        });

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen) {
            TextView userStatus = (TextView) mView.findViewById(R.id.user_single_status_txt);
            userStatus.setText(message);

            if (!isSeen) {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.BOLD_ITALIC);
            } else {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setName(String name) {
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name_txt);
            userName.setText(name);
        }

        public void setImage(String image, Context ctx) {
            CircleImageView userImage = (CircleImageView) mView.findViewById(R.id.user_single_img);
            Picasso.with(ctx).load(image).placeholder(R.drawable.female).into(userImage);
        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_online_icon);

            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
