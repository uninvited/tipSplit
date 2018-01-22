package com.miplot.tipsplit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.miplot.tipsplit.activities.GroupMembersActivity;

import java.util.List;

public class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.ViewHolder> {
    private Context context;
    private List<Group> groups;

    public GroupsListAdapter(Context context, List<Group> groups) {
        super();
        this.context = context;
        this.groups = groups;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private View parentView;
        private TextView nameTextView;
        private TextView descriptionTextView;

        ViewHolder(View v, View parentView, TextView nameTextView, TextView descriptionTextView) {
            super(v);
            this.parentView = parentView;
            this.nameTextView = nameTextView;
            this.descriptionTextView = descriptionTextView;
        }
    }

    @Override
    public GroupsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView;
        convertView = inflater.inflate(R.layout.group_list_item, parent, false);

        return new GroupsListAdapter.ViewHolder(convertView,
                convertView.findViewById(R.id.parentLayout),
                (TextView) convertView.findViewById(R.id.groupName),
                (TextView) convertView.findViewById(R.id.groupDescription));
    }

    @Override
    public void onBindViewHolder(final GroupsListAdapter.ViewHolder holder, int position) {
        final Group group = groups.get(position);

        holder.nameTextView.setText(group.getName());
        holder.descriptionTextView.setText(group.getDescription());

        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupMembersActivity.class);
                intent.putExtra(GroupMembersActivity.GROUP_NAME_INTENT_KEY, group.getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateData(List<Group> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
        notifyDataSetChanged();
    }
}