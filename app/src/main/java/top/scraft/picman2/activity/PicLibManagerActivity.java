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
import top.scraft.picman2.R;
import top.scraft.picman2.activity.adapter.PiclibManagerAdapter;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.dao.PictureLibrary;

import java.util.ArrayList;

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
            dialog.setPositiveButton(R.string.text_create, (dialogInterface, which) -> {
                String name = nameEdit.getEditText().getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(this, "请输入图库名", Toast.LENGTH_SHORT).show();
                } else {
                    PictureLibrary newLib = new PictureLibrary(null, null, name, null, true);
                    picmanStorage.getPictureLibraryDao().insert(newLib);
                    piclibs.add(newLib);
                    adapter.notifyDataSetChanged();
                }
            });
            dialog.setNegativeButton(R.string.text_cancel, null);
            dialog.show();
        });

        piclibs.addAll(picmanStorage.getPictureLibraryDao().queryBuilder().list());
        adapter.notifyDataSetChanged();
    }

}