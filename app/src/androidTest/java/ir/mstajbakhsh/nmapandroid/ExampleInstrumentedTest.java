package ir.mstajbakhsh.nmapandroid;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("ir.mstajbakhsh.nmapandroid", appContext.getPackageName());

        NMAPUtilities n = new NMAPUtilities(appContext);
        n.startInstallation();

        final StringBuilder line_buffer = new StringBuilder();
        OutputStream ops = new OutputStream() {
            @Override
            public void write(int b) {
                if (b == '\n') {
                    System.out.println(line_buffer.toString());
                    line_buffer.setLength(0);
                } else {
                    line_buffer.append(Character.toChars(b));
                }
            }
        };

        try {
            n.execCommand(ops, "-A", "192.168.1.0/24", "--system-dns");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}