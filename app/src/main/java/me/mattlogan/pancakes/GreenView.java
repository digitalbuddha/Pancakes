package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewStack;
import me.mattlogan.library.ViewStackActivity;

public class GreenView extends RelativeLayout {


    public GreenView(final Context context) {
        super(context);
        final ViewStack viewStack = ((ViewStackActivity) context).viewStack();
        LayoutInflater.from(context).inflate(R.layout.view_green, this, true);

        setBackgroundColor(Color.GREEN);

        findViewById(R.id.green_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.pop();
            }
        });

        findViewById(R.id.green_button_go_to_blue).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.push(new BlueView(context, viewStack));
            }
        });
    }
}
