package com.umaplay.fluxxan;

import com.umaplay.fluxxan.impl.BaseActionCreator;
import com.umaplay.fluxxan.impl.BaseReducer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Roboeletric still not supports API 24 stuffs
@Config(sdk = 23, constants=BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class FluxxanTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private Fluxxan<Boolean> mFluxxan;

    class MyReducer extends BaseReducer<Boolean> {
        @Override
        public DispatchResult<Boolean> reduce(Boolean state, Action action) throws Exception {
            return new DispatchResult<Boolean>(!state, true);
        }
    }

    @Before
    public void setUp() {
        Boolean state = false;
        mFluxxan = new Fluxxan(state);
        mFluxxan.start();
    }

    @After
    public void cleanUp() {
        mFluxxan.stop();
    }

    @Test
    public void returnInitialState() throws Exception {
        assertFalse(mFluxxan.getState());
    }

    @Test
    public void dispatcherIsNonNull() throws Exception {
        assertNotNull(mFluxxan.getDispatcher());
    }

    @Test
    public void injectDispatcherWorks() throws Exception {
        BaseActionCreator ac = new BaseActionCreator();
        mFluxxan.inject(ac);
        ac.dispatch(new Action("EMPTY_ACTION"));
    }

    @Test
    public void notInjectDispatcherThrows() throws Exception {
        exception.expect(IllegalStateException.class);
        BaseActionCreator ac = new BaseActionCreator();
        ac.dispatch(new Action("EMPTY_ACTION"));
    }

    @Test
    public void registerReducerWorks() throws Exception {
        MyReducer reducer = new MyReducer();
        mFluxxan.registerReducer(reducer);

        assertEquals(mFluxxan.getReducer(MyReducer.class), reducer);

        mFluxxan.unregisterReducer(MyReducer.class);
    }

    @Test
    public void unregisterReducerWorks() throws Exception {
        MyReducer reducer = new MyReducer();
        mFluxxan.registerReducer(reducer);
        mFluxxan.unregisterReducer(MyReducer.class);
        assertNull(mFluxxan.getReducer(MyReducer.class));
    }

    @Test
    public void addListenerWorks() throws Exception {
        MyReducer reducer = new MyReducer();
        mFluxxan.registerReducer(reducer);

        StateListener listener = mock(StateListener.class);

        mFluxxan.addListener(listener);

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);

        mFluxxan.getDispatcher().dispatch(new Action("EMPTY_ACTION"));

        verify(listener).hasStateChanged(captor.capture(), anyBoolean());

        assertEquals(true, captor.getValue());

        mFluxxan.unregisterReducer(MyReducer.class);
    }
}