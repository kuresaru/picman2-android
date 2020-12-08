package top.scraft.picman2.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import top.scraft.picman2.R;
import top.scraft.picman2.ServerController;
import top.scraft.picman2.activity.adapter.SearchAdapter;
import top.scraft.picman2.data.UserDetail;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.dao.Picture;
import top.scraft.picman2.storage.dao.gen.PictureDao;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int ACTIVITY_RESULT_LOGIN = 100;

    private SearchAdapter searchAdapter;
    private List<Picture> searchResults = new ArrayList<>();

    private MenuItem systemMenuItem;
    private MenuItem loginMenuItem;
    private MenuItem logoutMenuItem;

    private TextInputEditText inputSearch;

    private ServerController serverController;
    private String sacLoginUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // find views
        inputSearch = findViewById(R.id.inputSearch);
        findViewById(R.id.buttonSearch).setOnClickListener(this);
        RecyclerView recyclerView = findViewById(R.id.main_gallery);
        // get controllers
        serverController = ServerController.getInstance(getApplicationContext());
        // init
        searchAdapter = new SearchAdapter(this, searchResults);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(searchAdapter);
        requestPermissions();
        checkLogin();
    }

    private void checkLogin() {
        if (loginMenuItem != null) {
            systemMenuItem.setVisible(false);
            loginMenuItem.setVisible(false);
            logoutMenuItem.setVisible(false);
        }
        new Thread(() -> {
            UserDetail userDetail = serverController.getUserDetail();
            if (userDetail != null) {
                runOnUiThread(() -> {
                    if (userDetail.isLoggedIn()) {
                        Toast.makeText(this, "Already logged in: " + userDetail.getUsername(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Not login", Toast.LENGTH_SHORT).show();
                        sacLoginUrl = userDetail.getSacLoginUrl();
                    }
                    systemMenuItem.setVisible(userDetail.isAdmin());
                    loginMenuItem.setVisible((!userDetail.isLoggedIn()) && (sacLoginUrl != null));
                    logoutMenuItem.setVisible(userDetail.isLoggedIn());
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonSearch) {
            Editable searchText = inputSearch.getText();
            if (searchText != null) {
                String keyword = searchText.toString();
                if (keyword.isEmpty()) {
                    Toast.makeText(this, "搜索关键字不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    PictureDao pDao = PicmanStorage.getInstance(getApplicationContext()).getPictureDao();
                    searchResults.clear();
                    searchResults.addAll(pDao.queryBuilder().where(PictureDao.Properties.Description.like("%" + searchText.toString() + "%")).list()); // FIXME 遇到描述有%的怎么办?
                    // TODO 根据Tag找图片
                    searchAdapter.notifyDataSetChanged();
                    if (searchResults.size() == 0) {
                        Toast.makeText(this, "搜索结果为空", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ACTIVITY_RESULT_LOGIN) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String sact = data.getStringExtra("SACT");
                if (sact != null) {
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    serverController.setSact(sact);
                    System.out.println(sact);
                    checkLogin();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        systemMenuItem = menu.findItem(R.id.menu_main_system);
        loginMenuItem = menu.findItem(R.id.menu_main_login);
        logoutMenuItem = menu.findItem(R.id.menu_main_logout);
        systemMenuItem.setVisible(false);
        loginMenuItem.setVisible(false);
        logoutMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_login: {
                if ("testuser".equals(sacLoginUrl)) {
                    Toast.makeText(this, "testuser", Toast.LENGTH_SHORT).show();
                    serverController.setSact("0123456789abcdef0123456789abcdef");
                    checkLogin();
                } else {
                    Intent intent = new Intent(this, BrowserActivity.class);
                    intent.putExtra("URL", sacLoginUrl);
                    intent.putExtra("TITLE", "登录");
                    startActivityForResult(intent, ACTIVITY_RESULT_LOGIN);
                }
                break;
            }
            case R.id.menu_main_logout: {
                new Thread(() -> {
                    serverController.logout();
                    runOnUiThread(this::checkLogin);
                }).start();
                break;
            }
            case R.id.menu_main_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case R.id.menu_main_ping:
                menuPing();
                break;
            case R.id.menu_main_sync:
                menuSync();
                break;
            case R.id.menu_main_piclib:
                startActivity(new Intent(this, PicLibManagerActivity.class));
                break;
        }
        return false;
    }

    private void menuPing() {
        Toast.makeText(this, "正在测试", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            boolean ping = serverController.ping();
            final String text = ping ? "服务器连接正常" : "服务器连接失败";
            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    text, Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void menuSync() {
        new Thread(() -> serverController.getPicLibs()).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length == 2) {
            for (int v : grantResults) {
                if (v != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "未授权存储读写权限,无法使用", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, perms, 100);
        }
    }

}
