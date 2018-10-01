package jp.co.nishitaku.dentaku;

import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView textViewCalc = null;
    private TextView textViewResult = null;

    private Button btn5 = null;
    private Button btnKakeru = null;
    private Button btnTasu = null;
    private Button btnHiku = null;
    private Button btnWaru = null;
    private Button[] operatorBtnList = null;

    BigDecimal result = BigDecimal.ZERO;
    boolean equalFlag = false;
    boolean dotFlag = false;        // '.'ボタンが入力された状態
    boolean opeFlag = true;         // 演算子が入力された状態
    boolean decimalFlag = false;    // 小数が入力された状態

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;

    /**
     * 数値ボタンのクリックリスナー
     */
    OnClickListener numberKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // 計算エラーが発生している場合は無視する
            if (null == result) {
                return;
            }
            // ＝フラグがたっている場合は、表示をクリア
            if (equalFlag) {
                textViewCalc.setText("");
                equalFlag = false;
            }

            Button button = (Button) view;
            Log.d(TAG, "Left:" + button.getLeft() + ",Top:" + button.getTop());
            String numStr = (String)button.getText();
            if (dotFlag) {
                // 小数点フラグがたっている場合は、'.'+数値を式に追加
                numStr = '.' + numStr;
                dotFlag = false;
                decimalFlag = true;
                if (opeFlag) {
                    // 演算子フラグがたっている場合は、'0.+数値を式に追加
                    numStr = '0' + numStr;
                }
            }

            textViewCalc.append(numStr);
            opeFlag = false;
        }
    };

    /**
     * "."ボタンのクリックリスナー
     */
    OnClickListener dotKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // 小数が入力されていない場合はフラグをたてる
            if (!decimalFlag) {
                dotFlag = true;
            }
        }
    };

    /**
     * "5"ボタンのクリックリスナー
     */
    OnTouchListener fiveKeyTouchListner = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 押したときの動作
                    Log.d(TAG, "onTouch: ACTION_DOWN");

                    // 演算子ボタンを表示
                    setOperatorBtnVisibility(View.VISIBLE);
                    break;

                case MotionEvent.ACTION_MOVE:
                    // フリック中の動作
                    if (inViewBounds(btnKakeru, x, y)) {
                        // 「×」ボタンの色を変更
                        setOperatorBtnBackground(0);
                    } else if (inViewBounds(btnTasu, x, y)) {
                        // 「＋」ボタンの色を変更
                        setOperatorBtnBackground(1);
                    } else if (inViewBounds(btnHiku, x, y)) {
                        // 「−」ボタンの色を変更
                        setOperatorBtnBackground(2);
                    } else if (inViewBounds(btnWaru, x, y)) {
                        // 「÷」ボタンの色を変更
                        setOperatorBtnBackground(3);
                    } else {
                        // それ以外
                        setOperatorBtnBackground(4);
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    // 離したときの動作
                    Log.d(TAG, "onTouch: ACTION_UP");
                    // 演算子ボタンを非表示
                    setOperatorBtnVisibility(View.INVISIBLE);

                    if (inViewBounds(btnKakeru, x, y)) {
                        // 「×」ボタンの処理
                        operatorKeyAction(btnKakeru);
                    } else if (inViewBounds(btnTasu, x, y)) {
                        // 「＋」ボタンの処理
                        operatorKeyAction(btnTasu);
                    } else if (inViewBounds(btnHiku, x, y)) {
                        // 「−」ボタンの処理
                        operatorKeyAction(btnHiku);
                    } else if (inViewBounds(btnWaru, x, y)) {
                        // 「÷」ボタンの処理
                        operatorKeyAction(btnWaru);
                    }

                    setOperatorBtnBackground(4);
                    break;
            }
            return false;
        }
    };

    /**
     * 演算子ボタンの処理
     * @param opeBtn
     */
    private void operatorKeyAction(Button opeBtn) {
        Log.d(TAG, opeBtn.getText() + " pushed.");
        Log.d(TAG, "Left:" + opeBtn.getLeft() + ",Top:" + opeBtn.getTop());
        // 計算エラーが発生している場合は無視する
        if (null == result) {
            return;
        }

        String calcStr = textViewCalc.getText().toString();

        if (calcStr.length() == 0) {
            // 空だった場合は無視する
            return;
        }

        if (Calc.isOperator(calcStr.charAt(calcStr.length() - 1))) {
            // 式の最後の文字が演算子だった場合は、最後の文字を上書きする
            textViewCalc.setText(calcStr.substring(0, calcStr.length() - 1) + opeBtn.getText());
        } else {
            // そうでない場合は式に追加する
            textViewCalc.append(opeBtn.getText());
        }

        equalFlag = false;
        dotFlag = false;
        opeFlag = true;
        decimalFlag = false;
    }

    /**
     * "="ボタンのクリックリスナー
     * 計算結果を表示する
     */
    OnClickListener equalKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: equalKeyClickListener");
            Button button = (Button) view;
            Log.d(TAG, "Calc execute : " + textViewCalc.getText().toString());
            // 計算
            result = Calc.parseExpression(textViewCalc.getText().toString());

            if (null == result) {
                // 計算エラーの場合
                Log.d(TAG, "equalKeyClickListener: calc error.");
                textViewResult.setText("E");
            } else if (result.equals(new BigDecimal("0E-12"))) {
                // 0を除算した場合
                textViewResult.setText("0");
            } else {
                textViewResult.setText(result.toPlainString());
            }

            equalFlag = true;
            opeFlag = true;
            decimalFlag = false;
        }
    };

    /**
     * "CLEAR"ボタンのクリックリスナー
     */
    OnClickListener clearKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: clearKeyClickListener");
            result = BigDecimal.ZERO;
            textViewCalc.setText("");
            textViewResult.setText("");
            equalFlag = false;
            dotFlag = false;
            opeFlag = true;
            decimalFlag = false;
        }
    };

    /**
     * "DEL"ボタンのクリックリスナー
     */
    OnClickListener deleteKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: deleteKeyClickListener");
            CharSequence seq = textViewCalc.getText();
            if(seq.length() > 0) {
                textViewCalc.setText(seq.subSequence(0, seq.length() - 1));
                // 削除した結果、最後の文字が'.'だった場合は、これも削除する
                Log.d(TAG, "Last charactar is " + textViewCalc.getText());
                if (textViewCalc.getText().toString().endsWith(".")) {
                    Log.d(TAG, "end with .");
                    seq = textViewCalc.getText();
                    textViewCalc.setText(seq.subSequence(0, seq.length() - 1));
                    dotFlag = false;
                    decimalFlag = false;
                }
            }
            equalFlag = false;
            opeFlag = false;
        }
    };

    /**
     * "+/-"ボタンのクリックリスナー
     */
    OnClickListener plusminusKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: plusminusKeyClickListener");
            CharSequence calc = textViewCalc.getText();
            if (calc.toString().startsWith("-")) {
                textViewCalc.setText(calc.subSequence(1, calc.length()));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("-");
                sb.append(calc);
                textViewCalc.setText(sb);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCalc = findViewById(R.id.text_calc);
        textViewResult = findViewById(R.id.text_result);
        btn5 =  findViewById(R.id.btn_5);
    }

    /**
     * 演算子ボタンを動的に生成する
     * onCreateの時点ではベースとなるキー5の情報が取得できないため、本メソッドにておこなう
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        btnKakeru = createOpeBtn(R.string.kakeru);
        btnTasu = createOpeBtn(R.string.tasu);
        btnHiku = createOpeBtn(R.string.hiku);
        btnWaru = createOpeBtn(R.string.waru);

        FrameLayout.LayoutParams btnKakeruParams = createOpeBtnParams(R.string.kakeru);
        FrameLayout.LayoutParams btnTasuParams = createOpeBtnParams(R.string.tasu);
        FrameLayout.LayoutParams btnHikuParams = createOpeBtnParams(R.string.hiku);
        FrameLayout.LayoutParams btnWaruParams = createOpeBtnParams(R.string.waru);

        FrameLayout layout = findViewById(R.id.layout_tenkey);
        layout.addView(btnKakeru, btnKakeruParams);
        layout.addView(btnTasu, btnTasuParams);
        layout.addView(btnHiku, btnHikuParams);
        layout.addView(btnWaru, btnWaruParams);

        setOperatorBtnVisibility(View.INVISIBLE);

        operatorBtnList = new Button[]{btnKakeru, btnTasu, btnHiku, btnWaru};
        setListener();
    }

    /**
     * 演算子ボタンを作成する
     * @param type 演算子ボタンの種類
     * @return
     */
    private Button createOpeBtn(int type) {
        Button btn = new Button(this);
        btn.setText(getString(type));
        btn.setTextSize(30);
        btn.setTypeface(Typeface.SANS_SERIF);

        return btn;
    }

    /**
     * 演算子ボタンのパラメータを作成する
     * @param type 演算子ボタンの種類
     * @return
     */
    private FrameLayout.LayoutParams createOpeBtnParams(int type) {
        // 幅と高さ
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                btn5.getWidth(),
                btn5.getHeight()
        );
        // 位置
        int dx = 10;
        int dy = 10;
        switch (type) {
            case R.string.kakeru:
                params.leftMargin = btn5.getRight() + dx;
                params.topMargin = btn5.getTop();
                break;

            case R.string.tasu:
                params.rightMargin = btn5.getLeft() - dx;
                params.topMargin = btn5.getTop();
                break;

            case R.string.hiku:
                params.leftMargin = btn5.getLeft();
                params.topMargin = btn5.getBottom() + dy;
                break;

            case R.string.waru:
                params.leftMargin = btn5.getLeft();
                params.topMargin = btn5.getTop() - dy - btn5.getHeight();
                break;

            default:
                break;
        }

        return params;
    }

    private void setOperatorBtnVisibility(int visibility) {
        btnKakeru.setVisibility(visibility);
        btnTasu.setVisibility(visibility);
        btnHiku.setVisibility(visibility);
        btnWaru.setVisibility(visibility);
    }

    private void setOperatorBtnBackground(int index) {
        for (int i = 0; i < 4; i++) {
            GradientDrawable drawable = new GradientDrawable();
            // 角を丸くする
            drawable.setCornerRadius(20);
            drawable.mutate();
            if (index == i) {
                drawable.setColor(ContextCompat.getColor(this, R.color.flickkeyPressed));
            } else {
                drawable.setColor(ContextCompat.getColor(this, R.color.flickkeyNormal));
            }
            operatorBtnList[i].setBackground(drawable);
        }
    }

    private void setListener() {
        findViewById(R.id.btn_0).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_1).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_2).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_3).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_4).setOnClickListener(numberKeyClickListener);
        btn5.setOnClickListener(numberKeyClickListener);
        btn5.setOnTouchListener(fiveKeyTouchListner);
        findViewById(R.id.btn_6).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_7).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_8).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_9).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_dot).setOnClickListener(dotKeyClickListener);
        findViewById(R.id.btn_equal).setOnClickListener(equalKeyClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(clearKeyClickListener);
        findViewById(R.id.btn_delete).setOnClickListener(deleteKeyClickListener);
        findViewById(R.id.btn_plusminus).setOnClickListener(plusminusKeyClickListener);
    }


    /**
     * 入力x, yがビュー領域内かどうか判定する
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean inViewBounds(final View view, int x, int y) {
        Rect outRect = new Rect();
        view.getDrawingRect(outRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }
}
