package me.mattlogan.library;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class ViewStackTest {

    @Mock ViewStackDelegate delegate;
    @Mock ViewGroup container;

    ViewStack viewStack;

    @Before
    public void setup() {
        initMocks(this);
        viewStack = ViewStack.create(container, delegate);
    }

    @Test
    public void createWithNullContainer() {
        try {
            ViewStack.create(null, delegate);
            fail();
        } catch (NullPointerException e) {
            assertEquals("container == null", e.getMessage());
        }
    }

    @Test
    public void createWithNullDelegate() {
        try {
            ViewStack.create(container, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("delegate == null", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithNullBundle() {
        try {
            viewStack.saveToBundle(null, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("bundle == null", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithNullTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.saveToBundle(bundle, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithEmptyTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.saveToBundle(bundle, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void saveToBundle() {
        Bundle bundle = mock(Bundle.class);
        viewStack.saveToBundle(bundle, "tag");
        verify(bundle).putSerializable(eq("tag"), isA(Stack.class));
    }

    @Test
    public void rebuildFromBundleWithNullBundle() {
        try {
            viewStack.rebuildFromBundle(null, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("bundle == null", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithNullTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.rebuildFromBundle(bundle, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithEmptyTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.rebuildFromBundle(bundle, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithNullStack() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.rebuildFromBundle(bundle, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("Bundle doesn't contain any ViewStack state.", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundle() {
        Stack<ViewFactory> stack = new Stack<>();

        ViewFactory bottom = mock(ViewFactory.class);
        stack.push(bottom);

        ViewFactory top = mock(ViewFactory.class);
        Context context = mock(Context.class);
        View view = mock(View.class);
        when(container.getContext()).thenReturn(context);
        when(top.createView(context)).thenReturn(view);
        stack.push(top);

        Bundle bundle = mock(Bundle.class);
        when(bundle.getSerializable("tag")).thenReturn(stack);

        viewStack.rebuildFromBundle(bundle, "tag");

        assertEquals(2, viewStack.size());
        verify(container).removeAllViews();
        verify(container).addView(view);
    }

    @Test
    public void pushWithNullViewFactory() {
        try {
            viewStack.push(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("viewFactory == null", e.getMessage());
        }
    }

    @Test
    public void push() {
        ViewFactory viewFactory = mock(ViewFactory.class);
        Context context = mock(Context.class);
        View view = mock(View.class);
        when(container.getContext()).thenReturn(context);
        when(viewFactory.createView(context)).thenReturn(view);

        viewStack.push(viewFactory);

        assertEquals(1, viewStack.size());
        verify(container).removeAllViews();
        verify(container).addView(view);
    }

    @Test
    public void popWithSizeOne() {
        viewStack.push(mock(ViewFactory.class));

        ViewFactory result = viewStack.pop();

        assertNull(result);
        verify(delegate).finishStack();
    }

    @Test
    public void popWithSizeGreaterThanOne() {
        ViewFactory bottom = mock(ViewFactory.class);
        Context context = mock(Context.class);
        View view = mock(View.class);
        viewStack.push(bottom);

        ViewFactory top = mock(ViewFactory.class);
        viewStack.push(top);

        reset(container);

        when(container.getContext()).thenReturn(context);
        when(bottom.createView(context)).thenReturn(view);

        ViewFactory result = viewStack.pop();

        assertSame(top, result);

        verify(container).removeAllViews();
        verify(container).addView(view);
    }

    @Test
    public void peek() {
        ViewFactory bottom = mock(ViewFactory.class);
        viewStack.push(bottom);

        ViewFactory top = mock(ViewFactory.class);
        viewStack.push(top);

        ViewFactory result = viewStack.peek();

        assertSame(top, result);
    }

    @Test
    public void size() {
        ViewFactory bottom = mock(ViewFactory.class);
        viewStack.push(bottom);

        ViewFactory top = mock(ViewFactory.class);
        viewStack.push(top);

        assertEquals(2, viewStack.size());
    }

    @Test
    public void clear() {
        ViewFactory bottom = mock(ViewFactory.class);
        viewStack.push(bottom);

        ViewFactory top = mock(ViewFactory.class);
        viewStack.push(top);

        reset(container);

        viewStack.clear();

        verify(container).removeAllViews();
        verifyNoMoreInteractions(container);
    }
}
