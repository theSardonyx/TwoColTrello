package com.azeusojt.kryzlpascual.twocoltrello;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddTaskScreen2 extends AppCompatActivity {

    EditText txtTitle, txtDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_screen2);

        txtTitle = (EditText) findViewById (R.id.newTaskTitle);
        txtDesc = (EditText) findViewById (R.id.newTaskDesc);
    }

    public void cancel (View v) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void save (View v) {
        String title = txtTitle.getText().toString();
        String desc = txtDesc.getText().toString();
        if (title.trim().equals (""))
            Toast.makeText (this, "Please input a title.", Toast.LENGTH_SHORT).show();
        else {
            String[] info = {title, desc};
            Intent i = new Intent();
            i.putExtra("info", info);
            setResult (Activity.RESULT_OK, i);
            finish();
        }
    }
}
