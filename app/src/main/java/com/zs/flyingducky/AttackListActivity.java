package com.zs.flyingducky;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class AttackListActivity extends AppCompatActivity {

    List<Attack> attacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attack_list);
        attacks = generateMockListOfAttacks();//load attacks
        loadAttackListView();

    }

    private void loadAttackListView() {
        LinearLayout attackListLayout = (LinearLayout)findViewById(R.id.activity_attack_list);
        for(Attack attack : attacks)
        {
            attackListLayout.addView(attack.getView());
        }
    }

    private List<Attack> generateMockListOfAttacks()
    {
        List<Attack> result = new ArrayList<Attack>();
        result.add(new Attack("HelloWorld","STRING helloworld",
                getLayoutInflater().inflate(R.layout.attack_button,null), AttackListActivity.this));
        return result;
    }

    public void openAttack(Attack attack) {
        Intent intent = new Intent(getBaseContext(), AttackActivity.class);
        intent.putExtra("attack_title", attack.getTitle());
        intent.putExtra("attack_script", attack.getScript());
        startActivity(intent);
    }
}
