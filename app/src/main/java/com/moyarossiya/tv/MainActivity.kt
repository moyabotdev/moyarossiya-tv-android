package com.moyarossiya.tv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.moyarossiya.tv.ui.MoyaTvApp
import com.moyarossiya.tv.ui.theme.MoyaTvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            MoyaTvTheme {
                MoyaTvApp()
            }
        }
    }
}
