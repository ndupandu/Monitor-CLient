package com.pandu.monitoringclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/** Membungkus dashboard "Monitoring Client by Pandu" (alamat dari BuildConfig.APP_URL) dalam WebView. */
public class MainActivity extends Activity {
    private WebView web;
    private String host;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        host = Uri.parse(BuildConfig.APP_URL).getHost();
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setTextZoom(100);
        s.setUserAgentString(s.getUserAgentString() + " MonitoringClientApp/1.0");

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);                       // sesi login tersimpan antar pembukaan aplikasi
        cm.setAcceptThirdPartyCookies(web, false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String sch = u.getScheme();
                if ("about".equals(sch) || "data".equals(sch)) return false;
                if (("http".equals(sch) || "https".equals(sch)) && host != null && host.equalsIgnoreCase(u.getHost())) {
                    return false;                       // masih di dashboard sendiri
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));   // tautan luar dibuka di aplikasi lain
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (!r.isForMainFrame()) return;
                String url = TextUtils.htmlEncode(BuildConfig.APP_URL);
                String html = "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"
                        + "<style>body{font-family:sans-serif;margin:0;padding:32px 24px;background:#f3f5f9;color:#182033;text-align:center}"
                        + "h2{margin:40px 0 8px}p{color:#667085;line-height:1.5}"
                        + "a{display:inline-block;margin-top:20px;padding:14px 28px;border-radius:12px;background:#2f6fdb;color:#fff;text-decoration:none;font-weight:600}"
                        + "@media(prefers-color-scheme:dark){body{background:#0e1117;color:#e8ebf1}p{color:#97a1b3}}</style></head><body>"
                        + "<h2>Tidak dapat terhubung</h2><p>Server dashboard tidak menjawab.<br>Periksa koneksi internet / WiFi, lalu coba lagi.</p>"
                        + "<p style='font-size:12px'>" + url + "</p><a href='" + url + "'>Coba lagi</a></body></html>";
                v.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(msg)
                        .setPositiveButton("OK", (d, w) -> r.confirm())
                        .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(msg)
                        .setPositiveButton("OK", (d, w) -> r.confirm())
                        .setNegativeButton("Batal", (d, w) -> r.cancel())
                        .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }
        });

        web.setDownloadListener((url, ua, cd, mime, len) -> {
            if (Build.VERSION.SDK_INT <= 28
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Toast.makeText(this, "Izinkan penyimpanan, lalu ketuk unduh lagi.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                String name = URLUtil.guessFileName(url, cd, mime);
                DownloadManager.Request rq = new DownloadManager.Request(Uri.parse(url));
                String ck = CookieManager.getInstance().getCookie(url);   // berkas konsol butuh sesi login
                if (ck != null) rq.addRequestHeader("Cookie", ck);
                rq.addRequestHeader("User-Agent", ua);
                rq.setTitle(name);
                if (mime != null) rq.setMimeType(mime);
                rq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                rq.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(rq);
                Toast.makeText(this, "Mengunduh " + name, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Gagal mengunduh berkas.", Toast.LENGTH_LONG).show();
            }
        });

        if (saved == null || web.restoreState(saved) == null) {
            web.loadUrl(BuildConfig.APP_URL);
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        CookieManager.getInstance().flush();            // pastikan cookie login tersimpan ke disk
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }
}
