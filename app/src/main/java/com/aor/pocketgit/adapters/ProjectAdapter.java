package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.services.GitClone;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import java.util.List;

public class ProjectAdapter extends ArrayAdapter<Project> {
    public ProjectAdapter(Context context, List<Project> projects) {
        super(context, 0, projects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_project, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        ((TextView) convertView.findViewById(R.id.project_name)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.project_url)).setText(getItem(position).getUrl());
        Project project = getItem(position);
        ImageView icon = (ImageView) convertView.findViewById(R.id.image_state);
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f, 1, 0.5f, 1, 0.5f);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setDuration(3000);
        if (project.getState().equals(Project.STATE_FAILED)) {
            icon.clearAnimation();
            icon.setImageResource(R.drawable.ic_failed);
        } else if (project.getState().equals(Project.STATE_UNKNOWN) || project.getState().equals(Project.STATE_CLONED)) {
            if (GitUtils.repositoryCloned(project)) {
                icon.clearAnimation();
                icon.setImageResource(R.drawable.ic_cloned);
            } else {
                icon.clearAnimation();
                icon.setImageResource(R.drawable.ic_action_clone);
                icon.setVisibility(0);
            }
        } else if (project.getState().equals(Project.STATE_CLONED)) {
            icon.setVisibility(8);
        } else if (project.getState().equals(Project.STATE_CLONING)) {
            if (GitClone.isCloning(project.getId())) {
                icon.setImageResource(R.drawable.ic_cloning);
                icon.setVisibility(0);
                icon.startAnimation(rotateAnimation);
            } else {
                icon.clearAnimation();
                icon.setImageResource(R.drawable.ic_failed);
                icon.setVisibility(0);
            }
        }
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
