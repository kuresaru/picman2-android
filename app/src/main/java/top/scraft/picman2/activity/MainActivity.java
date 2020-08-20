package top.scraft.picman2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import top.scraft.picman2.R;
import top.scraft.picman2.ServerController;
import top.scraft.picman2.data.UserDetail;
import top.scraft.picman2.storage.DatabaseController;
import top.scraft.picman2.storage.PictureStorageController;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int ACTIVITY_RESULT_LOGIN = 100;

    private MenuItem piclibMenuItem;
    private MenuItem systemMenuItem;
    private MenuItem loginMenuItem;
    private MenuItem logoutMenuItem;

    private TextInputEditText inputSearch;

    private ServerController serverController;
    private DatabaseController databaseController;
    private PictureStorageController pictureStorage;
    private String sacLoginUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // find views
        inputSearch = findViewById(R.id.inputSearch);
        findViewById(R.id.buttonSearch).setOnClickListener(this);
        // get controllers
        serverController = ServerController.getInstance(getApplicationContext());
        databaseController = DatabaseController.getInstance(getApplicationContext());
        pictureStorage = PictureStorageController.getInstance(getApplicationContext());
        // init
        checkLogin();
    }

    private void checkLogin() {
        if (loginMenuItem != null) {
            piclibMenuItem.setVisible(false);
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
                    piclibMenuItem.setVisible(userDetail.isLoggedIn());
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
                    Toast.makeText(this, keyword, Toast.LENGTH_SHORT).show(); // test
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
        piclibMenuItem = menu.findItem(R.id.menu_main_piclib);
        systemMenuItem = menu.findItem(R.id.menu_main_system);
        loginMenuItem = menu.findItem(R.id.menu_main_login);
        logoutMenuItem = menu.findItem(R.id.menu_main_logout);
        piclibMenuItem.setVisible(false);
        systemMenuItem.setVisible(false);
        loginMenuItem.setVisible(false);
        logoutMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_login: {
                Intent intent = new Intent(this, BrowserActivity.class);
                intent.putExtra("URL", sacLoginUrl);
                intent.putExtra("TITLE", "登录");
                startActivityForResult(intent, ACTIVITY_RESULT_LOGIN);
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

}
