/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.slaves;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.FilePath;
import hudson.Proc;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Node.Mode;
import hudson.model.Slave;
import hudson.remoting.Callable;
import hudson.remoting.Which;
import hudson.util.ArgumentListBuilder;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Kohsuke Kawaguchi
 */
public class JNLPLauncherTest extends HudsonTestCase {
    /**
     * Starts a JNLP slave agent and makes sure it successfully connects to Hudson. 
     */
    public void testLaunch() throws Exception {
        if(Boolean.getBoolean("java.awt.headless")) {
            System.err.println("Skipping JNLPLauncherTest.testLaunch because we are running headless");
            return;
        }
        System.err.println("Not in headless mode, continuing with JNLPLauncherTest.testLaunch...");
        Computer c = addTestSlave();
        launchJnlpAndVerify(c, buildJnlpArgs(c));
    }

    /**
     * Tests the '-headless' option.
     * (Although this test doesn't really assert that the agent really is running in a headless mdoe.)
     */
    public void testHeadlessLaunch() throws Exception {
        Computer c = addTestSlave();
        launchJnlpAndVerify(c, buildJnlpArgs(c).add("-arg","-headless"));
    }

    private ArgumentListBuilder buildJnlpArgs(Computer c) throws Exception {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(new File(new File(System.getProperty("java.home")),"bin/java").getPath(),"-jar");
        args.add(Which.jarFile(netx.jnlp.runtime.JNLPRuntime.class).getAbsolutePath());
        args.add("-headless","-basedir");
        args.add(createTmpDir());
        args.add("-nosecurity","-jnlp", getJnlpLink(c));
        return args;
    }

    /**
     * Launches the JNLP slave agent and asserts its basic operations.
     */
    private void launchJnlpAndVerify(Computer c, ArgumentListBuilder args) throws Exception {
        Proc proc = createLocalLauncher().launch(args.toCommandArray(),
                new String[0], System.out, new FilePath(new File(".")));

        try {
            // verify that the connection is established, up to 10 secs
            for( int i=0; i<100; i++ ) {
                Thread.sleep(100);
                if(!c.isOffline())
                    break;
            }

            assertFalse("Slave failed to go online", c.isOffline());
            // run some trivial thing
            System.err.println("Calling task...");
            assertEquals("done", c.getChannel().callAsync(new NoopTask()).get(5, TimeUnit.MINUTES));
            System.err.println("...done.");
        } finally {
            proc.kill();
        }

        Thread.sleep(500);
        assertTrue(c.isOffline());
    }

    /**
     * Determines the link to the .jnlp file.
     */
    private String getJnlpLink(Computer c) throws Exception {
        HtmlPage p = new WebClient().goTo("computer/"+c.getName()+"/");
        String href = ((HtmlAnchor) p.getElementById("jnlp-link")).getHrefAttribute();
        href = new URL(new URL(p.getDocumentURI()),href).toExternalForm();
        return href;
    }

    /**
     * Adds a JNLP {@link Slave} to the system and returns it.
     */
    private Computer addTestSlave() throws Exception {
        List<Slave> slaves = new ArrayList<Slave>(hudson.getSlaves());
        File dir = Util.createTempDir();
        slaves.add(new DumbSlave("test","dummy",dir.getAbsolutePath(),"1", Mode.NORMAL, "",
                new JNLPLauncher(), RetentionStrategy.INSTANCE));
        hudson.setSlaves(slaves);
        Computer c = hudson.getComputer("test");
        assertNotNull(c);
        return c;
    }

    private static class NoopTask implements Callable<String,RuntimeException> {
        public String call() {
            return "done";
        }

        private static final long serialVersionUID = 1L;
    }
}
