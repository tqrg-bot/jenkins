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

import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.model.Node;
import hudson.model.Hudson;
import hudson.util.DescriptorList;
import hudson.Extension;
import hudson.DescriptorExtensionList;

/**
 * {@link Descriptor} for {@link Slave}.
 *
 * <h2>Views</h2>
 * <p>
 * This object needs to have <tt>newInstanceDetail.jelly</tt> view, which shows up in
 * <tt>http://server/hudson/computers/new</tt> page as an explanation of this job type.
 *
 * <h2>Other Implementation Notes</h2>
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class NodeDescriptor extends Descriptor<Node> {
    protected NodeDescriptor(Class<? extends Node> clazz) {
        super(clazz);
    }

    protected NodeDescriptor() {
    }

    public final String newInstanceDetailPage() {
        return '/'+clazz.getName().replace('.','/').replace('$','/')+"/newInstanceDetail.jelly";
    }

    @Override
    public String getConfigPage() {
        return getViewPage(clazz, "configure-entries.jelly");
    }

    /**
     * Returns all the registered {@link NodeDescriptor} descriptors.
     */
    public static DescriptorExtensionList<Node,NodeDescriptor> all() {
        return Hudson.getInstance().getDescriptorList(Node.class);
    }

    /**
     * All the registered instances.
     * @deprecated as of 1.286
     *      Use {@link #all()} for read access, and {@link Extension} for registration.
     */
    public static final DescriptorList<Node> ALL = new DescriptorList<Node>(Node.class);
}
