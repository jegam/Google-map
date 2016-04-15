package com.map;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    String[] navTitles={"","Home","Current location","Exit"};
    Integer[] navIcons={R.drawable.ic_home,R.drawable.ic_home,R.drawable.current_location,R.drawable.close};
    Double lat,lon;
    List<NavigationItems> Items = new ArrayList<NavigationItems>();
    public RecyclerViewAdapter(HomeScreen homeScreen, Double lat, Double lon) {
        this.context=homeScreen;
        this.lat=lat;
        this.lon=lon;
        createListItem();
    }

    private void createListItem() {
        for (int i = 0; i < navTitles.length; i++) {
            final NavigationItems nav_Details = new NavigationItems();
            nav_Details.setTitle(navTitles[i]);
            nav_Details.setNav_icon(navIcons[i]);
            Items.add(nav_Details);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(viewType==1){
                 View itemLayout = layoutInflater.inflate(R.layout.drawer_item_layout,null);
                 return new ViewHolder(itemLayout,viewType,context);
            }
            else if (viewType==0) {
                View itemHeader = layoutInflater.inflate(R.layout.header_layout,null);
                return new ViewHolder(itemHeader,viewType,context);
            }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position!=0){
            holder.navTitle.setText(Items.get(position).getTitle());
            holder.navIcon.setImageResource(Items.get(position).getNav_icon());
        }
    }

    @Override
    public int getItemCount() {
        return Items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)return 0;
        else return 1;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        TextView  navTitle;
        ImageView navIcon;
        Context context;

        public ViewHolder(View drawerItem , int itemType , Context context){
            super(drawerItem);
            this.context = context;
            drawerItem.setOnClickListener(this);
            if(itemType==1){
                navTitle = (TextView) itemView.findViewById(R.id.tv_NavTitle);
                navIcon = (ImageView) itemView.findViewById(R.id.iv_NavIcon);
            }
        }

        @Override
        public void onClick(View v) {
            HomeScreen homeScreen = (HomeScreen)context;
            homeScreen.drawerLayout.closeDrawers();
            switch (getPosition()){
                case 1:
                    homeScreen.startActivity(new Intent(context,HomeScreen.class));
                    break;
                case 2:
                    Intent intent = new Intent(homeScreen,SearchInstantFragment.class);
                    intent.putExtra("latitude",lat);
                    intent.putExtra("longitude", lon);
                    homeScreen.startActivity(intent);
                    Log.e("infoat", "locate" + lat + lon);
                    break;
                case 3:
                    ((HomeScreen) context).finish();
                    break;
            }
        }
    }

}
