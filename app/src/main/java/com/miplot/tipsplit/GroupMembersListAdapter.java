package com.miplot.tipsplit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupMembersListAdapter extends RecyclerView.Adapter<GroupMembersListAdapter.ViewHolder> {
    public static class DisplayedUser {
        public User user;
        public double balance;
    }
    private Context context;
    private List<DisplayedUser> displayedUsers;
    private List<Boolean> isSelected;
    private int size;

    public GroupMembersListAdapter(Context context, List<DisplayedUser> displayedUsers) {
        super();
        this.context = context;
        this.displayedUsers = displayedUsers;
        size = displayedUsers.size();
        this.isSelected = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            isSelected.add(false);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private TextView nameTextView;
        private TextView balanceTextView;

        ViewHolder(View v, CheckBox checkBox, TextView nameTextView, TextView balanceTextView) {
            super(v);
            this.checkBox = checkBox;
            this.nameTextView = nameTextView;
            this.balanceTextView = balanceTextView;
        }
    }

    @Override
    public GroupMembersListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView;
        convertView = inflater.inflate(R.layout.group_members_list_item, parent, false);

        return new GroupMembersListAdapter.ViewHolder(convertView,
                (CheckBox) convertView.findViewById(R.id.userSelectedCheckBox),
                (TextView) convertView.findViewById(R.id.userName),
                (TextView) convertView.findViewById(R.id.userBalance));
    }

    @Override
    public void onBindViewHolder(final GroupMembersListAdapter.ViewHolder holder, int position) {
        holder.checkBox.setChecked(isSelected.get(position));
        holder.nameTextView.setText(displayedUsers.get(position).user.getName());
        holder.balanceTextView.setText(String.format(Locale.ENGLISH, "%+.0f \u20BD", displayedUsers.get(position).balance));
        if (displayedUsers.get(position).balance >= 0) {
            holder.balanceTextView.setTextColor(context.getResources().getColor(R.color.positiveBalance));
        } else if (displayedUsers.get(position).balance < 0) {
            holder.balanceTextView.setTextColor(context.getResources().getColor(R.color.negativeBalance));
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSelected.set(holder.getAdapterPosition(), isChecked);
            }
        });
    }

    public void updateData(List<DisplayedUser> displayedUsers) {
        this.displayedUsers.clear();
        this.displayedUsers.addAll(displayedUsers);

        Log.e("TAG", "New # of displayed users: " + displayedUsers.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return size;
    }

    public List<User> getCheckedUsers() {
        List<User> checkedUsers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (isSelected.get(i)) {
                checkedUsers.add(displayedUsers.get(i).user);
            }
        }
        return checkedUsers;
    }
}