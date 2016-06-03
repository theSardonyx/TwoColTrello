package com.azeusojt.kryzlpascual.twocoltrello;

import android.app.Activity;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Task> todoTasks, doneTasks;
    ListView listTodo, listDone;

    int resumeColor = 0x000000;

    String[] bands = {"One OK Rock", "Kagamine Rin/Len", "Yiruma", "Kalafina", "Blue"};
    String[] songs = {"Clock Strikes, Pierce, Et Cetera, Kagerou, Kemuri, Nichijou Evolution, Jibun Rock, Paper Planes, Memories",
                    "Fire Flower, Suki Daisuki, Proof of Life, Soundless Voice, Ike Lenka, Kami Hikouki, Shuujin, Nazotoki, Nazokake",
                    "Letter, River Flows in You, Kiss the Rain, Chaconne, I",
                    "Magia, Heavenly Blue, Aria, Oblivious, Progressive, Moonfesta, Kizuato, Serenato, Kyrie, Manten, To the Beginning",
                    "Black Box, Bubblin', Fly By, All Rise, Taste It, How's a Man Supposed to Change"};
    String[] toasties = {"Best Rock Band Evah", "Twiins", "Piano Prodigy", "Keiko, Hikaru, Wakana", "Some British Boy Band"};

    public class Task {
        String title, desc;

        Task (String title, String desc) {
            this.title = title;
            this.desc = desc;
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
            Toast.makeText(MainActivity.this, toasties[position], Toast.LENGTH_SHORT).show();
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

        for (int i = 0; i < bands.length; i++) {
            todoTasks.add (new Task (bands[i], songs[i]));
        }
    }

    private boolean remove (List<Task> l, Task t) {
        return l.remove (t);
    }

    private boolean insert (List<Task> l, Task t) {
        return l.add (t);
    }

    public void addTask (View v) {
        Intent i = new Intent (this, AddTaskScreen2.class);
        startActivity (i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        String[] info = i.getStringArrayExtra("info");
        if (info != null) {
            Task t = new Task(info[0], info[1]);
            todoTasks.add(t);
        }
    }
}
