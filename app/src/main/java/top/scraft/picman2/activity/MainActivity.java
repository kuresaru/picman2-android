package top.scraft.picman2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import top.scraft.picman2.R;
import top.scraft.picman2.ServerController;
import top.scraft.picman2.data.UserDetail;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int ACTIVITY_RESULT_LOGIN = 100;

//    private SharedPreferences sharedPreferences;
    private Button button;
    private ServerController serverController;
    private MenuItem loginMenuItem;
    private String sacLoginUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        serverController = new ServerController(this);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);
        checkLogin();
    }

    private void checkLogin() {
        if (loginMenuItem != null) {
            loginMenuItem.setVisible(false);
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
                    loginMenuItem.setVisible((!userDetail.isLoggedIn()) && (sacLoginUrl != null));
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
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
        loginMenuItem = menu.findItem(R.id.menu_main_login);
        loginMenuItem.setVisible(false);
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
