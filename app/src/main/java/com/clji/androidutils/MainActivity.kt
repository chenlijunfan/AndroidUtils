package com.clji.androidutils

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.clji.androidutils.utils.statusbar.StatusBarUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //设置纯色title
        //StatusBarUtils().setColor(this, resources.getColor(R.color.colorPrimary), 0)
        StatusBarUtils().setTranslucentForWindow(this,0,toolbar,R.drawable.shape_gradient)
    }
}
