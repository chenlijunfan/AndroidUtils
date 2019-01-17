package com.clji.androidutils.utils.statusbar

import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.IntRange
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.clji.androidutils.R
import android.widget.LinearLayout
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable


/**
 * 状态栏工具类
 *
 * @author         chenlijun
 * @time           2019/1/17
 */
class StatusBarUtils {

    /**
     * 设置状态栏颜色(纯色)
     */
    fun setColor(
        activity: AppCompatActivity, @ColorInt color: Int, @IntRange(
            from = 0,
            to = 255
        ) statusBarAlpha: Int
    ) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            //5.0以上版本

            //设置FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS属性才能调用setStatusBarColor方法来设置状态栏颜色
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            //设置FLAG_TRANSLUCENT_STATUS透明状态栏
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //根据输入的颜色和透明度显示
            activity.window.statusBarColor = calculateStatusColor(color, statusBarAlpha)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //低版本

            //添加透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //获取顶级视图
            var decorView: ViewGroup = activity.window.decorView as ViewGroup
            //获取顶部的StatusBarView,自定义StatusBarView的Id(在resources中创建Id)
            var fakeStatusBarView: View =
                decorView.findViewById(R.id.statusbarutil_fake_status_bar_view)
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView.visibility == View.GONE) {
                    fakeStatusBarView.visibility = View.VISIBLE
                }
                //设置顶层颜色
                fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha))
            } else {
                //上述不符合，则创建一个View添加到顶级视图中
                decorView.addView(createStatusBarView(activity, color, statusBarAlpha))
            }
            setRootView(activity)
        }

    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private fun calculateStatusColor(@ColorInt color: Int, alpha: Int): Int {
        if (alpha === 0) {
            return color
        }
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }

    private fun createStatusBarView(
        activity: AppCompatActivity, @ColorInt color: Int,
        alpha: Int
    ): View? {
        // 绘制一个和状态栏一样高的矩形
        val statusBarView = View(activity)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getStatusBarHeight(activity)
        )
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha))
        //自定义的StatusBarView的Id
        statusBarView.id = R.id.fake_status_bar_view_id
        return statusBarView
    }

    /**
     * 设置根布局参数
     */
    private fun setRootView(activity: AppCompatActivity) {
        //ViewGroup容器存放UI组件
        val parent = activity.findViewById(android.R.id.content) as ViewGroup
        var i = 0
        val count = parent.childCount
        while (i < count) {
            val childView = parent.getChildAt(i)
            if (childView is ViewGroup) {
                childView.setFitsSystemWindows(true)
                childView.clipToPadding = true
            }
            i++
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        // 获得状态栏高度
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 为界面设置自定义透明View
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     * @param needOffsetView 需要向下偏移的 View
     */
    fun setTranslucentForWindow(
        activity: AppCompatActivity, @IntRange(from = 0, to = 255) statusBarAlpha: Int,
        needOffsetView: View?, bg: Int
    ) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            //5.0以上版本
            setTransparentForWindow(activity)
            addTranslucentView(activity, statusBarAlpha)
            if (needOffsetView != null) {
                val haveSetOffset = needOffsetView.getTag(R.id.TAG_KEY_HAVE_SET_OFFSET)
                if (haveSetOffset != null && haveSetOffset as Boolean) {
                    return
                }
                val layoutParams = needOffsetView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin, layoutParams.topMargin + getStatusBarHeight(activity),
                    layoutParams.rightMargin, layoutParams.bottomMargin
                )
                needOffsetView.setTag(R.id.TAG_KEY_HAVE_SET_OFFSET, true)
            }
        } else {
            //低版本
            //添加透明状态栏
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //获取顶级视图
            val decorView = activity.window.decorView as ViewGroup
            //获取顶部的StatusBarView,自定义id
            val fakeStatusBarView: View = decorView.findViewById(R.id.fake_status_bar_view_id)
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView!!.visibility == View.GONE) {
                    fakeStatusBarView!!.visibility = View.VISIBLE
                }
                //设置顶层颜色
                fakeStatusBarView!!.setBackgroundResource(bg)
            } else {
                //上述不符合，则创建一个View添加到顶级视图中
                val statusBarView = View(activity)
                val params =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(activity)
                    )
                statusBarView.layoutParams = params
                fakeStatusBarView!!.setBackgroundResource(bg)
                statusBarView.id = R.id.fake_status_bar_view_id
                decorView.addView(statusBarView)
            }
            setRootView(activity)
        }

    }

    private fun setTransparentForWindow(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window
                .decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window
                .setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
        }
    }

    /**
     * 添加半透明矩形条
     *
     * @param activity       需要设置的 activity
     * @param statusBarAlpha 透明值
     */
    private fun addTranslucentView(
        activity: AppCompatActivity, @IntRange(
            from = 0,
            to = 255
        ) statusBarAlpha: Int
    ) {
        val contentView = activity.findViewById<View>(android.R.id.content) as ViewGroup//系统Id
        val fakeTranslucentView: View? = contentView.findViewById(R.id.fake_translucent_view_id)
        if (fakeTranslucentView != null) {
            if (fakeTranslucentView!!.visibility == View.GONE) {
                fakeTranslucentView!!.visibility = View.VISIBLE
            }
            fakeTranslucentView!!.setBackgroundColor(Color.argb(statusBarAlpha, 0, 0, 0))
        } else {
            contentView.addView(createTranslucentStatusBarView(activity, statusBarAlpha))
        }
    }

    private fun createTranslucentStatusBarView(activity: AppCompatActivity, alpha: Int): View? {
        // 绘制一个和状态栏一样高的矩形
        val statusBarView = View(activity)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getStatusBarHeight(activity)
        )
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(calculateStatusColor(Color.argb(alpha, 0, 0, 0), alpha))
        //自定义的StatusBarView的Id
        statusBarView.id = R.id.fake_status_bar_view_id
        return statusBarView
    }


}