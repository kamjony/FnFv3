package org.lanaeus.fnfv3;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by KamrulHasan on 3/5/2018.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatsFragment chatFragment = new ChatsFragment();
                return chatFragment;

            case 2:
                FriendFragment friendFragment = new FriendFragment();
                return friendFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;  //since we have 3 tabs now... CHANGE IT WHEN YOU HAVE MORE TABS
    }


    //TO CHANGE THE NAmE OF THE TABS
    public CharSequence getPageTitle(int position){

        switch (position){
            case 0:
                return "REQUESTS";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }

    }
}
