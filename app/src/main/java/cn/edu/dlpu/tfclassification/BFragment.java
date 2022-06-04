package cn.edu.dlpu.tfclassification;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BFragment extends Fragment {

    DatabaseHelper db;
    private boolean run = false;
    private final Handler handler = new Handler();

    Button add_data;
    EditText add_name;


    ArrayList<String> listItem;
    ArrayAdapter adapter;
    ListView userlist;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bfragment, container, false);
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        //构建试图 初始化页面中的控件
////        listView2 = (ListView) getActivity().findViewById(R.id.historyListView);
//
//
//    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            // TO DO SOMETHING
            listItem.clear();
            viewData();
            handler.postDelayed(this,2000);
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // 一秒刷新一次
        run = true;
        handler.postDelayed(task,2000);

        db = new DatabaseHelper(getActivity());


        listItem = new ArrayList<>();

//
//        Button add_data = getActivity().findViewById(R.id.add_data);
//        EditText add_name = getActivity().findViewById(R.id.add_name);
        userlist = getActivity().findViewById(R.id.historyListView);

        viewData();

//        userlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                String text = userlist.getItemAtPosition(i).toString();
//                Toast.makeText(getActivity(),""+text,Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//
//        add_data.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String name = add_name.getText().toString();
//                if (!name.equals("")&&db.insertData(name)){
//                    Toast.makeText(getActivity(),"Data Added",Toast.LENGTH_SHORT).show();
//                    add_name.setText("");
//                    listItem.clear();
//                    viewData();
//                }
//                else {
//                    Toast.makeText(getActivity(),"Data Not Added",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    public void viewData() {
        Cursor cursor = db.viewData();
        if (cursor.getCount()==0){
            Toast.makeText(getActivity(),"No data to show",Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext()){
                listItem.add(cursor.getString(1));//index is name index 0 is ID
            }
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,listItem);
            userlist.setAdapter(adapter);
        }
    }
}


