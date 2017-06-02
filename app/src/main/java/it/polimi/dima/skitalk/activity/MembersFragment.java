package it.polimi.dima.skitalk.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import it.polimi.dima.model.HttpRequest;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.adapter.RecyclerMembersAdapter;
import it.polimi.dima.skitalk.util.DividerItemDecoration;
import it.polimi.dima.skitalk.util.RecyclerItemListener;
import it.polimi.dima.skitalk.util.Utils;
import it.polimi.dima.skitalk.util.VerticalSpacingDecoration;

public class MembersFragment extends Fragment {
    private int groupId;
    private int userId;
    private Timer timer;
    private List<User> membersList;
    private Map<Integer, User> membersMap;
    private RecyclerMembersAdapter membersAdapter;

    public MembersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        groupId = args.getInt("groupId");
        userId = args.getInt("userId");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        membersList = ((GroupActivity) getActivity()).getGroup().getMembers();
        //create a map idUser - User
        membersMap = new HashMap<Integer, User>();
        for(User u : membersList)
            membersMap.put(u.getId(), u);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView membersRecyclerView = (RecyclerView) getActivity().findViewById(R.id.members_fragment_recycler_view);
        membersAdapter = new RecyclerMembersAdapter(membersList);
        int spacing = getResources().getInteger(R.integer.member_fragment_recycler_spacing);
        membersRecyclerView.setAdapter(membersAdapter);
        membersRecyclerView.addItemDecoration(new VerticalSpacingDecoration(spacing));
        membersRecyclerView.addItemDecoration(
                new DividerItemDecoration(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                        R.drawable.item_decorator), spacing, false));
        //layout
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        membersRecyclerView.setLayoutManager(llm);
        membersRecyclerView.addOnItemTouchListener(new RecyclerItemListener(getActivity().getApplicationContext(), membersRecyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View v, int position) {
                        v.setSelected(true);
                        int selectedUserId = membersList.get(position).getId();
                        if(selectedUserId != userId) {
                            Intent myIntent = new Intent(getActivity(), OthersProfile.class);
                            Bundle extras = new Bundle();
                            extras.putInt("id", selectedUserId);
                            extras.putInt("mainUserId", userId);
                            extras.putBoolean("status", membersList.get(position).getIsOnline());
                            extras.putString("lastUpdate", membersList.get(position).getLastUpdate());
                            myIntent.putExtras(extras);
                            getActivity().startActivity(myIntent);
                        } else {
                            Intent myIntent = new Intent(getActivity(), MyProfile.class);
                            myIntent.putExtra("id", userId); //Optional parameters
                            getActivity().startActivity(myIntent);
                        }
                    }

                    public void onClickSwitch(View v, int position) {

                    }
                }, Utils.getScreenWidth(getActivity())-192));
    }

    private void scheduleUpdateTask() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
            new UpdateMembersStatusTask().execute();
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 2000);
    }

    private class UpdateMembersStatusTask extends AsyncTask<Integer, Void, Boolean> {

        private UpdateMembersStatusTask() {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            HttpRequest request = new HttpRequest("http://skitalk.altervista.org/php/getGroupOnline.php", "idGroup=" + groupId);
            Thread t = new Thread(request);
            t.start();
            JSONArray membersOnline = request.getArrayResponse();
            for (int i=0;  i < membersOnline.length(); i++){
                try {
                    JSONObject member = membersOnline.getJSONObject(i);
                    int uId = member.getInt("id");
                    User user = membersMap.get(uId);
                    if(userId == uId)
                        user.setOnline(1);
                    else
                        user.setOnline(member.getInt("isOnline"));
                    user.setTalking(member.getInt("idBusy") != -1);
                    user.setLastUpdate(member.getString("update_time"));
                    user.setKm(member.getInt("km"));
                    user.setCoords(member.getDouble("latitude"),member.getDouble("longitude"));
                    user.setAltitude(member.getInt("altitude"));
                    user.setSpeed(member.getInt("speed"));
                } catch (JSONException e) {}
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            membersAdapter.notifyDataSetChanged();
            ((GroupActivity)getActivity()).passUserToMap(membersList);
        }

        @Override
        protected void onPreExecute() {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onStart() {
        super.onStart();
        scheduleUpdateTask();
    }
}
