package me.mvp.frame.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import me.mvp.frame.R;
import me.mvp.frame.base.App;
import me.mvp.frame.di.component.AppComponent;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 使用 BaseSimpleDialogFragment 自定义自己需要的对话框
 *
 * @see <a href="https://github.com/RockyQu/MVPFrames/blob/rx/Common/src/main/java/com/tool/common/widget/dialog/BaseSimpleDialogFragment.java"></a>
 */
public abstract class BaseSimpleDialogFragment extends DialogFragment {

    // AppComponent
    protected AppComponent component;

    // 解除绑定
    private Unbinder unbinder = null;

    // 参数配置
    private Builder builder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        builder = builder();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(builder.canceledOnTouchOutside);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(builder.width, builder.height);
                window.setGravity(builder.gravity);
                window.setWindowAnimations(builder.animation);
                window.setAttributes(getLayoutParams(window));
            }
        }
    }

    private WindowManager.LayoutParams getLayoutParams(Window window) {
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.dimAmount = builder.dimAmount;// 背景明暗程度
        return layoutParams;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(builder.style, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);

        // 获取AppComponent
        component = ((App) getActivity().getApplication()).getAppComponent();

        // 绑定ButterKnife
        unbinder = ButterKnife.bind(this, view);

        // View的初始化可以放到这里执行
        this.create(savedInstanceState);

        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    /**
     * 拆分onCreateView，提供一个create方法，View的初始化可以放到这里执行，必须在子类中实现此方法
     */
    public abstract void create(Bundle savedInstanceState);

    /**
     * 获取布局文件，必须在子类中实现此方法
     */
    public abstract int getLayoutId();

    /**
     * 参数配置，可重写此方法添加参数配置
     */
    protected Builder builder() {
        return new Builder();
    }

    public static class Builder {

        /**
         * STYLE_NORMAL：会显示一个普通的dialog
         * STYLE_NO_TITLE：不带标题的dialog
         * STYLE_NO_FRAME：无框的dialog
         * STYLE_NO_INPUT：无法输入内容的dialog，即不接收输入的焦点，而且触摸无效。
         */
        private int style = DialogFragment.STYLE_NO_FRAME;

        private int width = LinearLayout.LayoutParams.MATCH_PARENT;
        private int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        /**
         * 窗体相对位置
         */
        private int gravity = Gravity.TOP;

        /**
         * 进入退出动画
         */
        private int animation = R.style.DialogEmptyAnimation;

        /**
         * 点击外部是否关闭对话框，默认关闭
         */
        private boolean canceledOnTouchOutside = false;

        /**
         * 背景明暗
         */
        private float dimAmount = 0.5f;

        public Builder() {
            ;
        }

        public Builder style(int style) {
            this.style = style;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder animation(int animation) {
            this.animation = animation;
            return this;
        }

        public Builder canceledOnTouchOutside(boolean canceledOnTouchOutside) {
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder dimAmount(float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }
    }
}