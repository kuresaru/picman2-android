package top.scraft.picman2.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import top.scraft.picman2.R;
import top.scraft.picman2.activity.adapter.PiclibSpinnerAdapter;
import top.scraft.picman2.server.ServerController;
import top.scraft.picman2.server.data.PictureDetail;
import top.scraft.picman2.server.data.Result;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.PictureStorageController;
import top.scraft.picman2.storage.dao.PiclibPictureMap;
import top.scraft.picman2.storage.dao.Picture;
import top.scraft.picman2.storage.dao.PictureLibrary;
import top.scraft.picman2.storage.dao.PictureTag;
import top.scraft.picman2.storage.dao.gen.DaoSession;
import top.scraft.picman2.storage.dao.gen.PiclibPictureMapDao;
import top.scraft.picman2.storage.dao.gen.PictureDao;
import top.scraft.picman2.storage.dao.gen.PictureTagDao;
import top.scraft.picman2.utils.Utils;

public class PictureEditorActivity extends AppCompatActivity {

    private EditText pidEdit;
    private Spinner piclibSelector;
    private TextInputLayout descriptionEditor;
    private EditText tagEditor;
    private ChipGroup tags;
    private ImageView imagePreview;

    private LayoutInflater layoutInflater;
    private Uri pictureUri = null;
    private byte[] pictureFile;
    private String pid = null;
    private final Set<String> tagList = new HashSet<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pidEdit = ((TextInputLayout) findViewById(R.id.pid)).getEditText();
        piclibSelector = findViewById(R.id.piclib_selector);
        descriptionEditor = findViewById(R.id.picture_editor_description);
        tagEditor = ((TextInputLayout) findViewById(R.id.picture_editor_add_tag)).getEditText();
        tags = findViewById(R.id.picture_editor_tags);
        imagePreview = findViewById(R.id.imageEditorPreview);
        layoutInflater = LayoutInflater.from(this);

        tagEditor.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Editable newTag = tagEditor.getText();
                if (newTag != null && newTag.length() > 0) {
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

        loadImage();

        PiclibSpinnerAdapter adapter = new PiclibSpinnerAdapter(this,
                PicmanStorage.getInstance(getApplicationContext()).getDaoSession().getPictureLibraryDao().queryBuilder().list());
        piclibSelector.setAdapter(adapter);
        piclibSelector.setVisibility(View.VISIBLE);
        if (adapter.getCount() == 0) {
            Toast.makeText(this, "没有任何图库, 请先新建图库", Toast.LENGTH_LONG).show();
            finish();
        }

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.save) {
                saveOrUpdatePicture();
                return true;
            }
            return false;
        });
    }

    private void saveOrUpdatePicture() {
        if (pid == null) {
            Toast.makeText(this, "图片未准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        String description = descriptionEditor.getEditText().getText().toString();
        if (!(description != null && description.length() > 0)) {
            Toast.makeText(this, "请输入图片描述", Toast.LENGTH_SHORT).show();
            return;
        }
        PictureLibrary library = (PictureLibrary) piclibSelector.getSelectedItem();

        new Thread(() -> {
            PicmanStorage picmanStorage = PicmanStorage.getInstance(getApplicationContext());
            DaoSession daoSession = picmanStorage.getDaoSession();
            PictureDao pDao = daoSession.getPictureDao();
            PictureTagDao tDao = daoSession.getPictureTagDao();
            PiclibPictureMapDao lpmDao = daoSession.getPiclibPictureMapDao();

            // 如果离线直接保存本地 如果在线先上传 成功后再保存本地
            if (!library.getOffline()) {
                // online
                ServerController serverController = ServerController.getInstance(getApplicationContext());
                Result<PictureDetail> updateResult = serverController.updatePictureMeta(library.getLid(), pid, description, tagList);
                if (updateResult.getCode() != 200) {
                    Utils.toastThread(this, updateResult.getMessage());
                    return;
                }
                assert updateResult.getData() != null;
                if (!updateResult.getData().isValid()) {
                    Result<Object> uploadResult = serverController.uploadPicture(library.getLid(), pid, pictureFile);
                    if (uploadResult.getCode() != 200) {
                        Utils.toastThread(this, "上传图片失败: " + uploadResult.getMessage());
                    }
                }
            }
            Picture oldRecord = pDao.queryBuilder().where(PictureDao.Properties.Pid.eq(pid)).unique();
            boolean newPicture = oldRecord == null;
            if (newPicture) {
                oldRecord = new Picture();
                oldRecord.setPid(pid);
                oldRecord.setCreateTime(System.currentTimeMillis() / 1000);
                oldRecord.setFileSize((long) pictureFile.length);
                Bitmap bmp = BitmapFactory.decodeByteArray(pictureFile, 0, pictureFile.length); // TODO gif
                oldRecord.setWidth(bmp.getWidth());
                oldRecord.setHeight(bmp.getHeight());
            }
            oldRecord.setDescription(description);
            oldRecord.setLastModify(System.currentTimeMillis() / 1000);
            pDao.insertOrReplace(oldRecord);
            // 更新Tag
            tDao.queryBuilder().where(PictureTagDao.Properties.AppInternalPid.eq(oldRecord.getAppInternalPid())).buildDelete().executeDeleteWithoutDetachingEntities();
            List<PictureTag> newTags = new ArrayList<>();
            for (String s : tagList) {
                PictureTag tag = new PictureTag();
                tag.setAppInternalPid(oldRecord.getAppInternalPid());
                tag.setTag(s);
                newTags.add(tag);
            }
            tDao.insertInTx(newTags);
            // 更新piclib
            lpmDao.queryBuilder().where(PiclibPictureMapDao.Properties.AppInternalLid.eq(library.getAppInternalLid()),
                    PiclibPictureMapDao.Properties.AppInternalPid.eq(oldRecord.getAppInternalPid())).buildDelete().executeDeleteWithoutDetachingEntities();
            PiclibPictureMap lpMap = new PiclibPictureMap();
            lpMap.setAppInternalLid(library.getAppInternalLid());
            lpMap.setAppInternalPid(oldRecord.getAppInternalPid());
            lpmDao.insert(lpMap);
            // 保存图片文件
            PictureStorageController storage = picmanStorage.getPictureStorage();
            if (!storage.getPicturePath(pid).exists()) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(pictureUri);
                    storage.savePicture(inputStream, pid);
                    inputStream.close();
                    Utils.toastThread(this, "图片已保存");
                } catch (IOException e) {
                    Utils.toastThread(this, "保存失败, 请检查存储权限和存储空间 " + e.getLocalizedMessage());
                    oldRecord.update();
                }
            } else {
                Utils.toastThread(this, "图片已更新");
            }
            runOnUiThread(this::finish);
        }).start();
    }

    private void loadImage() {
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (Intent.ACTION_SEND.equals(action)) {
                pictureUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            } else if (extras != null) {
                String uriString = extras.getString("picture_uri");
                pictureUri = Uri.parse(uriString);
            }
        }
        if (pictureUri != null) {
            imagePreview.setImageURI(pictureUri);
            pid = null;
            // 计算pid 加载记录
            new Thread(() -> {
                try {
                    pictureFile = Utils.readContent(getContentResolver(), pictureUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Utils.toastThread(PictureEditorActivity.this, "加载图片失败 " + e.getLocalizedMessage());
                    finish();
                    return;
                }
                String md5 = Utils.md5(pictureFile);
                String fileExtName = Utils.contentToFilePath(getContentResolver(), pictureUri);
                if (fileExtName != null) {
                    fileExtName = fileExtName.substring(fileExtName.lastIndexOf('.'));
                    pid = md5 + fileExtName;
                }
                if (pid == null) {
                    Utils.toastThread(PictureEditorActivity.this, "计算pid失败/分享无效");
                    finish();
                    return;
                }
                PictureDao dao = PicmanStorage.getInstance(getApplicationContext()).getDaoSession().getPictureDao();
                Picture oldRecord = dao.queryBuilder().where(PictureDao.Properties.Pid.eq(pid)).unique();
                runOnUiThread(() -> {
                    pidEdit.setText(pid);
                    if (oldRecord != null) {
                        Objects.requireNonNull(descriptionEditor.getEditText()).setText(oldRecord.getDescription());
                        for (PictureTag tag : oldRecord.getTags()) {
                            addTag(tag.getTag());
                        }
                    }
                });
            }).start();
        } else {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
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
                tagList.remove(tag);
            });
            builder.setNegativeButton("删除", (dialog, which) -> tags.removeView(view));
            builder.show();
        });
        tags.addView(view);
        tagList.add(tag);
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