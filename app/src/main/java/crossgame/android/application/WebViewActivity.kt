package crossgame.android.application

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webView)

        // Configurar as configurações do WebView, incluindo a ativação do JavaScript
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true

        // Definir um WebViewClient para interceptar as mudanças de URL
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // Aqui você pode interceptar e controlar a solicitação de carregamento de URL
                val url = request.url.toString()

                if (url.startsWith("https://discord.com/") && url.contains("code=")) {
                    // O URL contém o código de autorização do Discord
                    val code = extractAuthorizationCode(url)

                    // Faça algo com o código, como trocá-lo por um token de acesso
                    // Você pode implementar a troca do código por um token aqui

                    // Redirecione para a sua atividade desejada
                    val intent = Intent(this@WebViewActivity, SingupActivity::class.java)
                    startActivity(intent)
                    finish()

                    // Retorne 'true' para indicar que você interceptou a ação
                    return true
                }

                // Retorne 'false' para permitir o carregamento normal
                return false
            }
        }

        // Carregar a URL inicial passada através de um Intent extra
        val initialUrl = intent.getStringExtra("url")
        if (initialUrl != null) {
            webView.loadUrl(initialUrl)
        }
    }

    private fun extractAuthorizationCode(url: String): String {
        val codeQueryParam = "code="
        val codeStartIndex = url.indexOf(codeQueryParam)
        if (codeStartIndex >= 0) {
            val codeEndIndex = url.indexOf("&", codeStartIndex)
            if (codeEndIndex >= 0) {
                return url.substring(codeStartIndex + codeQueryParam.length, codeEndIndex)
            }
        }
        return ""
    }
}
