package com.azeusojt.kryzlpascual.twocoltrello;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Task> todoTasks, doneTasks;
    ListView listTodo, listDone;

    int resumeColor = 0x000000;

    public class Task {
        String title, desc;

        Task (String t, String d) {
            title = t;
            desc = d;
        }
    }

    class PassObject {
        View v;
        Task t;
        List<Task> srcList;

        PassObject (View v, Task t, List<Task> srcList) {
            this.v = v;
            this.t = t;
            this.srcList = srcList;
        }
    }

    static class ViewHolder {
        TextView title, desc;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTasks();

        listTodo = (ListView) findViewById (R.id.todo);
        listDone = (ListView) findViewById (R.id.done);

        ForTwoAdapter forTodo = new ForTwoAdapter (MainActivity.this, todoTasks);
        listTodo.setAdapter (forTodo);

        ForTwoAdapter forDone = new ForTwoAdapter (MainActivity.this, doneTasks);
        listDone.setAdapter (forDone);

        listTodo.setOnItemClickListener(new Clickety());
        listDone.setOnItemClickListener(new Clickety());

        listTodo.setOnItemLongClickListener(new LongClick());
        listDone.setOnItemLongClickListener(new LongClick());

        listTodo.setOnDragListener(new DragThere());
        listDone.setOnDragListener(new DragThere());
    }

    class Clickety implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Task select = (Task) parent.getItemAtPosition (position);
            Intent i = new Intent (MainActivity.this, EditTaskScreen.class);
            i.putExtra ("title", select.title);
            i.putExtra ("desc", select.desc);
            i.putExtra ("pos", position);
            i.putExtra ("from", parent.getId());
            startActivityForResult (i, 2);
        }
    }

    class LongClick implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
            Task select = (Task) parent.getItemAtPosition (position);

            ForTwoAdapter adapt = (ForTwoAdapter) parent.getAdapter();
            List<Task> list = adapt.getList();
            PassObject  pass = new PassObject (view, select, list);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
            view.startDrag (data, shadow, pass, 0);

            return true;
        }
    }

    class ForTwoAdapter extends BaseAdapter {
        private Context c;
        private List<Task> tasks;

        ForTwoAdapter(Context c, List<Task> l) {
            this.c = c;
            this.tasks = l;
        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public Object getItem (int position) {
            return tasks.get(position);
        }

        @Override
        public long getItemId (int position) {
            return position;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = ((Activity) c).getLayoutInflater();
                v = inflater.inflate(R.layout.task_item, null);

                ViewHolder hold = new ViewHolder();
                hold.title = (TextView) v.findViewById (R.id.task_title);
                hold.desc = (TextView) v.findViewById (R.id.task_desc);
                v.setTag (hold);
            }

            ViewHolder hold = (ViewHolder) v.getTag();
            hold.title.setText (tasks.get (position).title);
            hold.desc.setText (tasks.get (position).desc);

            v.setOnDragListener(new Draggy (tasks.get (position)));

            return v;
        }

        public List<Task> getList() {
            return tasks;
        }
    }

    class DragThere implements View.OnDragListener {
        @Override
        public boolean onDrag (View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                PassObject pass = (PassObject) event.getLocalState();
                View v2 = pass.v;
                Task passed = pass.t;

                List<Task> srcList = pass.srcList;
                ListView old = (ListView) v2.getParent();
                ForTwoAdapter src = (ForTwoAdapter) old.getAdapter();
                ListView newP = (ListView) v;
                ForTwoAdapter dest = (ForTwoAdapter) newP.getAdapter();
                List<Task> destList = dest.getList();

                if (remove (srcList, passed)) {
                    insert (destList, passed);
                }

                src.notifyDataSetChanged();
                dest.notifyDataSetChanged();

                newP.smoothScrollToPosition(dest.getCount() - 1);
            }

            return true;
        }
    }

    class Draggy implements View.OnDragListener {
        Task t;

       Draggy (Task t) {
            this.t = t;
        }

        @Override
        public boolean onDrag (View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED :
                    v.setBackgroundColor (0x30000000);
                    break;
                case DragEvent.ACTION_DRAG_EXITED :
                    v.setBackgroundColor(resumeColor);
                    break;
                case DragEvent.ACTION_DROP :
                    PassObject pass = (PassObject) event.getLocalState();
                    View v2 = pass.v;
                    Task passed = pass.t;

                    List<Task> srcList = pass.srcList;
                    ListView old = (ListView) v2.getParent();
                    ForTwoAdapter src = (ForTwoAdapter) old.getAdapter();
                    ListView newP = (ListView) v.getParent();
                    ForTwoAdapter dest = (ForTwoAdapter) newP.getAdapter();
                    List<Task> destList = dest.getList();

                    int remLoc = srcList.indexOf(passed);
                    int insLoc = destList.indexOf(t);

                    if (srcList != destList || remLoc != insLoc) {
                        if (remove (srcList, passed)) {
                            destList.add (insLoc, passed);
                        }

                        src.notifyDataSetChanged();
                        dest.notifyDataSetChanged();
                    }

                    v.setBackgroundColor (resumeColor);
                    break;
                case DragEvent.ACTION_DRAG_ENDED :
                    v.setBackgroundColor (resumeColor);
                default : break;
            }

            return true;
        }
    }

    private void initTasks() {
        todoTasks = new ArrayList<Task>();
        doneTasks = new ArrayList<Task>();
        try {
            BufferedInputStream bf = new BufferedInputStream(openFileInput("taskList.val"));
            BufferedReader reader = new BufferedReader (new InputStreamReader (bf));
            String line;
            try {
                //assuming no newline characters in desc string
                while ((line = reader.readLine()) != null) {
                    String[] curr = line.split ("\\|");
                    Task t = new Task (curr[0], curr[1]);
                    if (curr[2].equals("todo"))
                        todoTasks.add (t);
                    else doneTasks.add (t);
                }
                reader.close();
            } catch (IOException e) {}
        } catch (FileNotFoundException e) {}
    }

    private boolean remove (List<Task> l, Task t) {
        return l.remove (t);
    }

    private boolean insert (List<Task> l, Task t) {
        return l.add (t);
    }

    public void addTask (View v) {
        Intent i = new Intent (this, AddTaskScreen2.class);
        startActivityForResult (i, 1);
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        if (code == 1) {
            if (result == Activity.RESULT_OK) {
                String[] info = data.getStringArrayExtra("info");
                if (info != null) {
                    Task t = new Task(info[0], info[1]);
                    todoTasks.add(t);
                }
                ForTwoAdapter adapt = (ForTwoAdapter) listTodo.getAdapter();
                adapt.notifyDataSetChanged();
            }
        } else if (code == 2) {
            if (result == Activity.RESULT_OK) {
                String mode = data.getStringExtra("mode");
                int pos = data.getIntExtra ("pos", -1);
                int from = data.getIntExtra ("from", 0);
                ListView which = (ListView) findViewById (from);
                ForTwoAdapter adapt = (ForTwoAdapter) which.getAdapter();
                List<Task> arr = adapt.getList();
                Task t = arr.get (pos);
                if (mode.equals ("save")) {
                    String[] info = data.getStringArrayExtra ("info");
                    Task n = new Task (info[0], info[1]);
                    if (remove (arr, t))
                        arr.add (pos, n);
                } else {
                    remove (arr, t);
                }
                adapt.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveFiles();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveFiles();
    }

    private void saveFiles() {
        try {
            BufferedOutputStream bf = new BufferedOutputStream (openFileOutput("taskList.val", Context.MODE_PRIVATE));
            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter (bf));
            String curr;
            for (Task t : todoTasks) {
                curr = t.title + "|" + t.desc + "|todo\n";
                writer.write (curr);
            }
            for (Task t : doneTasks) {
                curr = t.title + "|" + t.desc + "|done\n";
                writer.write (curr);
            }
            writer.close();
        } catch (Exception e) {}
    }
}
