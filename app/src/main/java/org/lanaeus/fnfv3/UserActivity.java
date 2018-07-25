package org.lanaeus.fnfv3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    private RecyclerView mSearchList;
    private Toolbar mToolbar;

    private DatabaseReference mUserDatabase;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> searchRecyclerAdapter;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar = (Toolbar) findViewById(R.id.user_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Select Friend");
        mToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchList = (RecyclerView) findViewById(R.id.result_list);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mSearchList.setHasFixedSize(true);
        mSearchList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if (currentUser == null) {
            sendToLogin();
        } else {
            mUserDatabase.child(currentUser.getUid()).child("online").setValue("true"); //to make the user online -- do this for all activity
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if (currentUser != null) {
//            mUserDatabase.child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
//        }
//
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);
        final MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);


        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);


            }

            @Override
            public void onSearchViewClosed() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);


                mSearchList.setAdapter(searchRecyclerAdapter);

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                final String checkFlag= getIntent().getStringExtra("flag");

                if (newText != null & !newText.isEmpty()){

                    Query searchQuery = mUserDatabase.orderByChild("name_lowercase").startAt(newText).endAt(newText + "\uf8ff");

                    searchRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                            Users.class,
                            R.layout.user_single_layout,
                            UsersViewHolder.class,
                            searchQuery) {
                        @Override
                        protected void populateViewHolder(UsersViewHolder viewHolder, final Users model, int position) {

                            final String list_user_id = getRef(position).getKey();

                            viewHolder.setName(model.getName());
                            viewHolder.setStatus(model.getStatus());
                            viewHolder.setUserImage(model.getImage(), getApplicationContext());


                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(checkFlag.equals("FriendFragment")) {
                                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                        intent.putExtra("user_id", list_user_id);

                                        startActivity(intent);
                                    } else {

                                        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                                        intent.putExtra("user_id", list_user_id);
                                        intent.putExtra("user_name", model.getName());
                                        startActivity(intent);
                                    }
                                }
                            });


                        }


                    };

                    mSearchList.setAdapter(searchRecyclerAdapter);


                }

                return true;
            }
        });
        return true;
    }

    private void sendToLogin() {
        startActivity(new Intent(UserActivity.this, LogInActivity.class));
        finish();
    }




    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setStatus(String status) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status_txt);
            userStatusView.setText(status);
        }

        public void setName(String userName) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name_txt);
            userNameView.setText(userName);
        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_img);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.female).into(userImageView);

        }
    }
}
