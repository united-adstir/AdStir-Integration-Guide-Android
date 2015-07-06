package com.ad_stir.sample.WebViewIntegration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class WebViewIntegrationActivity extends ActionBarActivity {
    private Handler mHandler = new Handler();
    private WebViewClient mWebViewClient = new WebViewClient(){
        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri u = Uri.parse(url);
            String h = u.getHost();
            // 自サイトのドメイン以外の場合は標準設定のブラウザに飛ばすため、trueを返す
            // Open default browser if unknown host name.
            if (!h.startsWith("ja.ad-stir.com")) {
                Intent i = new Intent(Intent.ACTION_VIEW, u);
                startActivity(i);
                return true;
            }
            return false;
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout l = (RelativeLayout)findViewById(R.id.main_layout);
        WebView w = (WebView)findViewById(R.id.webView);
        w.setWebViewClient(mWebViewClient);

        // 別ウィンドウを開く際のハンドリング
        // Handle opening new window event
        w.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                try {
                    if (resultMsg.obj != null && resultMsg.obj instanceof WebView.WebViewTransport) {
                        final WebView iframeView = new WebView(MainActivity.this);
                        iframeView.setWebViewClient(mWebViewClient);
                        iframeView.setLayoutParams(new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                        l.addView(iframeView);
                        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                        transport.setWebView(iframeView);
                        resultMsg.sendToTarget();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    l.removeView(iframeView);
                                    iframeView.stopLoading();
                                    iframeView.setWebChromeClient(null);
                                    iframeView.setWebViewClient(null);
                                    iframeView.destroy();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        return true;
                    }
                    return false;
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        WebSettings s = w.getSettings();
        // JavaScriptの実行許可
        // Allow JavaScript execution
        s.setJavaScriptEnabled(true);
        // 別ウィンドウを開けるように
        // Allow open new window
        s.setSupportMultipleWindows(true);
        w.loadUrl("https://ja.ad-stir.com");
    }
}