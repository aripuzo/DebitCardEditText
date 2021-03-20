package ng.com.buttontech.library

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText


/**
 * Created by ari on 12/03/2021.
 */
class DebitCardEditText : AppCompatEditText {
    //
    companion object {
        private val DEFAULT_CARD_NUMBER_HINT = "0000000000000000"
        private val DEFAULT_STROKE = 2f
    }


    private var defaultLineTextColor = getThemeColor(R.attr.colorControlNormal)
    private var defaultFocusedLineTextColor = getThemeColor(R.attr.colorControlActivated)
    private var defaultFailedLineTextColor = getThemeColor(R.attr.colorControlActivated)
    private var defaultSuccessLineTextColor = getThemeColor(R.attr.colorControlActivated)


    var lineColor = defaultLineTextColor
    var failedLineColor = defaultFailedLineTextColor
    var focusedLineColor = defaultFocusedLineTextColor
    var successLineColor = defaultSuccessLineTextColor

    var cardNumberHint = DEFAULT_CARD_NUMBER_HINT

    private var mSpace = 16f

    private var mCharSize = 0f
    private var mNumSections = 4f
    private var mLineSpacing = 11f

    private var multi: Float = 0f

    private var mClickListener: OnClickListener? = null

    var lineStroke = DEFAULT_STROKE
    var lineStrokeSelected = DEFAULT_STROKE

    private var isFailed = false
    private var isSuccess = false

    private var mLinesPaint: Paint? = null

    var mColors = IntArray(3)

    constructor(context: Context?) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    constructor(context: Context, attrs: AttributeSet,
//                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
//        init(context, attrs)
//    }

    private fun init(context: Context, attrs: AttributeSet, defStyleRes: Int) {
        initializeAttributes(context, attrs, defStyleRes)
        multi = context.resources.displayMetrics.density
        lineStroke *= multi
        lineStrokeSelected *= multi
        mLinesPaint = Paint(paint)
        mLinesPaint?.strokeWidth = lineStroke
        if (!isInEditMode) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.colorControlActivated,
                outValue, true)
            val colorActivated = outValue.data
            mColors[0] = colorActivated
            context.theme.resolveAttribute(android.R.attr.colorPrimaryDark,
                outValue, true)
            val colorDark = outValue.data
            mColors[1] = colorDark
            context.theme.resolveAttribute(android.R.attr.colorControlHighlight,
                outValue, true)
            val colorHighlight = outValue.data
            mColors[2] = colorHighlight
        }
        setBackgroundResource(0)
        mSpace *= multi
        mLineSpacing *= multi

        val scaledSizeInPixels = textSize
        paint.textSize = scaledSizeInPixels

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }
        })
        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            setSelection(text!!.length)
            mClickListener?.onClick(v)
        }

    }


    private fun initializeAttributes(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.DebitCardEditText, defStyleAttr, 0)
            .let {
                failedLineColor = it.getColor(
                        R.styleable.DebitCardEditText_failed_line_color,
                        defaultFailedLineTextColor
                )

                lineColor = it.getColor(
                        R.styleable.DebitCardEditText_line_color,
                        defaultLineTextColor
                )

                successLineColor = it.getColor(
                        R.styleable.DebitCardEditText_success_line_color,
                        defaultSuccessLineTextColor
                )

                focusedLineColor = it.getColor(
                        R.styleable.DebitCardEditText_focused_line_color,
                        focusedLineColor
                )

//                it.getString(R.styleable.CompactCreditCardInput_label_text_font)?.let { font ->
//                    labelTextFont = font
//                }
//
//                textColor =
//                    it.getColor(R.styleable.CompactCreditCardInput_text_color, defaultTextColor)
//
//                hintColor = it.getColor(R.styleable.CompactCreditCardInput_hint_color, -1)

                lineStroke = it.getDimension(R.styleable.DebitCardEditText_line_stroke, DEFAULT_STROKE)

                lineStrokeSelected = it.getDimension(R.styleable.DebitCardEditText_line_stroke_selected, DEFAULT_STROKE)

                it.getString(R.styleable.DebitCardEditText_card_number_hint)?.let { hint ->
                    cardNumberHint = hint
                }

                it.recycle()
            }
        invalidate()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas?) {
        //super.onDraw(canvas);
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumSections * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumSections - 1)) / mNumSections
        }

        var startX = paddingLeft
        val bottom = height - paddingBottom
        var currentTextPos = 0

        var textDraw = text.toString()
        if(text?.isEmpty() == true) {
            textDraw = cardNumberHint
            paint.color = currentHintTextColor
        }
        else{
            paint.color = currentTextColor
        }

        val textLength = textDraw.length
        val textWidths = FloatArray(textLength)

        paint.getTextWidths(textDraw, 0, textLength, textWidths)
        paint.textSize

        for (i in 0 until mNumSections.toInt()) {
            updateColorForLines()
            canvas!!.drawLine(
                startX.toFloat(), bottom.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint!!)
            if (textDraw.length > currentTextPos) {
                val l = currentTextPos
                val k = currentTextPos + 4
                var start = startX + (16 * multi)
                for (j in l until k) {
                    if (textDraw.length > j) {
                        val middle = start
                        canvas.drawText(textDraw,
                            j,
                            j + 1,
                            middle - textWidths[j] / 2,
                            bottom - mLineSpacing,
                            paint)
                    }
                    currentTextPos++
                    start += (mCharSize.toInt()/8)
                }
            }
            startX += if (mSpace < 0) {
                (mCharSize * 2).toInt()
            } else {
                (mCharSize + mSpace).toInt()
            }
        }
    }

    fun setFailedState(isFailed: Boolean){
        this.isFailed = isFailed
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines() {
        mLinesPaint?.strokeWidth = lineStrokeSelected
        when {
            isFailed -> {
                mLinesPaint?.color = failedLineColor
            }
            isFocused -> {
                mLinesPaint?.color = focusedLineColor
            }
            isSuccess -> {
                mLinesPaint?.color = successLineColor
            }
            else -> {
                mLinesPaint!!.strokeWidth = lineStroke
                mLinesPaint?.color = lineColor
            }
        }
    }


    @ColorInt
    fun getThemeColor(@AttrRes attributeColor: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attributeColor, value, true)
        return value.data
    }
}