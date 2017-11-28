package com.zs.flyingducky;

import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * Created by zlatko on 11/27/17.
 */

public class Attack {
    private String title;
    private String duckyScript;
    private Button template;
    private Context activityContext;

    public Attack(String title, String duckyScript, View template, Context context)
    {
        this.title = title;
        this.duckyScript = duckyScript;
        this.template = (Button)template;
        this.template.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            openAttack();
        }
    });
        this.template.setText(title);
        activityContext = context;
    }

    private void openAttack() {
        AttackListActivity attackListActivity = (AttackListActivity)activityContext;
        attackListActivity.openAttack(this);
    }

    public View getView() {
        return template;
    }

    public String getTitle() {
        return title;
    }

    public String getScript() {
        return duckyScript;
    }
}
