package top.scraft.picman2.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import top.scraft.picman2.R;
import top.scraft.picman2.activity.adapter.PiclibManagerAdapter;
import top.scraft.picman2.server.ServerController;
import top.scraft.picman2.server.data.LibDetails;
import top.scraft.picman2.server.data.Result;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.dao.PictureLibrary;

public class PicLibManagerActivity extends AppCompatActivity {

    private final ArrayList<PictureLibrary> piclibs = new ArrayList<>();

    private PicmanStorage picmanStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_lib_manager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        picmanStorage = PicmanStorage.getInstance(getApplicationContext());

        PiclibManagerAdapter adapter = new PiclibManagerAdapter(this, piclibs);

        ListView listView = findViewById(R.id.list_piclib);
        listView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_piclib_create);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_piclib_create, null);
            TextInputLayout nameEdit = dialogView.findViewById(R.id.dialog_piclib_create_name);
            dialog.setTitle("新建图库");
            dialog.setView(dialogView);
            dialog.setNeutralButton(R.string.text_create_offline_library, (d, w) -> onCreateClick(true, nameEdit, adapter));
            dialog.setPositiveButton(R.string.text_create_online_library, (d, w) -> onCreateClick(false, nameEdit, adapter));
            dialog.setNegativeButton(R.string.text_cancel, null);
            dialog.show();
        });

        piclibs.addAll(picmanStorage.getDaoSession().getPictureLibraryDao().queryBuilder().list());
        adapter.notifyDataSetChanged();
    }

    private void onCreateClick(boolean offline, TextInputLayout nameEdit, PiclibManagerAdapter adapter) {
        String name = nameEdit.getEditText().getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入图库名", Toast.LENGTH_SHORT).show();
        } else {
            new Thread(() -> {
                PictureLibrary newLib;
                if (!offline) {
                    Result<LibDetails> result = ServerController.getInstance(getApplicationContext()).createLibrary(name);
                    if (result == null) {
                        runOnUiThread(() -> Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    if (result.getCode() != 200) {
                        runOnUiThread(() -> Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show());
                        return;
                    }
                    LibDetails details = result.getData();
                    newLib = new PictureLibrary(null, details.getLid(), details.getName(),
                            details.getLastUpdate(), false, details.isReadonly());
                } else {
                    newLib = new PictureLibrary(null, null, name,
                            System.currentTimeMillis() / 1000, true, false);
                }
                picmanStorage.getDaoSession().getPictureLibraryDao().insert(newLib);
                piclibs.add(newLib);
                runOnUiThread(adapter::notifyDataSetChanged);
            }).start();
        }
    }

}