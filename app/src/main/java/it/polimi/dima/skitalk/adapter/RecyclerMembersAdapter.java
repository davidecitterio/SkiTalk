package it.polimi.dima.skitalk.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

/**
 * Created by Max on 29/12/2016.
 */

public class RecyclerMembersAdapter extends
        RecyclerView.Adapter<RecyclerMembersAdapter.MyViewHolder> {

    private int groupId;
    private List<User> membersList;
    private Map<Integer, Integer> activeGroupMap;

    /**
     * View holder class
     * */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView memberName;
        public TextView memberInfo;
        public CircleImageView picture;
        public ImageView memberStatus;
        public LinearLayout myBackground;

        public MyViewHolder(View view) {
            super(view);
            memberName = (TextView) view.findViewById(R.id.member_name);
            memberInfo = (TextView) view.findViewById(R.id.member_info);
            picture = (CircleImageView) view.findViewById(R.id.member_picture);
            memberStatus = (ImageView) view.findViewById(R.id.member_status);
            myBackground = (LinearLayout) view.findViewById(R.id.member_row);
        }
    }

    public RecyclerMembersAdapter(int groupId, List<User> membersList, Map<Integer, Integer> activeGroupMap) {
        this.groupId = groupId;
        this.membersList = membersList;
        this.activeGroupMap = activeGroupMap;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User u = membersList.get(position);
        int activeGroup = activeGroupMap.get(u.getId());
        if (u.getName().equals(""))
            holder.memberName.setText(u.getNickname());
        else
            holder.memberName.setText(u.getName());
        if(u.getIsOnline())
            if(u.isTalking()) {
                holder.memberInfo.setText("is talking...");
                holder.memberInfo.setTextColor(Color.parseColor("#1976D2"));
            } else if(activeGroup == groupId) {
                holder.memberInfo.setText("Online");
                holder.memberInfo.setTextColor(Color.parseColor("#757575"));
            } else {
                holder.memberInfo.setText("Online in another group");
                holder.memberInfo.setTextColor(Color.parseColor("#757575"));
            }
        else {
            holder.memberInfo.setText("Last seen " + u.getLastUpdate());
            holder.memberInfo.setTextColor(Color.parseColor("#757575"));
        }

        if(u.getIsOnline() && activeGroup == groupId)
            holder.memberStatus.setImageResource(R.mipmap.ic_online);
        else if(u.getIsOnline())
            holder.memberStatus.setImageResource(R.mipmap.ic_online_inactive);
        else
            holder.memberStatus.setImageResource(R.mipmap.ic_offline);
        holder.picture.setImageBitmap(Utils.getResizedBitmap(u.getPicture(), 200));
        holder.myBackground.setSelected(false);
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_members_fragment, parent, false);
        return new MyViewHolder(v);
    }
}