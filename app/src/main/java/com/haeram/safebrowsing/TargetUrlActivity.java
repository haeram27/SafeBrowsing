package com.haeram.safebrowsing;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.haeram.safebrowsing.realm.RlmObjUrl;
import com.haeram.tools.android.file.AssetUtil;
import com.haeram.tools.android.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TargetUrlActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private Realm mRealm;
    private RealmResults<RlmObjUrl> mRlmResult_TargetUrls;
    private UrlAdapter mAdapter;

    private AutoCompleteTextView mACTVUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_target_url);
        setTitle("Target Url");


        mRecyclerView = (RecyclerView)findViewById(R.id.recycleview_url);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.requestFocus();

        //realm init
        mRealm = Realm.getDefaultInstance();
        mRlmResult_TargetUrls = mRealm.where(RlmObjUrl.class).findAllAsync();
        mAdapter = new UrlAdapter(mRlmResult_TargetUrls);
        mRecyclerView.setAdapter(mAdapter);
        mRlmResult_TargetUrls.addChangeListener(new RealmChangeListener<RealmResults<RlmObjUrl>>()
        {
            @Override
            public void onChange(RealmResults<RlmObjUrl> results) {
                Log.d(TAG,"Target changed!!");

                // Query result is updated in real time
                // Todo: check updated result
                int count = results.size();
                Log.d(TAG, String.format("results Size: %d", count));
                mAdapter.notifyDataSetChanged();
            }
        });

        mACTVUrl = (AutoCompleteTextView)findViewById(R.id.textview_url_input);
        mACTVUrl.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mACTVUrl.setText("http://");
                } else {
                    String str = mACTVUrl.getText().toString();
                    if(str.isEmpty() || "http://".equals(str)) mACTVUrl.setText("Input url...");
                }
            }
        });

        Button BTNAdd = (Button)findViewById(R.id.button_url_add);
        BTNAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = mACTVUrl.getText().toString();
                if (!url.isEmpty()) {
                    rlmQueryAsync(new Realm.Transaction(){
                        @Override
                        public void execute(Realm bgRealm) {
                            RlmObjUrl item = new RlmObjUrl();
                            item.setChecked(true);
                            item.setUrl(url);
                            bgRealm.copyToRealmOrUpdate(item);
                        }
                    });
                }
                mACTVUrl.clearFocus();
                mRecyclerView.scrollToPosition(mRlmResult_TargetUrls.size());
            }
        });

        ToggleButton BTNCheckAll = (ToggleButton)findViewById(R.id.button_check_all);
        BTNCheckAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((ToggleButton)v).isChecked()) {
                    rlmQueryAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            RealmResults<RlmObjUrl> results =
                                    bgRealm.where(RlmObjUrl.class)
                                            .findAll();
                            for (int cnt = 0; cnt < results.size(); cnt++) {
                                results.get(cnt).setChecked(true);
                            }
                        }
                    });
                } else {
                    rlmQueryAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            RealmResults<RlmObjUrl> results =
                                    bgRealm.where(RlmObjUrl.class)
                                            .findAll();
                            for (int cnt = 0; cnt < results.size(); cnt++) {
                                results.get(cnt).setChecked(false);
                            }
                        }
                    });
                }
            }
        });

        Button BTNClear = (Button)findViewById(R.id.button_clear);
        BTNClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlmQueryAsync(new Realm.Transaction(){
                    @Override
                    public void execute(Realm bgRealm) {
                        RealmResults<RlmObjUrl> results =
                                bgRealm.where(RlmObjUrl.class)
                                        .findAll();
                        results.deleteAllFromRealm();
                    }
                });
            }
        });

        Button BTNReset = (Button)findViewById(R.id.button_reset);
        BTNReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlmQueryAsync(new Realm.Transaction(){
                    @Override
                    public void execute(Realm bgRealm) {
                        RealmResults<RlmObjUrl> results =
                                bgRealm.where(RlmObjUrl.class)
                                        .findAll();
                        results.deleteAllFromRealm();
                    }
                });
                initTargetUrls();
            }
        });

        Button BTNSave = (Button)findViewById(R.id.button_save);
        BTNSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = getApplicationContext();
                OrderedRealmCollectionSnapshot snapshot = mRlmResult_TargetUrls.createSnapshot();
                StringBuilder sb = new StringBuilder();

                int size = snapshot.size();
                for(int i = 0; i < size; i++)
                {
                    RlmObjUrl obj = (RlmObjUrl) snapshot.get(i);
                    sb.append(obj.getUrl());
                    sb.append("\n");
                }

                try
                {
                    File dir = FileUtil.getExternalStoragePrivateFile(ctx, null, "urls");

                    if(dir != null)
                    {
                        String fileName = String.format("%s/urls_%s.txt",
                                dir.getCanonicalPath(),  FileUtil.getCurrentTimeString());
                        FileUtil.toTextFile(fileName, sb.toString());
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();

                    Toast.makeText(ctx, "Failed to save file",
                            Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(ctx, "Success to save file",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        // Cleanup Realm Resource
        mRlmResult_TargetUrls.removeAllChangeListeners(); // ChangeListener should be removed!
        mRlmResult_TargetUrls = null;
        mAdapter = null;
        mRealm.close();
        mRealm = null;
    }

    private void rlmQueryAsync(Realm.Transaction transaction)
    {
        mRealm.executeTransactionAsync(transaction,
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Realm Transaction was a success.");
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Log.d(TAG, "Realm Transaction failed and was automatically canceled.");
                        Log.d(TAG, error.getMessage());
                    }
                });
    }

    private void initExplain() {
        final ArrayList<RlmObjUrl> list = new ArrayList<>();

        String[] ar = new String[] {
            "\"CHECK\" - check/uncheck all items",
            "\"CLEAR\" - remove all",
            "\"RESET\" - remove all and add init urls",
            "\"SAVE\" - save list as file",
            "PLEASE CLICK \"RESET\" NOW TO START TEST!!!"
        };

        for (int i = 0; i < ar.length; i++) {
            RlmObjUrl item = new RlmObjUrl();
            item.setChecked(false);
            item.setUrl(ar[i]);
            list.add(item);
        }

        rlmQueryAsync(new Realm.Transaction(){
            @Override
            public void execute(Realm bgRealm) {
                for (int i = 0; i < list.size(); i++) {
                    RlmObjUrl item = list.get(i);
                    bgRealm.copyToRealmOrUpdate(item);
                }
            }
        });
    }

    private void initTargetUrls() {
        final ArrayList<RlmObjUrl> list = new ArrayList<>();
        try {
            String urls = AssetUtil.readAssetText(getApplicationContext(), "urls");

            Log.d(TAG,"initTargetUrls");
            if (!TextUtils.isEmpty(urls))
            {
                String[] ar = urls.split("\\n");
                for (int i = 0; i < ar.length; i++)
                {
                    RlmObjUrl item = new RlmObjUrl();
                    item.setChecked(false);
                    item.setUrl(ar[i]);
                    list.add(item);
                }
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        rlmQueryAsync(new Realm.Transaction(){
            @Override
            public void execute(Realm bgRealm) {
                for (int i = 0; i < list.size(); i++) {
                    RlmObjUrl item = list.get(i);
                    bgRealm.copyToRealmOrUpdate(item);
                }
            }
        });
    }

    class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.ViewHolder> {
        private List<RlmObjUrl> mList;

        public UrlAdapter(List<RlmObjUrl> list) { this.mList = list; }

        // inflating item layout and attach it to RecycleView
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_target_url, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        //insert data to ViewHolder such as getView() of ListView
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RlmObjUrl item = mList.get(position);
            holder.urlText.setText(item.getUrl());
            holder.checkBox.setChecked(item.isChecked());
            holder.checkBox.setOnClickListener(new CheckBoxClickListener(item.getUrl()));
        }

        //define how many data should be spreaded by list
        @Override
        public int getItemCount() { return mList.size(); }

        //ViewHolder has a role to preserve one View
        public class ViewHolder extends RecyclerView.ViewHolder {
            public CheckBox checkBox;
            public TextView urlText;

            public ViewHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView.findViewById(R.id.item_checkbox_select);
                urlText = (TextView) itemView.findViewById(R.id.item_textview_url);
            }
        }

        class CheckBoxClickListener implements View.OnClickListener {
            String mUrl;

            public CheckBoxClickListener (String url)
            { this.mUrl = url; }

            @Override
            public void onClick(final View v) {
                rlmQueryAsync(new Realm.Transaction(){
                    @Override
                    public void execute(Realm bgRealm) {
                        RlmObjUrl obj =
                                bgRealm.where(RlmObjUrl.class).equalTo("url", mUrl).findFirst();
                        obj.setChecked(((CheckBox)v).isChecked());
                    }
                });
            }
        }
    }
}
