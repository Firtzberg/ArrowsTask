package com.hrca.arrowstask;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Random;

/**
 * Shows a square grid of arrows.
 */
public class ArrowsView extends GridView {
    /**
     * Number of rows and column.
     */
    public static final int GRID_SIZE = 4;
    /**
     * Listener of ArrowsView events.
     */
    protected ArrowsViewListener listener;
    /**
     * Drawable for up arrow.
     */
    private Drawable upArrow;
    /**
     * Drawable for down arrow.
     */
    private Drawable downArrow;
    /**
     * Drawable for left arrow.
     */
    private Drawable leftArrow;
    /**
     * Drawable for right arrow.
     */
    private Drawable rightArrow;

    public ArrowsView(Context context) {
        super(context);
        init(null, 0);
    }

    public ArrowsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ArrowsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Sets listener for ArrowsView events.
     *
     * @param listener The listener to be set.
     */
    public void setListener(ArrowsViewListener listener) {
        this.listener = listener;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ArrowsView, defStyle, 0);

        upArrow = resolveDrawable(a, R.styleable.ArrowsView_upArrowDrawable, R.drawable.u);
        downArrow = resolveDrawable(a, R.styleable.ArrowsView_downArrowDrawable, R.drawable.d);
        leftArrow = resolveDrawable(a, R.styleable.ArrowsView_leftArrowDrawable, R.drawable.l);
        rightArrow = resolveDrawable(a, R.styleable.ArrowsView_rightArrowDrawable, R.drawable.r);

        a.recycle();

        // set number of columns to unchangeable value.
        super.setNumColumns(GRID_SIZE);

        // populate grid
        ArrowAdapter arrowAdapter = new ArrowAdapter();
        setAdapter(arrowAdapter);
        setOnItemClickListener(arrowAdapter);
    }

    /**
     * Gets set drawable attribute value from type array or default value.
     *
     * @param a                 Typed array in which the drawable might be set.
     * @param attribute         Value of attribute in typed array.
     * @param defaultResourceId Resource id to be used when attribute in typed array is not set.
     * @return Drawable to be used.
     */
    private Drawable resolveDrawable(final TypedArray a, final int attribute, final int defaultResourceId) {
        Drawable result = null;
        if (a.hasValue(attribute)) {
            result = a.getDrawable(attribute);
        }
        if (result == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                result = getResources().getDrawable(defaultResourceId, getContext().getTheme());
            } else {
                result = getResources().getDrawable(defaultResourceId);
            }
        }
        if (result != null)
            result.setCallback(this);

        return result;
    }

    @Override
    public void setNumColumns(int numColumns) {
        // prevent number of columns to be changed.
    }

    /**
     * Gets the arrow drawable value.
     *
     * @param arrow Specifies the arrow for which the drawable is got.
     * @return The arrow drawable value.
     */
    public Drawable getArrowDrawable(Arrow arrow) {
        switch (arrow) {
            case Up:
                return upArrow;
            case Down:
                return downArrow;
            case Left:
                return leftArrow;
            case Right:
                return rightArrow;
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Force square layout
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size = widthSize < heightSize ? widthSize : heightSize;

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }

    /**
     * Sets the view's arrow drawable value.
     *
     * @param arrow         Specifies the arrow for which the drawable is set.
     * @param arrowDrawable The arrow drawable value to use.
     */
    public void setArrowDrawable(Arrow arrow, Drawable arrowDrawable) {
        switch (arrow) {
            case Up:
                upArrow = arrowDrawable;
                break;
            case Down:
                downArrow = arrowDrawable;
                break;
            case Left:
                leftArrow = arrowDrawable;
                break;
            case Right:
                rightArrow = arrowDrawable;
                break;
            default:
                return;
        }
        invalidate();
    }

    /**
     * Arrow enumeration.
     */
    public enum Arrow {
        Up, Down, Left, Right
    }

    /**
     * Listens to ArrowsView events.
     */
    public interface ArrowsViewListener {
        /**
         * Invoked when arrow is clicked.
         *
         * @param hit True when desired arrow was clicked.
         */
        void onArrowClicked(boolean hit);
    }

    /**
     * Adapter for arrow drawables.
     * Generates arrow grid and listens to item clicks.
     */
    private class ArrowAdapter extends BaseAdapter implements OnItemClickListener {
        /**
         * Generated arrows.
         */
        protected final Arrow[] arrows;
        /**
         * Random number generator to generate random arrow array.
         */
        protected final Random random = new Random();

        ArrowAdapter() {
            arrows = new Arrow[GRID_SIZE * GRID_SIZE];
            generate();
        }

        /**
         * Generates new arrows map.
         */
        public void generate() {
            Arrow[] availableArrows = Arrow.values();
            for (int i = 0; i < arrows.length; i++) {
                arrows[i] = availableArrows[random.nextInt(availableArrows.length - 1) + 1];
            }
            arrows[random.nextInt(arrows.length)] = availableArrows[0];
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return arrows.length;
        }

        @Override
        public Object getItem(int i) {
            return arrows[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageDrawable(getArrowDrawable(arrows[position]));
            return imageView;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            boolean hit = arrows[i] == Arrow.values()[0];
            if (listener != null)
                listener.onArrowClicked(hit);
            if (hit) {
                generate();
            }
        }
    }
}
