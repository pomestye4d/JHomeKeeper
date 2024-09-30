/*
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.vga.hk.core.storage.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class TestStorage {
    private final File testFile = new File("test.rrd");

    @Before
    public void setUp() throws IOException {
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    public void test() throws IOException {
        RrdDef rrdDef = new RrdDef(testFile.getCanonicalPath(), 0, 60);
        rrdDef.setVersion(2);
        rrdDef.addDatasource("ds", DsType.GAUGE, 60, -5, 300);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 20, 999);
        try (RrdDb rrdDb = new RrdDb(rrdDef)) {
            Calendar testTime = Calendar.getInstance();
            testTime.set(Calendar.MINUTE, 0);
            testTime.set(Calendar.SECOND, 0);
            testTime.set(Calendar.MILLISECOND, 0);
            testTime.set(Calendar.HOUR, testTime.get(Calendar.HOUR) -2);
            long start = Util.getTimestamp(testTime);
            long timeStamp = start;
            for (int i = 0; i < 180; i++) {
                long sampleTime = timeStamp;
                rrdDb.createSample(sampleTime).setValue("ds", 30).update();
                timeStamp += 60;
            }
            long end = timeStamp;
            var archive = rrdDb.getArchive(0);
            FetchData f = rrdDb.createFetchRequest(ConsolFun.AVERAGE, start, end,120).fetchData();
            double[] values = f.getValues("ds");
            Assert.assertEquals("Data before first entry", Double.NaN, values[0], 0.0);
            Assert.assertEquals("Bad average in point 1", 30, values[1], 1e-3);
            Assert.assertEquals("Bad average in point 2", 30, values[2], 1e-3);
        }
    }
}
