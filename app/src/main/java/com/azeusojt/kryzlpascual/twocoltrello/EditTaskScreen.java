package com.azeusojt.kryzlpascual.twocoltrello;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditTaskScreen extends AppCompatActivity {
    EditText txtTitle, txtDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task_screen);

        Intent i = getIntent();

        txtTitle = (EditText) findViewById (R.id.editTaskTitle);
        txtDesc = (EditText) findViewById (R.id.editTaskDesc);

        txtTitle.setText (i.getStringExtra("title"));
        txtDesc.setText(i.getStringExtra("desc"));
    }

    public void cancel (View v) {
        setResult (Activity.RESULT_CANCELED);
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
            i.putExtra ("info", info);
            i.putExtra ("pos", getIntent().getIntExtra ("pos", -1));
            i.putExtra ("from", getIntent().getIntExtra ("from", 0));
            i.putExtra ("mode", "save");
            setResult (Activity.RESULT_OK, i);
            finish();
        }
    }

    public void delete (View v) {
        AlertDialog.Builder b = new AlertDialog.Builder (this);
        b.setTitle ("Confirm Delete");
        b.setMessage ("Are you sure you want to delete this task?");

        b.setNegativeButton ("No", new DialogInterface.OnClickListener() {
            public void onClick (DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent();
                i.putExtra ("pos", getIntent().getIntExtra ("pos", 0));
                i.putExtra ("from", getIntent().getIntExtra ("from", R.id.todo));
                i.putExtra ("mode", "del");
                setResult (Activity.RESULT_OK, i);
                finish();
            }
        });

        AlertDialog alert = b.create();
        alert.show();
    }
}
