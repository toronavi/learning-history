import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class VerticalTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 1. 幅と高さの計測指定(MeasureSpec)を入れ替えて親クラスに計算させる
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        
        // 2. 計算された寸法も「幅」と「高さ」を入れ替えて確定させる
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState

        canvas.save()

        // 3. Canvasの描画起点を左下に移動し、-90度回転させる
        canvas.translate(0f, height.toFloat())
        canvas.rotate(-90f)

        // 4. パディングを考慮して描画位置を調整
        canvas.translate(compoundPaddingLeft.toFloat(), compoundPaddingTop.toFloat())

        // 5. テキストのレイアウト（配置）を描画
        layout?.draw(canvas)

        canvas.restore()
    }
}