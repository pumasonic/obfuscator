/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.jobf.processors;

import me.superblaubeere27.jobf.IClassProcessor;
import me.superblaubeere27.jobf.JObf;
import me.superblaubeere27.jobf.JObfImpl;
import me.superblaubeere27.jobf.ProcessorCallback;
import me.superblaubeere27.jobf.utils.InliningUtils;
import me.superblaubeere27.jobf.utils.Utils;
import me.superblaubeere27.jobf.utils.values.DeprecationLevel;
import me.superblaubeere27.jobf.utils.values.EnabledValue;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InlineProcessor implements IClassProcessor {
    private static Random random = new Random();
    private static List<String> exceptions = new ArrayList<>();

    private EnabledValue enabled = new EnabledValue("Inlining", "Doesn't work, please don't use this", DeprecationLevel.BAD, false);

    private JObfImpl inst;

    public InlineProcessor(JObfImpl inst) {
        this.inst = inst;
    }

    @Override
    public void process(ProcessorCallback callback, ClassNode node) {
        if (!enabled.getObject()) return;

        int maxPasses = 3;

        boolean ok;
        int index = 0;

        do {
            ok = false;
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode abstractInsnNode : method.instructions.toArray()) {
                    if (abstractInsnNode instanceof MethodInsnNode) {
                        MethodInsnNode insnNode = (MethodInsnNode) abstractInsnNode;

                        ClassNode lookupClass = Utils.lookupClass(insnNode.owner);

                        if (lookupClass == null) continue;

                        MethodNode lookupMethod = Utils.getMethod(lookupClass, insnNode.name, insnNode.desc);

                        if (lookupMethod == null
                                || (lookupMethod.instructions.size() > 100 && !lookupMethod.name.equals("approximiere_pi"))
                                || !InliningUtils.canInlineMethod(node, lookupClass, lookupMethod))
                            continue;

                        InsnList inline = InliningUtils.inline(lookupMethod, lookupClass, method);
//                        inline.insertBefore(inline.getFirst(), NodeUtils.debugString("--- INLINE (" + lookupClass.name + "." + lookupMethod.name + lookupMethod.desc + ") ---"));
//                        inline.add(NodeUtils.debugString("--- END ---"));

//                    System.out.println(NodeUtils.prettyprint(inline));

                        method.instructions.insert(abstractInsnNode, inline);
                        method.instructions.remove(abstractInsnNode);

                        JObf.log.fine("Inlined method in " + node.name + "." + method.name + method.desc + "(" + lookupClass.name + "." + lookupMethod.name + lookupMethod.desc + ")");

                        ok = true;
                    }
                }
            }
            index++;
        } while (ok && index <= maxPasses);

//        System.out.println("Inlined " + inlined + " methods.");

        inst.setWorkDone();
    }


}