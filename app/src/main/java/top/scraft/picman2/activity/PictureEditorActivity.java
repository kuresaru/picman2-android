package top.scraft.picman2.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import top.scraft.picman2.R;
import top.scraft.picman2.utils.FileUtils;

import java.io.File;

public class PictureEditorActivity extends AppCompatActivity {

    private TextInputEditText pidEdit;
    private TextInputEditText tagEditor;
    private ChipGroup tags;
    private ImageView imagePreview;

    private LayoutInflater layoutInflater;
    private File imageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_editor);
        pidEdit = findViewById(R.id.pid);
        tagEditor = findViewById(R.id.picture_editor_add_tag);
        tags = findViewById(R.id.picture_editor_tags);
        imagePreview = findViewById(R.id.imageEditorPreview);
        layoutInflater = LayoutInflater.from(this);
        tagEditor.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Editable newTag = tagEditor.getText();
                if (newTag != null) {
                    String tag = newTag.toString();
                    if (isTagDummy(tag)) {
                        Toast.makeText(this, "标签已存在", Toast.LENGTH_SHORT).show();
                    } else {
                        addTag(tag);
                        tagEditor.setText(null);
                    }
                }
                return true;
            }
            return false;
        });
        loadImageFile();
        if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
            // 加载显示图片
            imagePreview.setImageURI(Uri.fromFile(imageFile));
            // 计算pid
            new Thread(() -> {
                String md5 = FileUtils.fileMD5(imageFile);
                if (md5 != null) {
                    String path = imageFile.getAbsolutePath();
                    String pid = md5 + path.substring(path.lastIndexOf('.'));
                    runOnUiThread(() -> pidEdit.setText(pid));
                }
            }).start();
        } else {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImageFile() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String file = extras.getString("file");
                imageFile = new File(file);
            }
        }
    }

    private void addTag(String tag) {
        View view = layoutInflater.inflate(R.layout.item_pictire_editor_tag, null);
        Chip chip = view.findViewById(R.id.item_picture_editor_tag);
        chip.setText(tag);
        chip.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(tag);
            builder.setMessage(tag);
            builder.setPositiveButton("编辑", (dialog, which) -> {
                tagEditor.setText(tag);
                tags.removeView(view);
            });
            builder.setNegativeButton("删除", (dialog, which) -> tags.removeView(view));
            builder.show();
        });
        tags.addView(view);
    }

    private boolean isTagDummy(String tag) {
        for (int i = 0; i < tags.getChildCount(); i++) {
            View v = tags.getChildAt(i);
            Chip chip = v.findViewById(R.id.item_picture_editor_tag);
            if (tag.equals(chip.getText().toString())) {
                return true;
            }
        }
        return false;
    }

}