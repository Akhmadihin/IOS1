package com.example.kubiki;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();

    }
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");
        webView.setWebViewClient(new CustomWebViewClient());
        webView.loadUrl("https://sites.google.com/view/kubiki?usp=sharing");
    }
    private void launchWhatsApp(String url) {
        try {
            String whatsappUrl = url.replace("https://api.whatsapp.com/send?phone=", "whatsapp://send?phone=")
                    .replace("https://wa.me/", "whatsapp://send?phone=");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(whatsappUrl));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "WhatsApp не установлен", Toast.LENGTH_SHORT).show();
                webView.loadUrl(url);
            }
        } catch (Exception e) {
            Log.e("WhatsAppError", "Failed to open WhatsApp", e);
            webView.loadUrl(url);
        }
    }

    private void openTelegramLink(String path) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (path.startsWith("join?invite=")) {
                String inviteCode = path.substring("join?invite=".length());
                intent.setData(Uri.parse("tg://join?invite=" + inviteCode));
            } else if (path.startsWith("resolve?domain=")) {
                String domain = path.substring("resolve?domain=".length());
                intent.setData(Uri.parse("tg://resolve?domain=" + domain));
            } else {
                intent.setData(Uri.parse("tg://" + path));
            }

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                String webUrl = "https://t.me/" + path;
                webView.loadUrl(webUrl);
            }
        } catch (Exception e) {
            Log.e("TelegramError", "Failed to open Telegram", e);
            Toast.makeText(this, "Ошибка при открытии Telegram", Toast.LENGTH_SHORT).show();
        }
    }
    public class WebAppInterface {
        private MainActivity mActivity;

        public WebAppInterface(MainActivity activity) {
            this.mActivity = activity;
        }

        @JavascriptInterface
        public void openWhatsApp(String phone) {
            mActivity.runOnUiThread(() -> mActivity.launchWhatsApp("https://api.whatsapp.com/send?phone=" + phone));
        }

        @JavascriptInterface
        public void openTelegram(String path) {
            mActivity.runOnUiThread(() -> mActivity.openTelegramLink(path));
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url == null) return false;

            try {
                if (url.startsWith("tg://") || url.startsWith("tg:") || url.contains("t.me/")) {
                    String path = url.startsWith("tg://") ? url.substring("tg://".length()) :
                            url.startsWith("tg:") ? url.substring("tg:".length()) :
                                    url.substring(url.indexOf("t.me/") + 5);
                    openTelegramLink(path);
                    return true;
                } else if (url.startsWith("whatsapp://") || url.contains("api.whatsapp.com") || url.contains("wa.me")) {
                    launchWhatsApp(url);
                    return true;
                } else if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                } else if (url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                    return true;
                }
            } catch (Exception e) {
                Log.e("URL_ERROR", "Error handling URL: " + url, e);
            }
            return false;
        }

    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}